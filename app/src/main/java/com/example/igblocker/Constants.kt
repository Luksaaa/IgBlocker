package com.example.igblocker

object Constants {
    const val CHANNEL_ID = "ig_block_channel"
    const val NOTIF_ID = 999
    const val INSTAGRAM_PKG = "com.instagram.android"
    
    // ⏱ Koliko dugo se Instagram može koristiti (1 minuta)
    const val UNLOCK_DURATION_MS = 60 * 1000L
    
    // ⏱ Koliko dugo se mora čekati nakon korištenja (1 sat)
    const val COOLDOWN_DURATION_MS = 60 * 60 * 1000L
}
