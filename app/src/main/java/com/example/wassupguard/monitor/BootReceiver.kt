package com.example.wassupguard.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Log.w(TAG, "All files access missing after boot. Service not started.")
            return
        }
        FileMonitorService.start(context)
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}

