package com.example.igblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class IGAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return

        // 🛡️ OŠTRA ZAŠTITA OD DEINSTALACIJE I UKIDANJA ADMINA
        // Skeniramo sistemske postavke i instalacijske procese
        if (pkg.contains("settings") || pkg.contains("packageinstaller")) {
            val rootNode = rootInActiveWindow ?: event.source ?: return
            val allText = getAllText(rootNode).lowercase()
            
            // Meta je naša aplikacija
            val isTarget = allText.contains("ig blocker")
            
            // Radnje koje želimo blokirati
            val dangerousKeywords = listOf(
                "uninstall", "deinstall", "ukloni", "obriši", "izbriši", 
                "force stop", "prisilno zaustavi", "deactivate", "deaktiviraj", 
                "clear data", "očisti podatke", "storage", "pohrana"
            )

            val isDangerousAction = dangerousKeywords.any { allText.contains(it) }

            if (isTarget && isDangerousAction) {
                // Mudra i oštra obrana: korisnik se šalje na početni ekran
                performGlobalAction(GLOBAL_ACTION_HOME)
                rootNode.recycle()
                return
            }
            rootNode.recycle()
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
