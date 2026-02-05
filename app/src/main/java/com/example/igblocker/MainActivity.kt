package com.example.igblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)

        setContent {
            var isUnlocked by remember { mutableStateOf(prefs.getBoolean("is_unlocked", false)) }
            var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentTime = System.currentTimeMillis()
                    val currentIsUnlocked = prefs.getBoolean("is_unlocked", false)
                    if (isUnlocked != currentIsUnlocked) {
                        isUnlocked = currentIsUnlocked
                    }
                    delay(1000)
                }
            }

            val lastUnlock = prefs.getLong("unlock_start", 0L)
            val unlockEnd = lastUnlock + Constants.UNLOCK_DURATION_MS
            val cooldownEnd = lastUnlock + Constants.COOLDOWN_DURATION_MS

            val unlockRemaining = unlockEnd - currentTime
            val cooldownRemaining = cooldownEnd - currentTime

            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(if (isUnlocked) Color(0xFF00C853) else Color.Red, CircleShape)
                            .clickable {
                                if (!isUnlocked) {
                                    if (cooldownRemaining <= 0) {
                                        prefs.edit().apply {
                                            putBoolean("is_unlocked", true)
                                            putLong("unlock_start", System.currentTimeMillis())
                                        }.apply()
                                        startForegroundService(Intent(this@MainActivity, BlockForegroundService::class.java))
                                    } else {
                                        val m = cooldownRemaining / 60000
                                        val s = (cooldownRemaining % 60000) / 1000
                                        Toast.makeText(this@MainActivity, "Čekaj još ${m}m ${s}s", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Manualno gašenje servisa (opcionalno)
                                    prefs.edit().putBoolean("is_unlocked", false).apply()
                                    stopService(Intent(this@MainActivity, BlockForegroundService::class.java))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isUnlocked) "ON" else "OFF", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))

                    val statusText = when {
                        isUnlocked -> {
                            val m = unlockRemaining / 60000
                            val s = (unlockRemaining % 60000) / 1000
                            "OTKLJUČANO: ${m}m ${s}s"
                        }
                        cooldownRemaining > 0 -> {
                            val m = cooldownRemaining / 60000
                            val s = (cooldownRemaining % 60000) / 1000
                            "Dostupno za: ${m}m ${s}s"
                        }
                        else -> "SPREMNO ZA KORIŠTENJE"
                    }
                    Text(statusText, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(Constants.CHANNEL_ID, "IG Blocker", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
    }
}
