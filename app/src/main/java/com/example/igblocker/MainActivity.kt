package com.example.igblocker

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
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
            // isBlockingActive: false -> Gumb prikazuje "ON" (zeleno, ali blokiranje je ugašeno)
            // isBlockingActive: true  -> Gumb prikazuje "OFF" (crveno, blokiranje je aktivno)
            var isBlockingActive by remember { mutableStateOf(prefs.getBoolean("is_blocking_active", false)) }
            var blockedPackages by remember { mutableStateOf(prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()) }
            var showAppPicker by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                while (true) {
                    isBlockingActive = prefs.getBoolean("is_blocking_active", false)
                    blockedPackages = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
                    delay(1000)
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- IKONE ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayList = remember(blockedPackages) {
                            blockedPackages.toList().take(4)
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
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.DarkGray).clickable { 
                                showAppPicker = true 
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }

                    // --- GLAVNI GUMB ---
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(if (!isBlockingActive) Color(0xFF00C853) else Color.Red, CircleShape)
                            .clickable {
                                if (!isBlockingActive) {
                                    // Želimo AKTIVIRATI blokiranje (Prebacujemo na OFF stanje)
                                    if (!isAdminActive()) { checkAndRequestDeviceAdmin(); return@clickable }
                                    if (!isUsageAccessGranted()) { openUsageAccessSettings(); return@clickable }

                                    prefs.edit { putBoolean("is_blocking_active", true) }
                                    isBlockingActive = true
                                    startBlockService()
                                } else {
                                    // Želimo UGASITI blokiranje (Prebacujemo na ON stanje)
                                    prefs.edit { putBoolean("is_blocking_active", false) }
                                    isBlockingActive = false
                                    stopService(Intent(this@MainActivity, BlockForegroundService::class.java))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // ON znači "Sve je ok, aplikacija ne blokira"
                        // OFF znači "Blokiranje je aktivno"
                        Text(if (!isBlockingActive) "ON" else "OFF", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(if (isBlockingActive) "Blokiranje AKTIVNO (Limit: 30min / 2h)" else "Blokiranje UGAŠENO", 
                        color = Color.Gray, fontSize = 14.sp)
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
        val icon = remember(packageName) { try { context.packageManager.getApplicationIcon(packageName) } catch (e: Exception) { null } }
        icon?.let {
            Image(bitmap = it.toBitmap().asImageBitmap(), contentDescription = null, 
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick))
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
            onDismissRequest = onDismiss, containerColor = Color(0xFF1A1A1A),
            title = { Text("Odaberi aplikacije", color = Color.White) },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(apps) { app ->
                        val isSelected = selectedApps.contains(app.packageName)
                        Row(modifier = Modifier.fillMaxWidth().clickable { onAppSelected(app.packageName) }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(app.packageName, onClick = {})
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(context.packageManager.getApplicationLabel(app).toString(), 
                                color = if (isSelected) Color.Green else Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("ZATVORI", color = Color.Red) } }
        )
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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
        try {
            startForegroundService(Intent(this, BlockForegroundService::class.java))
        } catch (e: Exception) {
            Toast.makeText(this, "Greška pri pokretanju servisa: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(Constants.CHANNEL_ID, "System Sync", NotificationManager.IMPORTANCE_LOW))
    }
}
