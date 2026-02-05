package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_service_channel"
    const val NOTIF_ID = 888
    const val INSTAGRAM_PKG = "com.instagram.android"
    
    // --- LAKO ZA PROMIJENITI ---
    private const val MINUTES_TO_USE = 1.0     // Koliko minuta se može koristiti
    private const val MINUTES_TO_WAIT = 60.0   // Koliko minuta se čeka nakon korištenja
    // --------------------------

    const val UNLOCK_DURATION_MS = (MINUTES_TO_USE * 60 * 1000).toLong()
    const val COOLDOWN_DURATION_MS = (MINUTES_TO_WAIT * 60 * 1000).toLong()
}
