package com.example.wassupguard

import android.app.Application

/**
 * Application class - runs when app starts
 * This is where we initialize things that need to exist app-wide, like the database
 * 
 * Think of this as a "setup" function that runs once when your app launches,
 * before any screen (Activity) is shown.
 */
class WassupGuardApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize database when app starts
        // Database is created lazily when first accessed via AppDatabase.getDatabase()
    }
}

