package com.example.igblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        val prefs = getSharedPreferences("ig_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("blocked_packages")) {
            prefs.edit { putStringSet("blocked_packages", Constants.DEFAULT_APPS.toSet()) }
        }

        setContent {
            var isUnlocked by remember { mutableStateOf(prefs.getBoolean("is_unlocked", false)) }
            var currentElapsed by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
            var blockedPackages by remember { mutableStateOf(prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()) }
            var showAppPicker by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentElapsed = SystemClock.elapsedRealtime()
                    isUnlocked = prefs.getBoolean("is_unlocked", false)
                    blockedPackages = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
                    delay(1000)
                }
            }

            val lastUnlockElapsed = prefs.getLong("unlock_start_elapsed", 0L)
            val waitMillis = (lastUnlockElapsed + Constants.COOLDOWN_DURATION_MS) - currentElapsed

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- IKONE BLOKIRANIH APLIKACIJA ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayList = remember(blockedPackages) {
                            val list = blockedPackages.toMutableList()
                            if (list.contains(Constants.INSTAGRAM_PKG)) {
                                list.remove(Constants.INSTAGRAM_PKG)
                                list.add(0, Constants.INSTAGRAM_PKG)
                            }
                            list.take(4)
                        }

                        displayList.forEach { pkg ->
                            AppIcon(pkg, onClick = { 
                                val newSet = blockedPackages.toMutableSet()
                                newSet.remove(pkg)
                                prefs.edit { putStringSet("blocked_packages", newSet) }
                                blockedPackages = newSet
                            })
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                                .clickable { showAppPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }

                    // --- GLAVNI GUMB ---
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(if (isUnlocked) Color(0xFF00C853) else Color.Red, CircleShape)
                            .clickable {
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
                                        moveTaskToBack(true)
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
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
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

                    Text(statusText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                if (showAppPicker) {
                    AppPickerDialog(
                        onDismiss = { showAppPicker = false },
                        onAppSelected = { pkg ->
                            val newSet = blockedPackages.toMutableSet()
                            if (newSet.contains(pkg)) newSet.remove(pkg) else newSet.add(pkg)
                            prefs.edit { putStringSet("blocked_packages", newSet) }
                            blockedPackages = newSet
                        },
                        selectedApps = blockedPackages
                    )
                }
            }
        }
    }

    @Composable
    fun AppIcon(packageName: String, onClick: () -> Unit) {
        val context = LocalContext.current
        val icon = remember(packageName) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        }
        
        icon?.let {
            Image(
                bitmap = it.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick)
            )
        }
    }

    @Composable
    fun AppPickerDialog(onDismiss: () -> Unit, onAppSelected: (String) -> Unit, selectedApps: Set<String>) {
        val context = LocalContext.current
        val apps = remember {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0 }
                .sortedBy { context.packageManager.getApplicationLabel(it).toString() }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Odaberi aplikacije", color = Color.White) },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(apps) { app ->
                        val isSelected = selectedApps.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(app.packageName) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(app.packageName, onClick = {})
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                context.packageManager.getApplicationLabel(app).toString(),
                                color = if (isSelected) Color.Green else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("DA", color = Color.Green, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("ZATVORI", color = Color.Red) }
            }
        )
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${IGAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(service) == true
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun isAdminActive(): Boolean {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        return dpm.isAdminActive(ComponentName(this, MyDeviceAdminReceiver::class.java))
    }

    private fun checkAndRequestDeviceAdmin() {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Zaštita aplikacije.")
        }
        startActivity(intent)
    }

    private fun startBlockService() {
        startForegroundService(Intent(this, BlockForegroundService::class.java))
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(Constants.CHANNEL_ID, "System Sync", NotificationManager.IMPORTANCE_LOW))
    }
}
