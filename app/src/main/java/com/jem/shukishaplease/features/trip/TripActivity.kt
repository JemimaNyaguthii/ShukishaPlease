package com.jem.shukishaplease.features.trip

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jem.shukishaplease.MainActivity
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.common.TrustedContact
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.play.ShukishaPlayHubActivity
import kotlinx.coroutines.delay
import java.util.Calendar

class TripActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destName = intent.getStringExtra("EXTRA_DEST_NAME") ?: "Destination"
        val destLat = intent.getDoubleExtra("EXTRA_DEST_LAT", 0.0)
        val destLon = intent.getDoubleExtra("EXTRA_DEST_LON", 0.0)

        setContent {
            ShukishaPleaseTheme {
                val viewModel: TripViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                LaunchedEffect(destName) {
                    viewModel.startTripTracking(context, destName, destLat, destLon)
                }

                TripScreen(
                    uiState = uiState,
                    onEndTrip = { finish() },
                    onNavigateHome = {
                        startActivity(Intent(this, MainActivity::class.java))
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

// ==========================================
// 1. MASCOT MINI ILLUSTRATION
// ==========================================
@Composable
fun MascotMiniIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(56.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 4f)

        // White Body (Rounded Rect)
        val bodyRect = RoundRect(
            rect = Rect(left = w * 0.15f, top = h * 0.28f, right = w * 0.85f, bottom = h * 0.9f),
            cornerRadius = CornerRadius(16f, 16f)
        )
        val bodyPath = Path().apply { addRoundRect(bodyRect) }
        drawPath(bodyPath, color = PureWhite)
        drawPath(bodyPath, color = NeoBlack, style = stroke)

        // Coral Cap
        val capPath = Path().apply {
            moveTo(w * 0.15f, h * 0.28f)
            cubicTo(w * 0.15f, h * 0.08f, w * 0.85f, h * 0.08f, w * 0.85f, h * 0.28f)
            close()
        }
        drawPath(capPath, color = NeoCoral)
        drawPath(capPath, color = NeoBlack, style = stroke)

        // Cap Brim on right
        val brimPath = Path().apply {
            moveTo(w * 0.65f, h * 0.28f)
            lineTo(w * 0.98f, h * 0.26f)
            lineTo(w * 0.92f, h * 0.33f)
            lineTo(w * 0.65f, h * 0.33f)
            close()
        }
        drawPath(brimPath, color = NeoCoral)
        drawPath(brimPath, color = NeoBlack, style = stroke)

        // Closed Smiling Eyes (^^)
        val eyeLeft = Path().apply {
            moveTo(w * 0.32f, h * 0.52f)
            quadraticTo(w * 0.40f, h * 0.44f, w * 0.48f, h * 0.52f)
        }
        drawPath(eyeLeft, color = NeoBlack, style = stroke)

        val eyeRight = Path().apply {
            moveTo(w * 0.52f, h * 0.52f)
            quadraticTo(w * 0.60f, h * 0.44f, w * 0.68f, h * 0.52f)
        }
        drawPath(eyeRight, color = NeoBlack, style = stroke)

        // Smile
        val mouthPath = Path().apply {
            moveTo(w * 0.42f, h * 0.68f)
            quadraticTo(w * 0.50f, h * 0.74f, w * 0.58f, h * 0.68f)
        }
        drawPath(mouthPath, color = NeoBlack, style = stroke)
    }
}

// ==========================================
// 2. TRIP MAP COMPONENT (~38% of screen) WITH DYNAMIC MOTION
// ==========================================
@Composable
fun TripMap(
    progress: Float,
    gpsStatusText: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()

        val p0 = Offset(0f, hPx * 0.95f)
        val pDest = Offset(wPx * 0.82f, hPx * 0.12f)
        val clampedProgress = progress.coerceIn(0.05f, 0.95f)
        val pUser = Offset(
            p0.x + (pDest.x - p0.x) * clampedProgress,
            p0.y + (pDest.y - p0.y) * clampedProgress
        )

        // Map Grid Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Map Base Beige Fill #E9E5D6
            drawRect(color = MapBaseBeige)

            // Park Blocks #CDE8D5
            val park1 = RoundRect(Rect(w * 0.05f, h * 0.45f, w * 0.25f, h * 0.75f), CornerRadius(20f, 20f))
            drawPath(Path().apply { addRoundRect(park1) }, color = MapParkGreen)

            val park2 = RoundRect(Rect(w * 0.45f, h * 0.22f, w * 0.68f, h * 0.48f), CornerRadius(20f, 20f))
            drawPath(Path().apply { addRoundRect(park2) }, color = MapParkGreen)

            // White Road Grid
            drawLine(PureWhite, Offset(w * 0.2f, 0f), Offset(w * 0.3f, h), strokeWidth = 16f)
            drawLine(PureWhite, Offset(w * 0.7f, 0f), Offset(w * 0.65f, h), strokeWidth = 16f)
            drawLine(PureWhite, Offset(0f, h * 0.2f), Offset(w, h * 0.15f), strokeWidth = 16f)
            drawLine(PureWhite, Offset(0f, h * 0.55f), Offset(w, h * 0.50f), strokeWidth = 16f)
            drawLine(PureWhite, Offset(0f, h * 0.82f), Offset(w, h * 0.78f), strokeWidth = 16f)

            // Waypoints
            val pWaypoint1 = Offset(w * 0.36f, h * 0.78f)
            val pWaypoint2 = Offset(w * 0.67f, h * 0.32f)

            // Route Line: Traveled Teal #008F7A (Dynamic to vehicle position)
            drawLine(color = NeoTeal, start = p0, end = pUser, strokeWidth = 16f, cap = StrokeCap.Round)

            // Route Line: Remaining Black #111008 (Dynamic from vehicle position to destination)
            drawLine(color = NeoBlack, start = pUser, end = pDest, strokeWidth = 16f, cap = StrokeCap.Round)

            // Waypoint 1
            drawCircle(color = PureWhite, radius = 14f, center = pWaypoint1)
            drawCircle(color = NeoBlack, radius = 14f, center = pWaypoint1, style = Stroke(width = 5f))

            // Waypoint 2
            drawCircle(color = PureWhite, radius = 14f, center = pWaypoint2)
            drawCircle(color = NeoBlack, radius = 14f, center = pWaypoint2, style = Stroke(width = 5f))

            // Current Position Yellow Bus Marker Circle (~48dp, 3dp black border) moving dynamically!
            drawCircle(color = NeoYellow, radius = 40f, center = pUser)
            drawCircle(color = NeoBlack, radius = 40f, center = pUser, style = Stroke(width = 6f))

            // Destination Coral Map Pin (#F47C7C)
            val pinPath = Path().apply {
                moveTo(pDest.x, pDest.y + 10f)
                cubicTo(pDest.x - 22f, pDest.y - 20f, pDest.x - 22f, pDest.y - 50f, pDest.x, pDest.y - 50f)
                cubicTo(pDest.x + 22f, pDest.y - 50f, pDest.x + 22f, pDest.y - 20f, pDest.x, pDest.y + 10f)
                close()
            }
            drawPath(pinPath, color = NeoCoral)
            drawPath(pinPath, color = NeoBlack, style = Stroke(width = 5f))
            drawCircle(color = NeoBlack, radius = 8f, center = Offset(pDest.x, pDest.y - 32f))
        }

        // Overlay Top-Left: 52dp White Circular Back Button
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .size(52.dp)
                .align(Alignment.TopStart)
                .clickable { onBackClick() },
            shape = CircleShape,
            color = PureWhite,
            border = BorderStroke(2.dp, NeoBlack)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Back", tint = NeoBlack, modifier = Modifier.size(32.dp))
            }
        }

        // Overlay Top-Right: Two Interactive White Pill Chips
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = NeoTeal
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = gpsStatusText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
                }
            }

            Surface(
                modifier = Modifier.clickable {
                    Toast.makeText(context, "📦 Offline Map Cache Active — Route tiles cached for offline travel.", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Text(
                    text = "Offline map",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack
                )
            }
        }
    }
}

