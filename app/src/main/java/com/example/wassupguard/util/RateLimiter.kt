package com.example.wassupguard.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.delay

/**
 * RateLimiter - Ensures VirusTotal API calls comply with free tier limits:
 * - 4 requests per minute
 * - 500 requests per day
 * - 15,500 requests per month
 * 
 * This prevents violating API terms and getting blocked
 */
object RateLimiter {
    private const val TAG = "RateLimiter"
    private const val PREFS_NAME = "virustotal_rate_limiter"
    
    // VirusTotal free tier limits
    private const val REQUESTS_PER_MINUTE = 4
    private const val REQUESTS_PER_DAY = 500
    private const val REQUESTS_PER_MONTH = 15500
    
    // Minimum delay between requests (15 seconds = 4 per minute)
    private const val MIN_DELAY_MS = 15_000L // 15 seconds
    
    // Track last request time
    private var lastRequestTime = 0L
    
    /**
     * Check if we can make an API call and wait if needed
     * Returns true if call can proceed, false if quota exceeded
     */
    suspend fun canMakeRequest(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check daily quota
        val dailyCount = getDailyCount(prefs)
        if (dailyCount >= REQUESTS_PER_DAY) {
            Log.w(TAG, "Daily quota exceeded: $dailyCount/$REQUESTS_PER_DAY")
            return false
        }
        
        // Check monthly quota
        val monthlyCount = getMonthlyCount(prefs)
        if (monthlyCount >= REQUESTS_PER_MONTH) {
            Log.w(TAG, "Monthly quota exceeded: $monthlyCount/$REQUESTS_PER_MONTH")
            return false
        }
        
        // Enforce minimum delay between requests
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        
        if (timeSinceLastRequest < MIN_DELAY_MS) {
            val waitTime = MIN_DELAY_MS - timeSinceLastRequest
            Log.d(TAG, "Rate limiting: waiting ${waitTime}ms before next request")
            delay(waitTime)
        }
        
        lastRequestTime = System.currentTimeMillis()
        return true
    }
    
    /**
     * Record that an API request was made
     */
    fun recordRequest(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        // Increment daily count
        val dailyCount = getDailyCount(prefs) + 1
        val dailyKey = getDailyKey()
        editor.putInt(dailyKey, dailyCount)
        
        // Increment monthly count
        val monthlyCount = getMonthlyCount(prefs) + 1
        val monthlyKey = getMonthlyKey()
        editor.putInt(monthlyKey, monthlyCount)
        
        editor.apply()
        
        Log.d(TAG, "API request recorded - Daily: $dailyCount/$REQUESTS_PER_DAY, Monthly: $monthlyCount/$REQUESTS_PER_MONTH")
    }
    
    /**
     * Get daily request count
     */
    private fun getDailyCount(prefs: SharedPreferences): Int {
        val dailyKey = getDailyKey()
        val count = prefs.getInt(dailyKey, 0)
        
        // Reset if it's a new day (check if key exists for today)
        val lastDailyKey = prefs.getString("last_daily_key", "")
        if (lastDailyKey != dailyKey) {
            // New day - reset counter
            prefs.edit().putString("last_daily_key", dailyKey).apply()
            return 0
        }
        
        return count
    }
    
    /**
     * Get monthly request count
     */
    private fun getMonthlyCount(prefs: SharedPreferences): Int {
        val monthlyKey = getMonthlyKey()
        val count = prefs.getInt(monthlyKey, 0)
        
        // Reset if it's a new month
        val lastMonthlyKey = prefs.getString("last_monthly_key", "")
        if (lastMonthlyKey != monthlyKey) {
            // New month - reset counter
            prefs.edit().putString("last_monthly_key", monthlyKey).apply()
            return 0
        }
        
        return count
    }
    
    /**
     * Get daily key (format: "daily_YYYY-MM-DD")
     */
    private fun getDailyKey(): String {
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        return "daily_$date"
    }
    
    /**
     * Get monthly key (format: "monthly_YYYY-MM")
     */
    private fun getMonthlyKey(): String {
        val date = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
            .format(java.util.Date())
        return "monthly_$date"
    }
    
    /**
     * Get current usage stats
     */
    fun getUsageStats(context: Context): UsageStats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UsageStats(
            dailyCount = getDailyCount(prefs),
            dailyLimit = REQUESTS_PER_DAY,
            monthlyCount = getMonthlyCount(prefs),
            monthlyLimit = REQUESTS_PER_MONTH
        )
    }
    
    /**
     * Reset all counters (for testing/debugging)
     */
    fun resetCounters(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.i(TAG, "Rate limiter counters reset")
    }
    
    data class UsageStats(
        val dailyCount: Int,
        val dailyLimit: Int,
        val monthlyCount: Int,
        val monthlyLimit: Int
    ) {
        val dailyRemaining: Int get() = dailyLimit - dailyCount
        val monthlyRemaining: Int get() = monthlyLimit - monthlyCount
        val dailyPercentage: Float get() = (dailyCount.toFloat() / dailyLimit) * 100
        val monthlyPercentage: Float get() = (monthlyCount.toFloat() / monthlyLimit) * 100
    }
}

