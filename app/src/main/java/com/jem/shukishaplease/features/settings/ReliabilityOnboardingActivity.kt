package com.jem.shukishaplease.features.settings

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.trip.IncomingCallAlertActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReliabilityOnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShukishaPleaseTheme {
                ReliabilityOnboardingScreen(
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun ReliabilityOnboardingScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showManufacturerHelpSheet by remember { mutableStateOf(false) }

    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else true

    val hasFullScreenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        notificationManager.canUseFullScreenIntent()
    } else true

    val isBatteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else true

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Security", tint = NeoBlack, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MAKE ALARM RELIABLE",
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = NeoMutedGrey)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Complete this checklist to ensure your stop alarm rings even in deep sleep or battery saver mode:",
                    fontSize = 13.sp,
                    color = NeoMutedGrey,
                    lineHeight = 18.sp
                )

                // 1. Location
                ChecklistRow(
                    title = "1. Location Access",
                    description = "Required to track GPS distance to your stop.",
                    isDone = hasLocation,
                    onFix = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )

                // 2. Notifications
                ChecklistRow(
                    title = "2. Notifications Permission",
                    description = "Required to post high-priority arrival alerts.",
                    isDone = hasNotification,
                    onFix = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )

                // 3. Exact Alarms
                ChecklistRow(
                    title = "3. Exact Hardware Alarms",
                    description = "Guarantees backup alarm fires exactly on time.",
                    isDone = hasExactAlarm,
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    }
                )

                // 4. Full-Screen Alerts
                ChecklistRow(
                    title = "4. Full-Screen Lockscreen Alerts",
                    description = "Displays the alarm activity over the lockscreen.",
                    isDone = hasFullScreenIntent,
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                // 5. Battery Optimization
                ChecklistRow(
                    title = "5. Ignore Battery Saver",
                    description = "Prevents system from killing tracking in deep sleep.",
                    isDone = isBatteryIgnored,
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                )

                // Manufacturer Help Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showManufacturerHelpSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Help", tint = NeoBlack)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Phone Help: Tecno, Infinix, Xiaomi, Samsung",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Test My Alarm Button
                Button(
                    onClick = {
                        Toast.makeText(context, "🔔 Test alarm scheduled! Full-screen alarm in 10 seconds...", Toast.LENGTH_LONG).show()
                        coroutineScope.launch {
                            delay(10000)
                            val fullScreenIntent = Intent(context, IncomingCallAlertActivity::class.java).apply {
                                putExtra("EXTRA_DEST_NAME", "Test Destination Stage")
                                putExtra("EXTRA_DIST_TEXT", "200 m")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(fullScreenIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Test", tint = NeoYellow)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Test My Alarm 🔔", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }

        if (showManufacturerHelpSheet) {
            ManufacturerHelpSheet(onDismiss = { showManufacturerHelpSheet = false })
        }
    }
}

@Composable
fun ChecklistRow(
    title: String,
    description: String,
    isDone: Boolean,
    onFix: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(2.dp, NeoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 12.sp, color = NeoMutedGrey)
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isDone) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeoTeal
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = PureWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PureWhite)
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.clickable { onFix() },
                    shape = RoundedCornerShape(12.dp),
                    color = NeoYellow,
                    border = BorderStroke(1.5.dp, NeoBlack)
                ) {
                    Text(
                        text = "FIX IT",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                }
            }
        }
    }
}

@Composable
fun ManufacturerHelpSheet(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = { Text(text = "Manufacturer Battery Steps 📱", fontWeight = FontWeight.Black, color = NeoBlack) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "• Tecno / Infinix:", fontWeight = FontWeight.Bold, color = NeoBlack)
                Text(text = "Open Phone Master ➔ Auto-start management ➔ Enable Shukisha Please.", fontSize = 12.sp, color = NeoMutedGrey)

                Text(text = "• Xiaomi / Poco / Redmi:", fontWeight = FontWeight.Bold, color = NeoBlack)
                Text(text = "Settings ➔ Apps ➔ Permissions ➔ Autostart ➔ Enable Shukisha. Set Battery Saver to 'No restrictions'.", fontSize = 12.sp, color = NeoMutedGrey)

                Text(text = "• Samsung Galaxy:", fontWeight = FontWeight.Bold, color = NeoBlack)
                Text(text = "Settings ➔ Device Care ➔ Battery ➔ Background usage limits ➔ Never sleeping apps ➔ Add Shukisha Please.", fontSize = 12.sp, color = NeoMutedGrey)

                Text(text = "• Oppo / Realme:", fontWeight = FontWeight.Bold, color = NeoBlack)
                Text(text = "Settings ➔ Battery ➔ App Battery Management ➔ Allow Background Activity.", fontSize = 12.sp, color = NeoMutedGrey)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}
