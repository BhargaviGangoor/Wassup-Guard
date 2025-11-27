package com.example.wassupguard.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.wassupguard.R
import com.example.wassupguard.util.Notifications
import com.example.wassupguard.util.WhatsAppFileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps WhatsApp file observers alive even when the app is backgrounded.
 */
class FileMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val observerMap = mutableMapOf<String, WhatsAppFileObserver>()
    private lateinit var fileScanner: FileScanner

    override fun onCreate() {
        super.onCreate()
        fileScanner = FileScanner(applicationContext)
        Notifications.ensureChannels(this)
        startForeground(NOTIFICATION_ID, buildNotification())
        serviceScope.launch { initializeObservers() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch { refreshObservers() }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        observerMap.values.forEach { it.stopWatching() }
        observerMap.clear()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun initializeObservers() {
        refreshObservers()
    }

    private fun refreshObservers() {
        val directories = WhatsAppFileObserver.getMandatoryPaths()
        val pathsToRemove = observerMap.keys - directories
        pathsToRemove.forEach { path ->
            observerMap.remove(path)?.stopWatching()
        }

        directories.forEach { path ->
            if (!observerMap.containsKey(path)) {
                val observer = WhatsAppFileObserver(path) { createdPath ->
                    fileScanner.scanFile(createdPath)
                }
                observer.startWatching()
                observerMap[path] = observer
            }
        }
    }

    private fun buildNotification(): Notification {
        val channelId = ensureServiceChannel()
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notif_foreground_title))
            .setContentText(getString(R.string.notif_foreground_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun ensureServiceChannel(): String {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                getString(R.string.notif_foreground_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_foreground_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
        return FOREGROUND_CHANNEL_ID
    }

    companion object {
        private const val NOTIFICATION_ID = 1101
        private const val FOREGROUND_CHANNEL_ID = "file_monitor_foreground"

        fun start(context: Context) {
            val intent = Intent(context, FileMonitorService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}

