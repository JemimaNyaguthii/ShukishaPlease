package com.jem.shukishaplease.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.BuildConfig
import com.jem.shukishaplease.MainActivity
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.common.TrustedContact
import com.jem.shukishaplease.core.ota.OtaUpdateDialog
import com.jem.shukishaplease.core.ota.OtaUpdateInfo
import com.jem.shukishaplease.core.ota.OtaUpdateManager
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.home.BottomNavBarSection
import com.jem.shukishaplease.features.play.ShukishaPlayHubActivity
import com.jem.shukishaplease.features.trip.IncomingCallAlertActivity
import com.jem.shukishaplease.features.trip.TripActivity
import com.jem.shukishaplease.features.trip.TripsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShukishaPleaseTheme {
                SettingsScreen(
                    onClose = { finish() },
                    onNavigateHome = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onNavigateTrips = {
                        startActivity(Intent(this, TripsActivity::class.java))
                        finish()
                    },
                    onNavigatePlay = {
                        startActivity(Intent(this, ShukishaPlayHubActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateTrips: () -> Unit,
    onNavigatePlay: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    val coroutineScope = rememberCoroutineScope()
    val otaManager = remember { OtaUpdateManager(context) }

    var isCheckingOta by remember { mutableStateOf(false) }
    var activeOtaInfo by remember { mutableStateOf<OtaUpdateInfo?>(null) }
    var selectedTab by remember { mutableStateOf(3) } // Settings / Profile tab selected

    var trustedContacts by remember { mutableStateOf(prefs.getTrustedContacts()) }

    // System Contact Picker Launcher (Zero READ_CONTACTS permission needed)
    val maxContacts = 3
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        if (contactUri != null) {
            var name = "Contact"
            var phone = "No Phone"
            try {
                // 1. Query contact display name and ID
                val cursor = context.contentResolver.query(contactUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        name = cursor.getString(nameIdx) ?: "Contact"
                    }
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIdx >= 0) {
                        val contactId = cursor.getString(idIdx)

                        // 2. Query phone numbers table
                        val pCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        if (pCursor != null) {
                            if (pCursor.moveToFirst()) {
                                val numIdx = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIdx >= 0) {
                                    phone = pCursor.getString(numIdx) ?: "No Phone"
                                }
                            }
                            pCursor.close()
                        }
                    }
                    cursor.close()
                }

                // Fallback: If phone is still "No Phone", query phone content provider directly using contactUri
                if (phone == "No Phone") {
                    val phoneCursor = context.contentResolver.query(
                        contactUri,
                        null,
                        null,
                        null,
                        null
                    )
                    phoneCursor?.close()
                }
            } catch (e: Exception) {
                Log.e("ContactPicker", "Error picking contact phone: ${e.localizedMessage}")
            }

            Log.d("ContactPicker", "Successfully picked: Name=$name, Phone=$phone")

            if (trustedContacts.size < maxContacts) {
                val updated = trustedContacts + TrustedContact(name, phone)
                trustedContacts = updated
                prefs.saveTrustedContacts(updated)
                Toast.makeText(context, "Added $name ($phone)!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Maximum 3 trusted contacts allowed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = NeoBlack, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMUTER PROFILE & SETTINGS",
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = NeoMutedGrey)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Commuter Name Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = NeoBlack, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Commuter Name", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        var editedName by remember { mutableStateOf(prefs.userName) }

                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { newName ->
                                editedName = newName
                                prefs.userName = if (newName.isNotBlank()) newName else "Msafiri"
                            },
                            label = { Text("Enter Your Name", color = NeoMutedGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NeoBlack,
                                unfocusedTextColor = NeoBlack,
                                focusedBorderColor = NeoBlack,
                                unfocusedBorderColor = NeoBlack
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Trusted Contacts Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Contacts", tint = NeoBlack, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Trusted Contacts (${trustedContacts.size}/3)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            }

                            if (trustedContacts.size < maxContacts) {
                                Surface(
                                    modifier = Modifier.clickable { contactPickerLauncher.launch(null) },
                                    shape = CircleShape,
                                    color = NeoYellow,
                                    border = BorderStroke(2.dp, NeoBlack)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact", tint = NeoBlack, modifier = Modifier.padding(6.dp).size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (trustedContacts.isEmpty()) {
                            Text(text = "No trusted contacts added yet. Tap '+' to select from contacts.", fontSize = 12.sp, color = NeoMutedGrey)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                trustedContacts.forEachIndexed { index, contact ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = NeoCreamBackground,
                                        border = BorderStroke(1.5.dp, NeoBlack)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeoBlack)
                                                Text(text = contact.phone, fontSize = 12.sp, color = NeoMutedGrey)
                                            }

                                            IconButton(
                                                onClick = {
                                                    val updated = trustedContacts.toMutableList().apply { removeAt(index) }
                                                    trustedContacts = updated
                                                    prefs.saveTrustedContacts(updated)
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = EndTripRed, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Explanation Text
                        Text(
                            text = "Who sees your location and when: Trusted contacts only receive your live location tracking link when you explicitly tap 'Share trip' during an active journey.",
                            fontSize = 11.sp,
                            color = NeoMutedGrey,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 3. Alarm Reliability & Testing Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoYellow),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "🛡️ ALARM RELIABILITY & TESTING", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Check permissions, exact alarms, and battery optimizations so you never miss your stop.", fontSize = 12.sp, color = NeoBlack)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, ReliabilityOnboardingActivity::class.java)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(23.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                            ) {
                                Text("Checklist 🛡️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

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
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(23.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = NeoBlack),
                                border = BorderStroke(2.dp, NeoBlack)
                            ) {
                                Text("Test Alarm 🔔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "🚨 Test SOS triggered! Opening emergency sheet...", Toast.LENGTH_SHORT).show()
                                val sosIntent = Intent(context, TripActivity::class.java).apply {
                                    putExtra("EXTRA_DEST_NAME", "Test Destination")
                                    putExtra("EXTRA_DEST_LAT", -0.4201)
                                    putExtra("EXTRA_DEST_LON", 36.9476)
                                }
                                context.startActivity(sosIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoCoral, contentColor = PureWhite),
                            border = BorderStroke(2.dp, NeoBlack)
                        ) {
                            Text("Test SOS Mode 🚨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // 4. App Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "SHUKISHA PLEASE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Installed Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", fontSize = 13.sp, color = NeoMutedGrey)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Smart Commuter Stop Alerts & Mini-Games", fontSize = 12.sp, color = NeoMutedGrey)
                    }
                }

                // 5. Check for OTA Updates Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = "Update", tint = NeoBlack, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Over-The-Air (OTA) Updates", fontWeight = FontWeight.Black, fontSize = 14.sp, color = NeoBlack)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Check for the latest app updates directly without Play Store.", fontSize = 12.sp, color = NeoMutedGrey)
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isCheckingOta = true
                                coroutineScope.launch {
                                    val info = otaManager.checkForUpdates("https://raw.githubusercontent.com/example/shukisha/main/update.json")
                                    isCheckingOta = false
                                    if (info.isUpdateAvailable) {
                                        activeOtaInfo = info
                                    } else {
                                        Toast.makeText(context, "Shukisha Please is up to date! (v${BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite),
                            enabled = !isCheckingOta
                        ) {
                            if (isCheckingOta) {
                                CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Check for OTA Updates 🚀", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation Bar
            BottomNavBarSection(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    when (index) {
                        0 -> onNavigateHome()
                        1 -> onNavigateTrips()
                        2 -> onNavigatePlay()
                    }
                }
            )
        }

        // OTA Update Dialog
        if (activeOtaInfo != null) {
            OtaUpdateDialog(
                updateInfo = activeOtaInfo!!,
                onDismiss = { activeOtaInfo = null },
                onUpdateNow = { apkUrl ->
                    otaManager.downloadAndInstallApk(apkUrl)
                    activeOtaInfo = null
                }
            )
        }
    }
}
