package com.example.igblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
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
import androidx.core.content.edit
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createNotificationChannel()

        // POTPUNA SLOBODA: Nema više automatskog iskakanja postavki ovdje.
        
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)

        setContent {
            var isUnlocked by remember { mutableStateOf(prefs.getBoolean("is_unlocked", false)) }
            var currentElapsed by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentElapsed = SystemClock.elapsedRealtime()
                    isUnlocked = prefs.getBoolean("is_unlocked", false)
                    delay(1000)
                }
            }

            val lastUnlockElapsed = prefs.getLong("unlock_start_elapsed", 0L)
            val waitMillis = (lastUnlockElapsed + Constants.COOLDOWN_DURATION_MS) - currentElapsed

            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(if (isUnlocked) Color(0xFF00C853) else Color.Red, CircleShape)
                            .clickable {
                                // Postavke se otvaraju SAMO kada ti svjesno klikneš na gumb
                                if (!isAdminActive()) {
                                    checkAndRequestDeviceAdmin()
                                    return@clickable
                                }
                                if (!isAccessibilityServiceEnabled(this@MainActivity)) {
                                    openAccessibilitySettings()
                                    return@clickable
                                }

                                val nowElapsed = SystemClock.elapsedRealtime()
                                val currentWait = (prefs.getLong("unlock_start_elapsed", 0L) + Constants.COOLDOWN_DURATION_MS) - nowElapsed

                                if (!isUnlocked) {
                                    if (currentWait <= 0) {
                                        isUnlocked = true
                                        prefs.edit {
                                            putBoolean("is_unlocked", true)
                                            putLong("unlock_start_elapsed", nowElapsed)
                                        }
                                        startBlockService()
                                        moveTaskToBack(true) // Samo pri aktivaciji se minimizira
                                    } else {
                                        val m = currentWait / 60000
                                        val s = (currentWait % 60000) / 1000
                                        Toast.makeText(this@MainActivity, "Čekaj još ${m}m ${s}s", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isUnlocked = false
                                    prefs.edit { putBoolean("is_unlocked", false) }
                                    stopService(Intent(this@MainActivity, BlockForegroundService::class.java))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isUnlocked) "ON" else "OFF", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val statusText = if (isUnlocked) {
                        val remaining = (lastUnlockElapsed + Constants.UNLOCK_DURATION_MS) - currentElapsed
                        if (remaining > 0) {
                            val m = remaining / 60000
                            val s = (remaining % 60000) / 1000
                            "OTKLJUČANO: ${m}m ${s}s"
                        } else "BLOKIRANO"
                    } else if (waitMillis > 0) {
                        val m = waitMillis / 60000
                        val s = (waitMillis % 60000) / 1000
                        "Dostupno za: ${m}m ${s}s"
                    } else "SPREMNO"

                    Text(statusText, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ovdje više nema koda koji te izbacuje van.
        // Kad uđeš u aplikaciju, ostaješ u njoj dok sam ne izađeš.
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun startBlockService() {
        startForegroundService(Intent(this, BlockForegroundService::class.java))
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${IGAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(service) == true
    }

    private fun isAdminActive(): Boolean {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        return dpm.isAdminActive(ComponentName(this, MyDeviceAdminReceiver::class.java))
    }

    private fun checkAndRequestDeviceAdmin() {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        if (!dpm.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Zaštita od deinstalacije.")
            }
            startActivity(intent)
        }
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(Constants.CHANNEL_ID, "System Sync", NotificationManager.IMPORTANCE_LOW))
    }
}
