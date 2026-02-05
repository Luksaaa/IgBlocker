package com.example.igblocker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class InstagramLimitWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val prefs = applicationContext
            .getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)

        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val blockActive = prefs.getBoolean("block_active", false)
        if (!blockActive) {
            nm.cancel(Constants.NOTIF_ID)
            return Result.success()
        }

        val start = prefs.getLong("block_start", 0L)
        val now = System.currentTimeMillis()
        val end = start + Constants.BLOCK_DURATION_MS
        val remaining = end - now

        // 🔓 BLOK ZAVRŠIO
        if (remaining <= 0) {

            prefs.edit()
                .putBoolean("block_active", false)
                .putLong("block_start", 0L)
                .apply()

            nm.cancel(Constants.NOTIF_ID)
            return Result.success()
        }

        // ⏱ FORMAT VREMENA
        val minutes = remaining / 60000
        val seconds = (remaining % 60000) / 1000

        val unlockTime = SimpleDateFormat("HH:mm")
            .format(Date(end))

        val notification = NotificationCompat.Builder(
            applicationContext,
            Constants.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Instagram blocked")
            .setContentText(
                "Unlock in ${minutes}m ${seconds}s • at $unlockTime"
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .build()

        nm.notify(Constants.NOTIF_ID, notification)

        return Result.success()
    }
}
