package com.jem.shukishaplease.features.play

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.MainActivity
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.home.BottomNavBarSection
import com.jem.shukishaplease.features.settings.SettingsActivity
import com.jem.shukishaplease.features.trip.TripsActivity
import com.jem.shukishaplease.features.trivia.TriviaActivity

class ShukishaPlayHubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShukishaPleaseTheme {
                ShukishaPlayHubScreen(
                    onNavigateHome = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onNavigateTrips = {
                        startActivity(Intent(this, TripsActivity::class.java))
                        finish()
                    },
                    onNavigateSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

// ==========================================
// FEATURED PURPLE WAVING MASCOT
// ==========================================
@Composable
fun PurpleWavingMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 5f)

        // Raised Waving Left Arm
        val leftArmPath = Path().apply {
            moveTo(w * 0.28f, h * 0.42f)
            lineTo(w * 0.12f, h * 0.22f)
        }
        drawPath(leftArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPink, radius = 12f, center = Offset(w * 0.12f, h * 0.22f))
        drawCircle(color = NeoBlack, radius = 12f, center = Offset(w * 0.12f, h * 0.22f), style = stroke)

        // Raised Right Arm
        val rightArmPath = Path().apply {
            moveTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.88f, h * 0.22f)
        }
        drawPath(rightArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPink, radius = 12f, center = Offset(w * 0.88f, h * 0.22f))
        drawCircle(color = NeoBlack, radius = 12f, center = Offset(w * 0.88f, h * 0.22f), style = stroke)

        // Purple Rounded Body
        val bodyRect = RoundRect(
            rect = Rect(left = w * 0.25f, top = h * 0.28f, right = w * 0.75f, bottom = h * 0.82f),
            cornerRadius = CornerRadius(28f, 28f)
        )
        val bodyPath = Path().apply { addRoundRect(bodyRect) }
        drawPath(bodyPath, color = NeoPurple)
        drawPath(bodyPath, color = NeoBlack, style = stroke)

        // Teal Cap with Brim on Right
        val capPath = Path().apply {
            moveTo(w * 0.23f, h * 0.28f)
            cubicTo(w * 0.23f, h * 0.08f, w * 0.77f, h * 0.08f, w * 0.77f, h * 0.28f)
            close()
        }
        drawPath(capPath, color = NeoTeal)
        drawPath(capPath, color = NeoBlack, style = stroke)

        val brimPath = Path().apply {
            moveTo(w * 0.60f, h * 0.28f)
            lineTo(w * 0.96f, h * 0.26f)
            lineTo(w * 0.90f, h * 0.32f)
            lineTo(w * 0.60f, h * 0.32f)
            close()
        }
        drawPath(brimPath, color = NeoTeal)
        drawPath(brimPath, color = NeoBlack, style = stroke)

        // Big White Eyes & Pupils
        val eyeLeft = Offset(w * 0.41f, h * 0.48f)
        val eyeRight = Offset(w * 0.59f, h * 0.48f)

        drawCircle(color = PureWhite, radius = 14f, center = eyeLeft)
        drawCircle(color = NeoBlack, radius = 14f, center = eyeLeft, style = stroke)
        drawCircle(color = NeoBlack, radius = 6f, center = Offset(w * 0.41f, h * 0.48f))

        drawCircle(color = PureWhite, radius = 14f, center = eyeRight)
        drawCircle(color = NeoBlack, radius = 14f, center = eyeRight, style = stroke)
        drawCircle(color = NeoBlack, radius = 6f, center = Offset(w * 0.59f, h * 0.48f))

        // Pink Cheeks
        drawCircle(color = NeoPink, radius = 8f, center = Offset(w * 0.32f, h * 0.58f))
        drawCircle(color = NeoPink, radius = 8f, center = Offset(w * 0.68f, h * 0.58f))

        // Smiling Mouth
        val mouthPath = Path().apply {
            moveTo(w * 0.42f, h * 0.64f)
            quadraticTo(w * 0.50f, h * 0.72f, w * 0.58f, h * 0.64f)
        }
        drawPath(mouthPath, color = NeoBlack, style = stroke)
    }
}

