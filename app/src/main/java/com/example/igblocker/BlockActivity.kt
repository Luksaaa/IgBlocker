package com.example.igblocker

import android.app.Activity
import android.os.Bundle
<<<<<<< HEAD
import android.view.WindowManager
=======
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36

class BlockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val prefs = getSharedPreferences("ig_prefs", MODE_PRIVATE)
<<<<<<< HEAD
        val isUnlocked = prefs.getBoolean("is_unlocked", false)

        // Ako je korisnik u međuvremenu otključao, zatvori ovaj ekran
        if (isUnlocked) {
=======
        val cycleStart = prefs.getLong("cycleStart", System.currentTimeMillis())
        val remaining = Constants.BLOCK_DURATION_MS- (System.currentTimeMillis() - cycleStart)

        if (remaining <= 0L) {
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
            finish()
            return
        }

<<<<<<< HEAD
        // Ovdje možeš dodati layout za block ekran ako ga imaš
        // npr. setContentView(R.layout.activity_block)
    }
=======

    }

>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
}
