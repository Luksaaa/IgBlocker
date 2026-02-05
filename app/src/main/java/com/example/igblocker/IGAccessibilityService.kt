package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // 🛡️ ZAŠTITA OD BRISANJA (Blokiranje postavki aplikacija)
        // Ako korisnik pokuša ući u postavke aplikacija ili uređaja
        if (pkg == "com.android.settings") {
            val contentDescription = event.contentDescription?.toString()?.lowercase() ?: ""
            val className = event.className?.toString()?.lowercase() ?: ""
            
            // Ako se u postavkama spominje "ig blocker", "admin" ili "deinstaliraj", izbaci ga
            if (contentDescription.contains("ig blocker") || 
                className.contains("uninstalldeviceadmin") || 
                className.contains("deviceadminadd")) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                return
            }
        }

        // 📸 BLOKIRANJE INSTAGRAMA
        if (pkg == Constants.INSTAGRAM_PKG) {
            val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
            val nowElapsed = SystemClock.elapsedRealtime()
            val startElapsed = prefs.getLong("unlock_start_elapsed", 0L)
            
            val isExpired = nowElapsed >= (startElapsed + Constants.UNLOCK_DURATION_MS)
            val isUnlocked = prefs.getBoolean("is_unlocked", false) && !isExpired

            if (!isUnlocked) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                if (isExpired && prefs.getBoolean("is_unlocked", false)) {
                    prefs.edit().putBoolean("is_unlocked", false).apply()
                }
            }
        }
    }

    override fun onInterrupt() {}
}
