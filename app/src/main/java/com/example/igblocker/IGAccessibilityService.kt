package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // Provjeri je li otvoren Instagram
        if (pkg != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        var isUnlocked = prefs.getBoolean("is_unlocked", false)
        val unlockStart = prefs.getLong("unlock_start", 0L)
        val now = System.currentTimeMillis()

        // 🔄 Provjeri je li vrijeme otključanosti (ON) isteklo
        if (isUnlocked && unlockStart > 0) {
            val unlockEnd = unlockStart + Constants.UNLOCK_DURATION_MS
            if (now >= unlockEnd) {
                isUnlocked = false
                prefs.edit()
                    .putBoolean("is_unlocked", false)
                    .putLong("unlock_start", 0L)
                    .apply()
            }
        }

        // Ako je isUnlocked = false (stanje OFF), blokiraj pristup
        if (!isUnlocked) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
