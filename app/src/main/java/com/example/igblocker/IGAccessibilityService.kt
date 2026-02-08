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

        // 🛡️ ZAŠTITA OD DEINSTALACIJE I GAŠENJA (Samo za IG Blocker)
        // Provjeravamo sustavne postavke i instalater paketa
        if (packageName == "com.android.settings" || packageName == "com.google.android.packageinstaller" || packageName == "com.android.packageinstaller") {
            val rootNode = rootInActiveWindow ?: return
            
            // Tražimo tekst "IG Blocker" na ekranu postavki
            val nodes = rootNode.findAccessibilityNodeInfosByText("IG Blocker")
            if (nodes.isNotEmpty()) {
                val allText = getAllText(rootNode).lowercase()
                
                // Ključne riječi koje ukazuju na pokušaj micanja ili gašenja aplikacije
                val dangerousKeywords = listOf(
                    "uninstall", "deinstall", "ukloni", "obriši", "izbriši", 
                    "force stop", "prisilno zaustavi", "clear data", "očisti podatke", "pohrana"
                )
                
                if (dangerousKeywords.any { allText.contains(it) }) {
                    // Ako korisnik pokuša kliknuti na bilo što od ovoga za našu aplikaciju, baci ga na Home
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    return
                }
            }
        }

        // 🚫 PAMETNO BLOKIRANJE ODABRANIH APLIKACIJA (Postojeći kod)
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

    // Pomoćna funkcija za čitanje cijelog teksta na ekranu
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
