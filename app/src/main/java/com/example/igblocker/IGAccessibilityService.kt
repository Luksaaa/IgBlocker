package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class IGAccessibilityService : AccessibilityService() {

    private lateinit var intelligentMonitoring: IntelligentMonitoring

    override fun onCreate() {
        super.onCreate()
        intelligentMonitoring = IntelligentMonitoring(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        // 🛡️ ZAŠTITA OD DEINSTALACIJE I GAŠENJA (Samo za Blocky)
        if (packageName == "com.android.settings" || packageName == "com.google.android.packageinstaller" || packageName == "com.android.packageinstaller") {
            val rootNode = rootInActiveWindow ?: return
            
            // Tražimo tekst "Blocky" na ekranu (naziv aplikacije)
            val nodes = rootNode.findAccessibilityNodeInfosByText("Blocky")
            if (nodes.isNotEmpty()) {
                val allText = getAllText(rootNode).lowercase()
                
                // Ključne riječi koje ukazuju na pokušaj micanja, gašenja ili micanja admin ovlasti
                val dangerousKeywords = listOf(
                    "uninstall", "deinstall", "ukloni", "obriši", "izbriši", 
                    "force stop", "prisilno zaustavi", "clear data", "očisti podatke", "pohrana",
                    "deactivate", "deaktiviraj", "admin", "administrator"
                )
                
                if (dangerousKeywords.any { allText.contains(it) }) {
                    // Ako detektiramo pokušaj micanja zaštite, baci korisnika na Home screen
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    return
                }
            }
        }

        // 🚫 PAMETNO BLOKIRANJE ODABRANIH APLIKACIJA
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName == this.packageName || packageName == "com.android.settings") return

            val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
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

    private fun getAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                sb.append(getAllText(child))
                child.recycle()
            }
        }
        return sb.toString()
    }

    override fun onInterrupt() {}
}
