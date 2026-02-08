package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_service_channel"
    const val NOTIF_ID = 888
    
    const val INSTAGRAM_PKG = "com.instagram.android"
    const val WHATSAPP_PKG = "com.whatsapp"
    const val SNAPCHAT_PKG = "com.snapchat.android"
    
    val DEFAULT_APPS = listOf(INSTAGRAM_PKG, WHATSAPP_PKG, SNAPCHAT_PKG)

    // Inteligentno praćenje
    const val USAGE_WINDOW_MS = 2 * 60 * 1000L // 2 sata
    const val USAGE_LIMIT_MS = 2 * 60 * 1000L      // 30 minuta limita
    const val BLOCK_DURATION_MS = 2 * 60 * 1000L   // Trajanje blokade
}
