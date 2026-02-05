package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.packageName != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val unlockStart = prefs.getLong("unlock_start", 0L)
        
        // Instagram je otključan samo ako je is_unlocked true I ako nije prošla 1 minuta
        val isExpired = now >= (unlockStart + Constants.UNLOCK_DURATION_MS)
        val isUnlocked = prefs.getBoolean("is_unlocked", false) && !isExpired

        if (!isUnlocked) {
            // Blokiraj ako nije otključano ili je vrijeme isteklo
            performGlobalAction(GLOBAL_ACTION_HOME)
            
            // Ako je vrijeme isteklo a još stoji is_unlocked = true, ugasi ga
            if (isExpired && prefs.getBoolean("is_unlocked", false)) {
                prefs.edit().putBoolean("is_unlocked", false).apply()
            }
        }
    }

    override fun onInterrupt() {}
}
