package com.example.wassupguard.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wassupguard.R

object Notifications {
    const val CHANNEL_ID_ALERTS = "scan_alerts"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                context.getString(R.string.notif_channel_scan_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_scan_alerts_desc)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun notifyThreat(context: Context, title: String, text: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher) // using launcher icon for now
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}


