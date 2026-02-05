package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return


        if (pkg != Constants.INSTAGRAM_PKG) return

        val prefs = getSharedPreferences("ig_prefs", MODE_PRIVATE)
        val blockActive = prefs.getBoolean("block_active", false)

        if (blockActive) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
