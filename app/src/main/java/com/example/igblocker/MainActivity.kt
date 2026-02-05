package com.example.igblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)

        setContent {
<<<<<<< HEAD
            var isUnlocked by remember {
                mutableStateOf(prefs.getBoolean("is_unlocked", false))
=======

            var blockActive by remember {
                mutableStateOf(prefs.getBoolean("block_active", false))
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
            }

            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
<<<<<<< HEAD
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                color = if (isUnlocked) Color(0xFF00C853) else Color.Red,
                                shape = CircleShape
                            )
                            .clickable {
                                if (!isUnlocked) {
                                    prefs.edit()
                                        .putBoolean("is_unlocked", true)
                                        .putLong("unlock_start", System.currentTimeMillis())
                                        .apply()
                                    isUnlocked = true
                                } else {
                                    prefs.edit()
                                        .putBoolean("is_unlocked", false)
                                        .putLong("unlock_start", 0L)
                                        .apply()
                                    isUnlocked = false
                                }
                                // Odmah pokreni worker da ažurira obavijest
                                startWorker()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked) "ON" else "OFF",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = if (isUnlocked) "Instagram je OTKLJUČAN" else "Instagram je BLOKIRAN",
                        color = Color.Gray,
                        fontSize = 14.sp
=======

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            color = if (blockActive) Color.Red else Color(0xFF00C853),
                            shape = CircleShape
                        )
                        .clickable {

                            // 👉 SAMO AKO NIJE BLOKIRANO
                            if (!blockActive) {

                                prefs.edit()
                                    .putBoolean("block_active", true)
                                    .putLong("block_start", System.currentTimeMillis())
                                    .apply()

                                startWorker()

                                blockActive = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (blockActive) "OFF" else "ON",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
                    )
                }
            }
        }
    }

    private fun startWorker() {
<<<<<<< HEAD
        val oneTimeWork = OneTimeWorkRequestBuilder<InstagramLimitWorker>().build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "ig_block_worker",
                ExistingWorkPolicy.REPLACE,
                oneTimeWork
=======
        val work = PeriodicWorkRequestBuilder<InstagramLimitWorker>(
            1, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "ig_block_worker",
                ExistingPeriodicWorkPolicy.REPLACE,
                work
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
            )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Instagram Block",
<<<<<<< HEAD
                NotificationManager.IMPORTANCE_LOW // Koristimo LOW da ne "pinga" svaku promjenu tajmera
            ).apply {
                setShowBadge(false)
=======
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
>>>>>>> a4d021dcbde99dca50d3ac8165cd794d63af0d36
            }

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
