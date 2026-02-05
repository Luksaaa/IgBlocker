package com.example.igblocker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class InstagramLimitWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isUnlocked = prefs.getBoolean("is_unlocked", false)
        val unlockStart = prefs.getLong("unlock_start", 0L)
        val now = System.currentTimeMillis()

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, Constants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        if (isUnlocked) {
            val unlockEnd = unlockStart + Constants.UNLOCK_DURATION_MS
            val remaining = unlockEnd - now

            if (remaining <= 0) {
                // Vrijeme isteklo, prebaci na blokirano
                prefs.edit()
                    .putBoolean("is_unlocked", false)
                    .putLong("unlock_start", 0L)
                    .apply()
                
                showBlockedNotification(nm, builder)
            } else {
                // Prikaži preostalo vrijeme
                val minutes = remaining / 60000
                val seconds = (remaining % 60000) / 1000
                
                builder.setContentTitle("Instagram je OTKLJUČAN")
                    .setContentText("Ponovno blokiranje za: ${minutes}m ${seconds}s")
                    .setColor(0xFF00C853.toInt()) // Zelena

                nm.notify(Constants.NOTIF_ID, builder.build())

                // Ponovno pokreni worker za 10 sekundi da osvježi tajmer
                val nextWork = OneTimeWorkRequestBuilder<InstagramLimitWorker>()
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(applicationContext).enqueue(nextWork)
            }
        } else {
            showBlockedNotification(nm, builder)
        }

        return Result.success()
    }

    private fun showBlockedNotification(nm: NotificationManager, builder: NotificationCompat.Builder) {
        builder.setContentTitle("Instagram je BLOKIRAN")
            .setContentText("Klikni za otključavanje u aplikaciji")
            .setColor(0xFFFF0000.toInt()) // Crvena
        
        nm.notify(Constants.NOTIF_ID, builder.build())
    }
}
