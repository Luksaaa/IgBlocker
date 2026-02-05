package com.example.igblocker

object Constants {

    const val CHANNEL_ID = "ig_block_channel"
    const val NOTIF_ID = 999

    const val INSTAGRAM_PKG = "com.instagram.android"


    // ⏱ Koliko dugo ostaje otključano (ON) prije nego se vrati na OFF (blokirano)
    // Postavljeno na 1 minutu za test
    const val UNLOCK_DURATION_MS = 1 * 60 * 1000L

    // ⏱ TEST (1 min blok)
    const val BLOCK_DURATION_MS = 1 * 60 * 1000L

}
