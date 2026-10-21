package com.jem.shukishaplease.features.trip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.MainActivity
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.home.BottomNavBarSection
import com.jem.shukishaplease.features.play.ShukishaPlayHubActivity
import com.jem.shukishaplease.features.settings.SettingsActivity

class TripsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShukishaPleaseTheme {
                TripsScreen(
                    onClose = { finish() },
                    onNavigateHome = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onNavigatePlay = {
                        startActivity(Intent(this, ShukishaPlayHubActivity::class.java))
                        finish()
                    },
                    onNavigateSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        finish()
                    },
                    onStartRoute = { name, lat, lon ->
                        val intent = Intent(this, TripActivity::class.java).apply {
                            putExtra("EXTRA_DEST_NAME", name)
                            putExtra("EXTRA_DEST_LAT", lat)
                            putExtra("EXTRA_DEST_LON", lon)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun TripsScreen(
    onClose: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigatePlay: () -> Unit,
    onNavigateSettings: () -> Unit,
    onStartRoute: (String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    var selectedTab by remember { mutableStateOf(1) } // Trips tab selected

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
                    Icon(imageVector = Icons.Default.Subway, contentDescription = "Trips", tint = NeoBlack, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMUTER TRIPS & ROUTES",
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
                // Active Trip Live Monitor Card (if trip is active)
                if (prefs.isTripActive) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔴 LIVE JOURNEY MONITORING", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeoCoral)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NeoYellow,
                                    border = BorderStroke(1.5.dp, NeoBlack)
                                ) {
                                    Text(text = "Active", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = prefs.activeTripDestination, fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Background tracking active. We will alert you before you arrive.", fontSize = 12.sp, color = NeoMutedGrey)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val serviceIntent = Intent(context, TripTrackingService::class.java)
                                    context.stopService(serviceIntent)
                                    prefs.clearActiveTrip()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EndTripRed, contentColor = PureWhite)
                            ) {
                                Text(text = "End Active Trip", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Pre-trip journey prompt card
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
                            Text(text = "🗺️ NO ACTIVE JOURNEY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Ready for your next commute?", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Select a frequent route below or set a custom destination on the Home screen to start background stop alerts.", fontSize = 12.sp, color = NeoMutedGrey)
                        }
                    }
                }

                // 1. Commuter Lifetime Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoYellow),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "TOTAL COMMUTE STATS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${prefs.gamesCompleted + 4} Trips Completed", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "128.4 km total distance tracked", fontSize = 12.sp, color = NeoBlack)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeoTeal,
                            border = BorderStroke(1.5.dp, NeoBlack)
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PureWhite
                            )
                        }
                    }
                }

                // 2. Favorite / Frequent Routes Quick-Launch
                Text(text = "FREQUENT ROUTES", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeoBlack)

                RouteQuickLaunchCard(
                    title = "Karatina Stage → Nyeri Town",
                    distance = "14.2 km · ~35 mins",
                    onClick = { onStartRoute("Nyeri Town", -0.4201, 36.9476) }
                )

                RouteQuickLaunchCard(
                    title = "CBD General Post Office → Westlands",
                    distance = "6.5 km · ~20 mins",
                    onClick = { onStartRoute("Westlands / Sarit Centre", -1.2618, 36.8055) }
                )

                RouteQuickLaunchCard(
                    title = "Kasarani Mwiki → CBD Nairobi",
                    distance = "12.8 km · ~45 mins",
                    onClick = { onStartRoute("CBD / GPO", -1.286389, 36.817223) }
                )

                // 3. Recent Journeys History Log
                Text(text = "RECENT JOURNEYS", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeoBlack)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = NeoBlack, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Karatina Stage → Nyeri Town", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeoBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Completed Yesterday · Stop alert triggered successfully", fontSize = 12.sp, color = NeoMutedGrey)
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
                        2 -> onNavigatePlay()
                        3 -> onNavigateSettings()
                    }
                }
            )
        }
    }
}

@Composable
fun RouteQuickLaunchCard(
    title: String,
    distance: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(2.dp, NeoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeoSkyBlue,
                border = BorderStroke(1.5.dp, NeoBlack),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = NeoBlack, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeoBlack)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = distance, fontSize = 12.sp, color = NeoMutedGrey)
            }

            Surface(
                shape = CircleShape,
                color = NeoYellow,
                border = BorderStroke(1.5.dp, NeoBlack),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = NeoBlack, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
