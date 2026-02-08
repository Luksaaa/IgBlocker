package com.example.igblocker

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BlockForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val isBlockingActive = prefs.getBoolean("is_blocking_active", false)

        if (isBlockingActive) {
            showNotification()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("IG Blocker je AKTIVAN")
            .setContentText("Pratim korištenje aplikacija (limit 30min/2h)")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(PendingIntent.getActivity(
                this, 0, 
                Intent(this, MainActivity::class.java), 
                PendingIntent.FLAG_IMMUTABLE
            ))
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(Constants.NOTIF_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
