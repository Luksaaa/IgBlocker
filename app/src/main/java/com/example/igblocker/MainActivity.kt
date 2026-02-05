package com.example.igblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.work.*
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

            // Osvježava UI svake sekunde za točan prikaz preostalog vremena
            LaunchedEffect(Unit) {
                while(true) {
                    currentTime = System.currentTimeMillis()
                    isUnlocked = prefs.getBoolean("is_unlocked", false)
                    delay(1000)
                }
            }

            val lastUnlock = prefs.getLong("unlock_start", 0L)
            val nextAvailable = lastUnlock + Constants.COOLDOWN_DURATION_MS
            val waitMillis = nextAvailable - currentTime

            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(if (isUnlocked) Color(0xFF00C853) else Color.Red, CircleShape)
                            .clickable {
                                val currentWait = (prefs.getLong("unlock_start", 0L) + Constants.COOLDOWN_DURATION_MS) - System.currentTimeMillis()

                                if (!isUnlocked) {
                                    if (currentWait <= 0) {
                                        isUnlocked = true
                                        prefs.edit().apply {
                                            putBoolean("is_unlocked", true)
                                            putLong("unlock_start", System.currentTimeMillis())
                                        }.apply()
                                        startWorker()
                                    } else {
                                        val m = (currentWait / 60000)
                                        val s = (currentWait % 60000) / 1000
                                        Toast.makeText(this@MainActivity, "Čekaj još ${m}m ${s}s", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isUnlocked = false
                                    prefs.edit().putBoolean("is_unlocked", false).apply()
                                    startWorker()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isUnlocked) "ON" else "OFF", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))
                    
                    val statusText = if (isUnlocked) "OTKLJUČANO" 
                                    else if (waitMillis > 0) {
                                        val m = waitMillis / 60000
                                        val s = (waitMillis % 60000) / 1000
                                        "Dostupno za: ${m}m ${s}s"
                                    }
                                    else "SPREMNO ZA KORIŠTENJE"

                    Text(statusText, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }

    private fun startWorker() {
        val work = OneTimeWorkRequestBuilder<InstagramLimitWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork("ig_worker", ExistingWorkPolicy.REPLACE, work)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(Constants.CHANNEL_ID, "IG Block", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Instagram Blocker Status"
            }
            nm.createNotificationChannel(channel)
        }
    }
}
