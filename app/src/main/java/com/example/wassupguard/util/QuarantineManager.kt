package com.example.wassupguard.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * QuarantineManager - Isolates malicious files safely
 * 
 * What it does:
 * - Moves dangerous files to a secure quarantine folder
 * - Prevents files from being accessed by other apps
 * - Keeps a record of quarantined files
 * 
 * Think of it like: A safety vault where dangerous files are locked away
 */
object QuarantineManager {
    private const val TAG = "QuarantineManager"
    private const val QUARANTINE_FOLDER = "quarantine"

    /**
     * Get the quarantine directory (creates if doesn't exist)
     */
    private fun getQuarantineDir(context: Context): File {
        val quarantineDir = File(context.filesDir, QUARANTINE_FOLDER)
        if (!quarantineDir.exists()) {
            quarantineDir.mkdirs()
        }
        return quarantineDir
    }

    /**
     * Quarantine a malicious file
     * Moves the file to quarantine folder and renames it with timestamp
     * 
     * @param file The malicious file to quarantine
     * @param context Android context
     * @return The quarantined file path, or null if failed
     */
    fun quarantineFile(file: File, context: Context): File? {
        return try {
            if (!file.exists() || !file.isFile) {
                Log.w(TAG, "File does not exist or is not a file: ${file.path}")
                return null
            }

            val quarantineDir = getQuarantineDir(context)
            val timestamp = System.currentTimeMillis()
            val originalName = file.name
            val quarantinedName = "${timestamp}_${originalName}"
            val quarantinedFile = File(quarantineDir, quarantinedName)

            // Copy file to quarantine (we copy instead of move to be safe)
            FileInputStream(file).use { input ->
                FileOutputStream(quarantinedFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Delete original file after successful copy
            if (quarantinedFile.exists() && quarantinedFile.length() == file.length()) {
                file.delete()
                Log.i(TAG, "File quarantined: $originalName -> ${quarantinedFile.name}")
                quarantinedFile
            } else {
                Log.e(TAG, "Failed to quarantine file: $originalName")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error quarantining file: ${file.path}", e)
            null
        }
    }

    /**
     * Check if a file is in quarantine
     */
    fun isQuarantined(file: File, context: Context): Boolean {
        val quarantineDir = getQuarantineDir(context)
        return file.path.startsWith(quarantineDir.path)
    }

    /**
     * Get list of all quarantined files
     */
    fun getQuarantinedFiles(context: Context): List<File> {
        val quarantineDir = getQuarantineDir(context)
        return quarantineDir.listFiles()?.filter { it.isFile } ?: emptyList()
    }
}

