package com.example.igblocker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BlockForegroundService : Service() {

    private var countDownTimer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val isUnlocked = prefs.getBoolean("is_unlocked", false)
        val unlockStart = prefs.getLong("unlock_start", 0L)
        val unlockEnd = unlockStart + Constants.UNLOCK_DURATION_MS
        val now = System.currentTimeMillis()

        if (isUnlocked && now < unlockEnd) {
            startTimer(unlockEnd - now)
        } else {
            // Ako nije otključano, servis se ne bi trebao pokretati ili se gasi
            stopSelf()
        }

        return START_STICKY
    }

    private fun startTimer(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_unlocked", false).apply()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    private fun updateNotification(remainingMs: Long) {
        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val unlockStart = prefs.getLong("unlock_start", 0L)
        val unlockEnd = unlockStart + Constants.UNLOCK_DURATION_MS

        val timeStr = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(remainingMs),
            TimeUnit.MILLISECONDS.toSeconds(remainingMs) % 60)

        val unlockAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(unlockEnd))

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Instagram je OTKLJUČAN")
            .setContentText("Blokiranje za: $timeStr (u $unlockAt)")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        startForeground(Constants.NOTIF_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