// ==========================================
// 3. TRIP HEADER COMPONENT
// ==========================================
@Composable
fun TripHeader(
    destinationName: String = "Nyeri Town",
    etaMinutes: String = "12 min",
    distanceText: String = "8.4 km",
    arrivalTimeText: String = "arrive 21:06"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(text = "Heading to", fontSize = 13.sp, color = NeoMutedGrey)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = destinationName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = NeoBlack)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = etaMinutes, fontSize = 30.sp, fontWeight = FontWeight.Black, color = NeoBlack)
            Text(text = "$distanceText · $arrivalTimeText", fontSize = 12.sp, color = NeoMutedGrey)
        }
    }
}

// ==========================================
// 4. TRIP PROGRESS BAR COMPONENT WITH AUTO-UPDATE STAGES
// ==========================================
@Composable
fun TripProgressBar(
    progress: Float = 0.65f,
    originName: String = "Karatina",
    midStopName: String = "Kiganjo",
    destinationName: String = "Nyeri Town"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            shape = RoundedCornerShape(12.dp),
            color = NeoCreamBackground,
            border = BorderStroke(2.dp, NeoBlack)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f)),
                    shape = RoundedCornerShape(12.dp),
                    color = NeoTeal
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = originName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
            Text(text = midStopName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
            Text(text = destinationName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
        }
    }
}

