package com.example.wassupguard.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wassupguard.util.HashUtils
import com.example.wassupguard.util.Notifications
import java.io.File

class FileMonitorWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val TAG = "FileMonitorWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started.")
        return try {
            // For now, scan files within app's files directory as a placeholder.
            // Integration with WhatsApp media will use MediaStore in a subsequent step.
            val appFilesDir: File = applicationContext.filesDir
            val files = appFilesDir.listFiles()?.toList().orEmpty()

            var scanned = 0
            files.forEach { f ->
                if (f.isFile) {
                    val sha = HashUtils.sha256(f)
                    Log.d(TAG, "Scanned ${f.name} sha256=$sha")
                    scanned++
                }
            }

            Notifications.notifyThreat(
                applicationContext,
                title = applicationContext.getString(com.example.wassupguard.R.string.notif_scan_complete_title),
                text = applicationContext.getString(com.example.wassupguard.R.string.notif_scan_complete_text, scanned),
                notificationId = 1001
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during file monitoring", e)
            Result.failure()
        }
    }
}


