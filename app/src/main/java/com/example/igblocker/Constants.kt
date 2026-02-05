package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_channel"
    const val NOTIF_ID = 999
    const val INSTAGRAM_PKG = "com.instagram.android"
    
    // --- LAKO ZA PROMIJENITI ---
    private const val MINUTES_TO_USE = 1       // Promijeni ovo za test (npr. 0.5 za 30s)
    private const val MINUTES_TO_WAIT = 1 // Koliko se čeka (60 min = 1 sat)
    // ---------------------------

    const val UNLOCK_DURATION_MS = (MINUTES_TO_USE * 60 * 1000).toLong()
    const val COOLDOWN_DURATION_MS = (MINUTES_TO_WAIT * 60 * 1000).toLong()
}