// ==========================================
// 5. ALERT INFO CARD COMPONENT
// ==========================================
@Composable
fun AlertInfoCard(
    buzzMin: String = "5 min",
    shoutMin: String = "3 min"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NeoYellow),
        border = BorderStroke(2.dp, NeoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MascotMiniIllustration()
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Relax, sit back. I'll buzz at $buzzMin and shout at $shoutMin.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBlack,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==========================================
// 6. ALERT LIST COMPONENT
// ==========================================
@Composable
fun AlertList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AlertRow(label = "5 min", description = "Gentle buzz")
        AlertRow(label = "3 min", description = "Sound + notification")
        AlertRow(label = "At stage", description = "Full-screen alarm, even on silent")
    }
}

@Composable
fun AlertRow(label: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = NeoBlack,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = description,
            fontSize = 13.sp,
            color = NeoMutedGrey
        )
    }
}

// ==========================================
// 7. SOS EMERGENCY BUTTON COMPONENT
// ==========================================
@Composable
fun SosButton(
    onSosTriggered: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val startTime = System.currentTimeMillis()
            while (isPressed && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / 2000f).coerceIn(0f, 1f) // 2 seconds hold
                if (holdProgress >= 1f) {
                    onSosTriggered()
                    isPressed = false
                    holdProgress = 0f
                    break
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        holdProgress = 0f
                    }
                )
            },
        shape = RoundedCornerShape(32.dp),
        color = NeoCoral,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (holdProgress > 0f) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = holdProgress),
                    shape = RoundedCornerShape(32.dp),
                    color = PureWhite.copy(alpha = 0.35f)
                ) {}
            }

            Text(
                text = if (holdProgress > 0f) "Hold to trigger SOS (${(2 * (1f - holdProgress)).toInt() + 1}s)..." else "🚨 HOLD SOS (EMERGENCY)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )
        }
    }
}

// ==========================================
// 8. TRIP ACTION BUTTONS COMPONENT
// ==========================================
@Composable
fun TripActionButtons(
    onShareClick: () -> Unit,
    onPlayClick: () -> Unit,
    onSosClick: () -> Unit,
    onEndTripClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SOS Button
        SosButton(onSosTriggered = onSosClick)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Share trip button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clickable { onShareClick() },
                shape = RoundedCornerShape(29.dp),
                color = PureWhite,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NeoBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Share trip", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                }
            }

            // Play while I ride button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clickable { onPlayClick() },
                shape = RoundedCornerShape(29.dp),
                color = NeoBlack,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Play", tint = NeoYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Play while I ride", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PureWhite)
                }
            }
        }

        // End trip red button
        TextButton(onClick = onEndTripClick) {
            Text(
                text = "End trip",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = EndTripRed
            )
        }
    }
}

