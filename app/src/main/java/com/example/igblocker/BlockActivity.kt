package com.example.igblocker

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class BlockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val prefs = getSharedPreferences("ig_prefs", MODE_PRIVATE)
        val cycleStart = prefs.getLong("cycleStart", System.currentTimeMillis())
        val remaining = Constants.BLOCK_DURATION_MS- (System.currentTimeMillis() - cycleStart)

        if (remaining <= 0L) {
            finish()
            return
        }


    }

}