// ==========================================
// MAIN SHUKISHA PLAY HUB SCREEN
// ==========================================
@Composable
fun ShukishaPlayHubScreen(
    onNavigateHome: () -> Unit,
    onNavigateTrips: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    var selectedTab by remember { mutableStateOf(2) } // Play tab selected

    val isTripActive = prefs.isTripActive
    val destName = if (prefs.activeTripDestination.isNotBlank()) prefs.activeTripDestination else "your destination"
    val etaMin = prefs.activeTripEtaMinutes
    val etaFormatted = if (etaMin >= 60) {
        val hrs = etaMin / 60
        val mins = etaMin % 60
        if (mins > 0) "$hrs hr $mins min" else "$hrs hr"
    } else {
        "$etaMin min"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pick a game\nto ride with",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        lineHeight = 26.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = PureWhite,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(22.dp),
                                shape = CircleShape,
                                color = NeoYellow,
                                border = BorderStroke(2.dp, NeoBlack)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${prefs.totalPoints}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack
                            )
                        }
                    }
                }

                // 2. Active Trip Banner (Dynamically updates destination & live ETA)
                if (isTripActive) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = NeoBlack,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Clock", tint = NeoYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$etaFormatted to $destName · games pause at your stop",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                }

                // 3. Featured Card (Kenya Quiz)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoYellow),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PureWhite,
                                border = BorderStroke(2.dp, NeoBlack)
                            ) {
                                Text(
                                    text = if (isTripActive) "Fits this trip · ~$etaFormatted" else "Fits this trip · ~8 min",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoBlack
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Kenya\nQuiz",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(10.dp),
                                    shape = CircleShape,
                                    color = NeoTeal
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2,140 riding now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoBlack
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(context, TriviaActivity::class.java)
                                        context.startActivity(intent)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = NeoBlack,
                                border = BorderStroke(2.dp, NeoBlack)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Play now", fontSize = 13.sp, fontWeight = FontWeight.Black, color = PureWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = PureWhite, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Mascot Illustration on Right
                        PurpleWavingMascot(
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }

                // 4. 2x2 Grid Game Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Unscramble It (Purple)
                    GameGridCard(
                        title = "Unscramble It",
                        subtitle = "~3 min · +40 pts",
                        backgroundColor = NeoPurple,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(context, UnscrambleActivity::class.java)
                            context.startActivity(intent)
                        }
                    )

                    // Missing Letters (Sky Blue)
                    GameGridCard(
                        title = "Missing Letters",
                        subtitle = "~3 min · +40 pts",
                        backgroundColor = NeoSkyBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(context, MissingLettersActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // This or That (Pink)
                    GameGridCard(
                        title = "This or That",
                        subtitle = "~4 min · +60 pts",
                        backgroundColor = NeoPink,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(context, ThisOrThatActivity::class.java)
                            context.startActivity(intent)
                        }
                    )

                    // Thika Road League (White)
                    GameGridCard(
                        title = "Thika Road\nLeague",
                        subtitle = "You're #12 this week",
                        backgroundColor = PureWhite,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(context, TriviaActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 5. Bottom Navigation Bar
            BottomNavBarSection(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    when (index) {
                        0 -> onNavigateHome()
                        1 -> onNavigateTrips()
                        3 -> onNavigateSettings()
                    }
                }
            )
        }
    }
}

@Composable
fun GameGridCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, NeoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = NeoBlack,
                lineHeight = 18.sp
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBlack.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShukishaPlayHubPreview() {
    ShukishaPleaseTheme {
        ShukishaPlayHubScreen(
            onNavigateHome = {},
            onNavigateTrips = {},
            onNavigateSettings = {}
        )
    }
}
