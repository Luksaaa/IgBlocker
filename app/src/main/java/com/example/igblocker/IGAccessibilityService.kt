package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class IGAccessibilityService : AccessibilityService() {

    private lateinit var intelligentMonitoring: IntelligentMonitoring

    override fun onCreate() {
        super.onCreate()
        intelligentMonitoring = IntelligentMonitoring(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            if (packageName == this.packageName || packageName == "com.android.settings") return

            val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
            // Usklađeno s MainActivity: "is_blocking_active"
            val isBlockingActive = prefs.getBoolean("is_blocking_active", false)
            
            if (!isBlockingActive) return

            val blockedPackages = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()

            if (blockedPackages.contains(packageName)) {
                if (intelligentMonitoring.isOverLimit(packageName)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
        }
    }

    override fun onInterrupt() {}
}
