package com.example.igblocker

import android.accessibilityservice.AccessibilityService
<<<<<<< HEAD
import android.content.Context
=======
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

<<<<<<< HEAD
        // Samo ako se pokušava otvoriti Instagram
        if (pkg != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        var isUnlocked = prefs.getBoolean("is_unlocked", false)
        val unlockStart = prefs.getLong("unlock_start", 0L)
        val now = System.currentTimeMillis()

        // 🔄 Provjeri je li vrijeme otključanosti isteklo
        if (isUnlocked && unlockStart > 0) {
            val unlockEnd = unlockStart + Constants.UNLOCK_DURATION_MS
            if (now >= unlockEnd) {
                // Vrijeme je isteklo, vrati na OFF (blokirano)
                isUnlocked = false
                prefs.edit()
                    .putBoolean("is_unlocked", false)
                    .putLong("unlock_start", 0L)
                    .apply()
            }
        }

        // AKO JE OFF (isUnlocked = false), BLOKIRAJ
        if (!isUnlocked) {
=======

        if (pkg != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", MODE_PRIVATE)
        val blockActive = prefs.getBoolean("block_active", false)

        if (blockActive) {
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
