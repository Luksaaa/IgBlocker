package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.packageName != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val nowElapsed = SystemClock.elapsedRealtime()
        val startElapsed = prefs.getLong("unlock_start_elapsed", 0L)
        
        // Provjeri je li otključan i je li vrijeme isteklo (elapsedRealtime je imun na promjenu sata)
        val isExpired = nowElapsed >= (startElapsed + Constants.UNLOCK_DURATION_MS)
        val isUnlocked = prefs.getBoolean("is_unlocked", false) && !isExpired

        if (!isUnlocked) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            if (isExpired && prefs.getBoolean("is_unlocked", false)) {
                prefs.edit().putBoolean("is_unlocked", false).apply()
            }
        }
    }

    override fun onInterrupt() {}
}
