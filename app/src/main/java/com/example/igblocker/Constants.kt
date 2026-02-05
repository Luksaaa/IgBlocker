package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_service_channel"
    const val NOTIF_ID = 888
    const val INSTAGRAM_PKG = "com.instagram.android"
    
    // Konfiguracija vremena (minuti)
    private const val MINUTES_TO_USE = 1.0
    private const val MINUTES_TO_WAIT = 60.0

    const val UNLOCK_DURATION_MS = (MINUTES_TO_USE * 60 * 1000).toLong()
    const val COOLDOWN_DURATION_MS = (MINUTES_TO_WAIT * 60 * 1000).toLong()
}
