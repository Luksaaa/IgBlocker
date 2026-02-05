package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)

        // 🛡️ PRECIZNA ZAŠTITA OD DEINSTALACIJE
        // Ciljamo samo ekran s detaljima naše aplikacije u Postavkama
        if (pkg.contains("settings") || pkg.contains("packageinstaller")) {
            val rootNode = rootInActiveWindow ?: event.source ?: return
            
            // Tražimo točno našu aplikaciju na ekranu
            val nodes = rootNode.findAccessibilityNodeInfosByText("IG Blocker")
            if (nodes.isNotEmpty()) {
                val allText = getAllText(rootNode).lowercase()
                
                // Blokiramo samo ako su vidljivi gumbi za brisanje ili gašenje admina
                val dangerousKeywords = listOf(
                    "uninstall", "deinstall", "ukloni", "obriši", "izbriši", 
                    "force stop", "prisilno zaustavi", "clear data", "očisti podatke"
                )

                if (dangerousKeywords.any { allText.contains(it) }) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    rootNode.recycle()
                    return
                }
            }
            rootNode.recycle()
        }

        // 🚫 BLOKIRANJE SAMO ODABRANIH APLIKACIJA
        val blockedPackages = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
        
        if (blockedPackages.contains(pkg)) {
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
