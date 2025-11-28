package com.example.wassupguard.util

import com.example.wassupguard.network.VirusTotalResponse

/**
 * SafeScoreCalculator - Calculates a safety score (0-100) for scanned files
 * 
 * What it does:
 * - Analyzes VirusTotal scan results
 * - Calculates a score based on threat detections
 * - Provides a clear safety rating for users
 * 
 * Score breakdown:
 * - 90-100: Safe (very few or no detections)
 * - 70-89:  Mostly Safe (some suspicious detections)
 * - 50-69:  Suspicious (multiple detections)
 * - 0-49:   Dangerous (many malicious detections)
 */
object SafeScoreCalculator {

    /**
     * Calculate safety score from VirusTotal response
     * 
     * @param response VirusTotal API response
     * @return Safety score from 0 (dangerous) to 100 (safe)
     */
    fun calculateScore(response: VirusTotalResponse?): Int {
        if (response == null || response.data?.attributes?.lastAnalysisStats == null) {
            // No data available - return neutral score
            return 50
        }

        val stats = response.data.attributes.lastAnalysisStats
        val harmless = stats.harmless ?: 0
        val malicious = stats.malicious ?: 0
        val suspicious = stats.suspicious ?: 0
        val undetected = stats.undetected ?: 0
        val timeout = stats.timeout ?: 0

        val totalScans = harmless + malicious + suspicious + undetected + timeout

        if (totalScans == 0) {
            // No scan data
            return 50
        }

        // Calculate score based on threat ratio
        // Higher harmless = higher score
        // Higher malicious/suspicious = lower score
        val safeRatio = harmless.toFloat() / totalScans
        val threatRatio = (malicious + suspicious).toFloat() / totalScans

        // Base score from safe ratio (0-70 points)
        var score = (safeRatio * 70).toInt()

        // Penalty for threats (subtract up to 50 points)
        val threatPenalty = (threatRatio * 50).toInt()
        score -= threatPenalty

        // Bonus for high harmless count (add up to 30 points)
        if (harmless > 50) {
            score += 30
        } else if (harmless > 20) {
            score += 15
        }

        // Ensure score is between 0 and 100
        return score.coerceIn(0, 100)
    }

    /**
     * Get safety label from score
     */
    fun getSafetyLabel(score: Int): String {
        return when {
            score >= 90 -> "Safe"
            score >= 70 -> "Mostly Safe"
            score >= 50 -> "Suspicious"
            else -> "Dangerous"
        }
    }

    /**
     * Get safety color (for UI)
     */
    fun getSafetyColor(score: Int): String {
        return when {
            score >= 90 -> "Green"
            score >= 70 -> "Yellow"
            score >= 50 -> "Orange"
            else -> "Red"
        }
    }
}

