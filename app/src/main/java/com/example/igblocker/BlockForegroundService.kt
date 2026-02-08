package com.example.igblocker

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class BlockForegroundService : Service() {

    private lateinit var intelligentMonitoring: IntelligentMonitoring
    private val handler = Handler(Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkActiveAppAndBlock()
            handler.postDelayed(this, 5000) // Provjeravaj svakih 5 sekundi
        }
    }

    override fun onCreate() {
        super.onCreate()
        intelligentMonitoring = IntelligentMonitoring(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val isBlockingActive = prefs.getBoolean("is_blocking_active", false)

        if (isBlockingActive) {
            showNotification()
            handler.post(checkRunnable)
        } else {
            handler.removeCallbacks(checkRunnable)
            stopSelf()
        }

        return START_STICKY
    }

    private fun checkActiveAppAndBlock() {
        val currentApp = getForegroundPackageName() ?: return
        
        // Ne blokiraj sustav ili sebe
        if (currentApp == packageName || currentApp == "com.android.settings") return

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val blockedPackages = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()

        if (blockedPackages.contains(currentApp)) {
            if (intelligentMonitoring.isOverLimit(currentApp)) {
                // Izbaci korisnika na početni zaslon
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
            }
        }
    }

    private fun getForegroundPackageName(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val events = usm.queryEvents(time - 10000, time)
        val event = UsageEvents.Event()
        var lastPackage: String? = null
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("IG Blocker je AKTIVAN")
            .setContentText("Pratim korištenje aplikacija u stvarnom vremenu")
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

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }
}
