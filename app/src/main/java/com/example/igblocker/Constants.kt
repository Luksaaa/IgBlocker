package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_service_channel"
    const val NOTIF_ID = 888
    const val INSTAGRAM_PKG = "com.instagram.android"
    const val ACTION_STOP_SERVICE = "STOP_BLOCKER_SERVICE"
    
    // --- KONFIGURACIJA (Lako za mijenjati) ---
    private const val MINUTES_TO_BLOCK = 15.0  // Koliko dugo traje blokada
    
    const val BLOCK_DURATION_MS = (MINUTES_TO_BLOCK * 60 * 1000).toLong()
}
