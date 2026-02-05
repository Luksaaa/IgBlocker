package com.example.igblocker

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

class BlockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val prefs = getSharedPreferences("ig_prefs", MODE_PRIVATE)
        val isUnlocked = prefs.getBoolean("is_unlocked", false)

        // Ako je korisnik u međuvremenu otključao, zatvori ovaj ekran
        if (isUnlocked) {
            finish()
            return
        }

        // Ovdje možeš dodati layout za block ekran ako ga imaš
        // npr. setContentView(R.layout.activity_block)
    }
}