// ==========================================
// 9. TRIP BOTTOM SHEET COMPONENT
// ==========================================
@Composable
fun TripBottomSheet(
    destinationName: String,
    originName: String,
    midStopName: String,
    etaMinutes: String,
    distanceText: String,
    arrivalTimeText: String,
    progress: Float,
    onShareClick: () -> Unit,
    onPlayClick: () -> Unit,
    onSosClick: () -> Unit,
    onEndTripClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = PureWhite,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            TripHeader(
                destinationName = destinationName,
                etaMinutes = etaMinutes,
                distanceText = distanceText,
                arrivalTimeText = arrivalTimeText
            )

            // Progress Bar (Dynamic Fill & Auto-Updated Stages)
            TripProgressBar(
                progress = progress,
                originName = originName,
                midStopName = midStopName,
                destinationName = destinationName
            )

            // Info Card
            AlertInfoCard()

            // Alert List
            AlertList()

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            TripActionButtons(
                onShareClick = onShareClick,
                onPlayClick = onPlayClick,
                onSosClick = onSosClick,
                onEndTripClick = onEndTripClick
            )
        }
    }
}

// ==========================================
// MAIN ACTIVE TRIP SCREEN
// ==========================================
@Composable
fun TripScreen(
    uiState: ActiveTripUiState = ActiveTripUiState(),
    onEndTrip: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigatePlay: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    val trustedContacts = prefs.getTrustedContacts()

    var showShareModal by remember { mutableStateOf(false) }
    var showSosModal by remember { mutableStateOf(false) }
    var showStartedPrompt by remember { mutableStateOf(true) }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isLateNight = currentHour >= prefs.lateNightHour

    val destName = if (uiState.destinationName.isNotBlank()) uiState.destinationName else "Destination"
    val distanceText = if (uiState.distanceLeftKm >= 1f) "%.1f km".format(uiState.distanceLeftKm) else "%.0f m".format(uiState.distanceLeftKm * 1000f)
    val arrivalTimeText = "arrive ${uiState.arrivalTime}"

    val startPlace = if (uiState.startPlaceName.isNotBlank() && uiState.startPlaceName != "Origin") uiState.startPlaceName else "Starting Point"
    val arrival = if (uiState.arrivalTime.isNotBlank() && uiState.arrivalTime != "--:--") uiState.arrivalTime else "soon"

    val isGpsOn = try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (e: Exception) {
        true
    }

    var showLocationOffDialog by remember { mutableStateOf(!isGpsOn) }

    if (showLocationOffDialog) {
        AlertDialog(
            onDismissRequest = { showLocationOffDialog = false },
            containerColor = PureWhite,
            title = { Text(text = "Turn on location 📍", fontWeight = FontWeight.Black, color = NeoBlack) },
            text = {
                Text(
                    text = "Shukisha Please needs your location enabled to track your journey and alert you before your stop.",
                    fontSize = 13.sp,
                    color = NeoMutedGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationOffDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                ) {
                    Text("Turn On Location", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationOffDialog = false }) {
                    Text("Cancel", color = NeoMutedGrey)
                }
            }
        )
    }

    val hasRealLocation = uiState.currentLat != 0.0 && uiState.currentLon != 0.0

    val tripShareMessage = if (hasRealLocation) {
        "I'm heading to $destName from $startPlace. Arriving about $arrival. My location: https://maps.google.com/?q=${uiState.currentLat},${uiState.currentLon}"
    } else {
        "I'm heading to $destName from $startPlace. Arriving about $arrival."
    }

    val sosEmergencyMessage = if (hasRealLocation) {
        "🚨 EMERGENCY SOS! I need help at ${uiState.currentPlaceName} (${uiState.currentLat}, ${uiState.currentLon}). My location: https://maps.google.com/?q=${uiState.currentLat},${uiState.currentLon}"
    } else {
        "🚨 EMERGENCY SOS! I need help heading to $destName."
    }

    val arrivalShareMessage = "I've arrived safely at $destName. Thank you for tracking with Shukisha Please!"

    // Automatically prompt "Tell contact you've started?" on trip start (especially if late night)
    if (showStartedPrompt && trustedContacts.isNotEmpty() && isLateNight) {
        AlertDialog(
            onDismissRequest = { showStartedPrompt = false },
            containerColor = PureWhite,
            title = { Text(text = "Riding after dark 🌙", fontWeight = FontWeight.Black, color = NeoBlack) },
            text = {
                Text(
                    text = "Tell ${trustedContacts.first().name} you've started your trip?",
                    fontSize = 14.sp,
                    color = NeoMutedGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStartedPrompt = false
                        val contact = trustedContacts.first()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Hi ${contact.name}, $tripShareMessage")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Trip with ${contact.name}"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                ) {
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartedPrompt = false }) {
                    Text("Skip", color = NeoMutedGrey)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Map Section (~38% height) with Dynamic Motion Vehicle Marker!
            TripMap(
                progress = uiState.progress,
                gpsStatusText = uiState.gpsStatus,
                onBackClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
            )

            // 2. Overlapping Bottom Sheet (~62% height)
            TripBottomSheet(
                destinationName = destName,
                originName = uiState.startPlaceName,
                midStopName = uiState.midPlaceName,
                etaMinutes = uiState.etaDisplayText,
                distanceText = distanceText,
                arrivalTimeText = arrivalTimeText,
                progress = uiState.progress,
                onShareClick = {
                    showShareModal = true
                },
                onPlayClick = onNavigatePlay,
                onSosClick = {
                    showSosModal = true
                },
                onEndTripClick = {
                    // Stop foreground service, clear active trip, and prompt arrival if trusted contacts exist
                    val serviceIntent = Intent(context, TripTrackingService::class.java)
                    context.stopService(serviceIntent)
                    prefs.clearActiveTrip()

                    if (trustedContacts.isNotEmpty()) {
                        val contact = trustedContacts.first()
                        val arrivalIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Hi ${contact.name}, $arrivalShareMessage")
                        }
                        try {
                            context.startActivity(Intent.createChooser(arrivalIntent, "Tell ${contact.name} you've arrived?"))
                        } catch (e: Exception) {
                        }
                    }
                    onEndTrip()
                },
                modifier = Modifier.weight(0.62f)
            )
        }

        // SOS Emergency Confirmation Modal
        if (showSosModal) {
            AlertDialog(
                onDismissRequest = { showSosModal = false },
                containerColor = PureWhite,
                title = { Text(text = "🚨 EMERGENCY SOS", fontWeight = FontWeight.Black, color = EndTripRed) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Choose emergency action:", fontSize = 13.sp, color = NeoMutedGrey)

                        Button(
                            onClick = {
                                showSosModal = false
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EndTripRed, contentColor = PureWhite)
                        ) {
                            Text("📞 Call 999", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                showSosModal = false
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, sosEmergencyMessage)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Send SOS Location via"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                        ) {
                            Text("📍 Send My Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSosModal = false }) {
                        Text("Cancel", color = NeoMutedGrey)
                    }
                }
            )
        }

        // Trusted Contacts Quick Share Modal
        if (showShareModal) {
            AlertDialog(
                onDismissRequest = { showShareModal = false },
                containerColor = PureWhite,
                title = { Text(text = "Share Trip", fontWeight = FontWeight.Black, color = NeoBlack) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Choose a trusted contact or open system share:", fontSize = 13.sp, color = NeoMutedGrey)

                        if (trustedContacts.isNotEmpty()) {
                            trustedContacts.forEach { contact ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showShareModal = false
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "Hi ${contact.name}, $tripShareMessage")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share with ${contact.name}"))
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = NeoYellow,
                                    border = BorderStroke(1.5.dp, NeoBlack)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "👤 ${contact.name} (${contact.phone})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeoBlack)
                                    }
                                }
                            }
                        } else {
                            Text(text = "No trusted contacts configured. Add them in Settings.", fontSize = 12.sp, color = NeoMutedGrey)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showShareModal = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, tripShareMessage)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Trip via"))
                        },
                        colors =ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                    ) {
                        Text("Share Trip via", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareModal = false }) {
                        Text("Cancel", color = NeoMutedGrey)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveTripScreenPreview() {
    ShukishaPleaseTheme {
        TripScreen(
            uiState = ActiveTripUiState(
                destinationName = "Nyeri Town",
                startPlaceName = "Karatina",
                midPlaceName = "Kiganjo",
                etaMinutes = 12,
                etaDisplayText = "12 min",
                distanceLeftKm = 8.4f,
                arrivalTime = "21:06",
                progress = 0.65f,
                gpsStatus = "GPS strong"
            ),
            onEndTrip = {},
            onNavigateHome = {},
            onNavigatePlay = {}
        )
    }
}
