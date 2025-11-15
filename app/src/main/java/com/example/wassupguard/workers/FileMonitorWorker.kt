package com.example.wassupguard.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wassupguard.data.AppDatabase
import com.example.wassupguard.data.entity.ScanLog
import com.example.wassupguard.data.entity.Signature
import com.example.wassupguard.network.ApiClient
import com.example.wassupguard.network.VirusTotalApi
import com.example.wassupguard.util.HashUtils
import com.example.wassupguard.util.Notifications
import com.example.wassupguard.util.QuarantineManager
import com.example.wassupguard.util.RateLimiter
import com.example.wassupguard.util.SafeScoreCalculator
import com.example.wassupguard.util.WhatsAppFileObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * FileMonitorWorker - Main scanning worker that:
 * 1. Scans WhatsApp media folders for PDF and images
 * 2. Generates SHA-256 hashes
 * 3. Checks local signature database first
 * 4. If not found, queries VirusTotal API
 * 5. Calculates safety score
 * 6. Quarantines malicious files
 * 7. Saves scan results to database
 * 8. Notifies user of threats
 */
class FileMonitorWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val TAG = "FileMonitorWorker"
    private val database = AppDatabase.getDatabase(appContext)
    private val virusTotalApi = ApiClient.createVirusTotalApi()

    // File extensions to scan (PDF and images only)
    private val allowedExtensions = setOf(
        ".pdf",
        ".jpg", ".jpeg",
        ".png",
        ".gif",
        ".bmp",
        ".webp"
    )

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started - scanning WhatsApp media folders")
        return try {
            withContext(Dispatchers.IO) {
                val scannedFiles = scanWhatsAppMedia()
                val threatsFound = scannedFiles.count { it.isThreat }
                
                // Notify user of scan completion
                if (threatsFound > 0) {
                    Notifications.notifyThreat(
                        applicationContext,
                        title = applicationContext.getString(com.example.wassupguard.R.string.notif_threat_detected_title),
                        text = applicationContext.getString(com.example.wassupguard.R.string.notif_threat_detected_text, threatsFound),
                        notificationId = 1002
                    )
                } else {
            Notifications.notifyThreat(
                applicationContext,
                title = applicationContext.getString(com.example.wassupguard.R.string.notif_scan_complete_title),
                        text = applicationContext.getString(com.example.wassupguard.R.string.notif_scan_complete_text, scannedFiles.size),
                notificationId = 1001
            )
                }

            Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during file monitoring", e)
            Result.failure()
        }
    }

    /**
     * Scan WhatsApp media folders for PDF and image files
     */
    private suspend fun scanWhatsAppMedia(): List<ScanResult> {
        val results = mutableListOf<ScanResult>()
        val whatsAppPaths = WhatsAppFileObserver.getWhatsAppMediaPaths()
        
        if (whatsAppPaths.isEmpty()) {
            Log.w(TAG, "No WhatsApp media folders found")
            // Fallback: scan app's files directory for testing
            val appFilesDir = File(applicationContext.filesDir, "test_files")
            if (appFilesDir.exists()) {
                scanDirectory(appFilesDir, results)
            }
            return results
        }

        // Scan each WhatsApp media folder
        whatsAppPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, results)
            }
        }

        return results
    }

    /**
     * Recursively scan a directory for PDF and image files
     */
    private suspend fun scanDirectory(directory: File, results: MutableList<ScanResult>) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Recursively scan subdirectories
                    scanDirectory(file, results)
                } else if (file.isFile && isAllowedFileType(file)) {
                    val result = scanFile(file)
                    results.add(result)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.path}", e)
        }
    }

    /**
     * Check if file is PDF or image
     */
    private fun isAllowedFileType(file: File): Boolean {
        val extension = file.extension.lowercase()
        return allowedExtensions.contains(".$extension")
    }

    /**
     * Scan a single file: hash, check database, query VirusTotal if needed
     */
    private suspend fun scanFile(file: File): ScanResult {
        Log.d(TAG, "Scanning file: ${file.name}")
        
        // Generate SHA-256 hash
        val hash = HashUtils.sha256(file)
        Log.d(TAG, "File hash: $hash")
        
        // Check local signature database first
        val localSignature = database.signatureDao().getByHash(hash)
        
        if (localSignature != null) {
            // Found in local database
            Log.d(TAG, "File found in local database: ${localSignature.threatLabel}")
            val verdict = determineVerdict(localSignature.threatLabel)
            val score = if (verdict == "Malicious" || verdict == "Suspicious") 20 else 80
            
            // Quarantine if malicious
            if (verdict == "Malicious") {
                QuarantineManager.quarantineFile(file, applicationContext)
            }
            
            // Save scan log
            saveScanLog(file, hash, verdict, score, "local")
            
            return ScanResult(file, hash, verdict, score, isThreat = verdict != "Safe")
        }
        
        // Not in local database - query VirusTotal (with rate limiting)
        return try {
            // Check rate limits before making API call
            if (!RateLimiter.canMakeRequest(applicationContext)) {
                Log.w(TAG, "Rate limit reached - skipping API call for ${file.name}")
                // Mark as unknown but don't quarantine (we don't know if it's safe)
                saveScanLog(file, hash, "Unknown (Rate Limited)", 50, "rate_limited")
                return ScanResult(file, hash, "Unknown (Rate Limited)", 50, isThreat = false)
            }
            
            val response = virusTotalApi.getFileReport(hash)
            
            // Record successful API call
            RateLimiter.recordRequest(applicationContext)
            val stats = response.data?.attributes?.lastAnalysisStats
            
            val maliciousCount = stats?.malicious ?: 0
            val suspiciousCount = stats?.suspicious ?: 0
            val harmlessCount = stats?.harmless ?: 0
            
            // Determine verdict
            val verdict = when {
                maliciousCount > 0 -> "Malicious"
                suspiciousCount > 2 -> "Suspicious"
                harmlessCount > 10 -> "Safe"
                else -> "Suspicious"
            }
            
            // Calculate safety score
            val score = SafeScoreCalculator.calculateScore(response)
            
            // Save to local database for future use
            if (verdict != "Safe") {
                database.signatureDao().upsert(
                    Signature(
                        hash = hash,
                        threatLabel = response.data?.attributes?.lastAnalysisStats?.let {
                            "Malicious: $maliciousCount, Suspicious: $suspiciousCount"
                        },
                        source = "virustotal",
                        lastUpdatedEpochMillis = System.currentTimeMillis()
                    )
                )
            }
            
            // Quarantine if malicious
            if (verdict == "Malicious") {
                QuarantineManager.quarantineFile(file, applicationContext)
                Log.w(TAG, "MALICIOUS FILE QUARANTINED: ${file.name}")
            }
            
            // Save scan log
            saveScanLog(file, hash, verdict, score, "virustotal")
            
            ScanResult(file, hash, verdict, score, isThreat = verdict != "Safe")
            
        } catch (e: Exception) {
            // Check if it's a rate limit error (HTTP 429)
            val errorMessage = e.message ?: ""
            if (errorMessage.contains("429") || errorMessage.contains("rate limit") || errorMessage.contains("quota")) {
                Log.w(TAG, "VirusTotal rate limit exceeded for hash: $hash")
                // Don't record request since it failed due to rate limit
                saveScanLog(file, hash, "Unknown (Rate Limited)", 50, "rate_limited")
                return ScanResult(file, hash, "Unknown (Rate Limited)", 50, isThreat = false)
            }
            
            Log.e(TAG, "Error querying VirusTotal for hash: $hash", e)
            // If API fails for other reasons, mark as unknown but don't quarantine
            saveScanLog(file, hash, "Unknown", 50, "error")
            ScanResult(file, hash, "Unknown", 50, isThreat = false)
        }
    }

    /**
     * Determine verdict from threat label
     */
    private fun determineVerdict(threatLabel: String?): String {
        if (threatLabel == null) return "Safe"
        val lower = threatLabel.lowercase()
        return when {
            lower.contains("malicious") -> "Malicious"
            lower.contains("suspicious") -> "Suspicious"
            else -> "Safe"
        }
    }

    /**
     * Save scan result to database
     */
    private suspend fun saveScanLog(
        file: File,
        hash: String,
        verdict: String,
        score: Int,
        source: String
    ) {
        try {
            database.scanLogDao().insert(
                ScanLog(
                    id = 0, // Auto-generated
                    filePath = file.absolutePath,
                    fileName = file.name,
                    fileSizeBytes = file.length(),
                    hashSha256 = hash,
                    verdict = verdict,
                    timestampEpochMillis = System.currentTimeMillis()
                )
            )
            Log.d(TAG, "Scan log saved: ${file.name} - $verdict (Score: $score)")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving scan log", e)
        }
    }

    /**
     * Data class to hold scan results
     */
    private data class ScanResult(
        val file: File,
        val hash: String,
        val verdict: String,
        val score: Int,
        val isThreat: Boolean
    )
}
