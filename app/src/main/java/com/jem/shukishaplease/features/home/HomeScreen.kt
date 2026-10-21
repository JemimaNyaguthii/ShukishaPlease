package com.jem.shukishaplease.features.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.model.Destination
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.settings.SettingsActivity
import com.jem.shukishaplease.features.trip.TripsActivity

// ==========================================
// MASCOT VECTOR ILLUSTRATION
// ==========================================
@Composable
fun MascotIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 5f
        val stroke = Stroke(width = strokeWidth)

        // Arms (black lines with purple balls)
        val leftArmPath = Path().apply {
            moveTo(w * 0.22f, h * 0.45f)
            lineTo(w * 0.08f, h * 0.35f)
        }
        drawPath(leftArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPurple, radius = 10f, center = Offset(w * 0.08f, h * 0.35f))
        drawCircle(color = NeoBlack, radius = 10f, center = Offset(w * 0.08f, h * 0.35f), style = stroke)

        val rightArmPath = Path().apply {
            moveTo(w * 0.78f, h * 0.45f)
            lineTo(w * 0.92f, h * 0.35f)
        }
        drawPath(rightArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPurple, radius = 10f, center = Offset(w * 0.92f, h * 0.35f))
        drawCircle(color = NeoBlack, radius = 10f, center = Offset(w * 0.92f, h * 0.35f), style = stroke)

        // Main Yellow Body (Rounded Rectangle)
        val bodyRect = RoundRect(
            rect = Rect(
                left = w * 0.22f,
                top = h * 0.32f,
                right = w * 0.78f,
                bottom = h * 0.92f
            ),
            cornerRadius = CornerRadius(24f, 24f)
        )
        val bodyPath = Path().apply { addRoundRect(bodyRect) }
        drawPath(bodyPath, color = NeoYellow)
        drawPath(bodyPath, color = NeoBlack, style = stroke)

        // Coral Cap
        val capPath = Path().apply {
            moveTo(w * 0.2f, h * 0.32f)
            cubicTo(w * 0.2f, h * 0.12f, w * 0.8f, h * 0.12f, w * 0.8f, h * 0.32f)
            close()
        }
        drawPath(capPath, color = NeoCoral)
        drawPath(capPath, color = NeoBlack, style = stroke)

        // Cap Brim on right
        val brimPath = Path().apply {
            moveTo(w * 0.65f, h * 0.32f)
            lineTo(w * 0.94f, h * 0.30f)
            lineTo(w * 0.9f, h * 0.36f)
            lineTo(w * 0.65f, h * 0.36f)
            close()
        }
        drawPath(brimPath, color = NeoCoral)
        drawPath(brimPath, color = NeoBlack, style = stroke)

        // Big White Eyes with Black Pupils
        val leftEyeCenter = Offset(w * 0.39f, h * 0.52f)
        val rightEyeCenter = Offset(w * 0.61f, h * 0.52f)

        drawCircle(color = PureWhite, radius = 13f, center = leftEyeCenter)
        drawCircle(color = NeoBlack, radius = 13f, center = leftEyeCenter, style = stroke)
        drawCircle(color = NeoBlack, radius = 6f, center = Offset(w * 0.39f, h * 0.52f))

        drawCircle(color = PureWhite, radius = 13f, center = rightEyeCenter)
        drawCircle(color = NeoBlack, radius = 13f, center = rightEyeCenter, style = stroke)
        drawCircle(color = NeoBlack, radius = 6f, center = Offset(w * 0.61f, h * 0.52f))

        // Pink Cheeks
        drawCircle(color = NeoPink.copy(alpha = 0.8f), radius = 6f, center = Offset(w * 0.3f, h * 0.62f))
        drawCircle(color = NeoPink.copy(alpha = 0.8f), radius = 6f, center = Offset(w * 0.7f, h * 0.62f))

        // Small Smile
        val mouthPath = Path().apply {
            moveTo(w * 0.44f, h * 0.68f)
            quadraticTo(w * 0.5f, h * 0.74f, w * 0.56f, h * 0.68f)
        }
        drawPath(mouthPath, color = NeoBlack, style = stroke)
    }
}

// ==========================================
// REUSABLE BOTTOM NAV ITEM
// ==========================================
@Composable
fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeoYellow,
            border = BorderStroke(2.dp, NeoBlack)
        ) {
            Row(
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = NeoBlack, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeoBlack)
            }
        }
    } else {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = title, tint = PureWhite, modifier = Modifier.size(22.dp))
        }
    }
}

// ==========================================
// 1. TOP BAR SECTION
// ==========================================
@Composable
fun TopBarSection(
    userName: String = "Msafiri",
    coinAmount: String = "0"
) {
    val initialLetter = if (userName.isNotBlank()) userName.take(1).uppercase() else "M"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = NeoPurple,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = initialLetter, fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "Sasa,", fontSize = 12.sp, color = NeoMutedGrey)
                Text(text = userName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeoBlack)
            }
        }

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
                Text(text = coinAmount, fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoBlack)
            }
        }
    }
}

// ==========================================
// 2. HERO CARD SECTION
// ==========================================
@Composable
fun HeroCardSection(
    headline: String = "Tushukishe\nwapi leo?",
    bodyText: String = "Pick your stage. I'll shout before we get there."
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NeoTeal),
        border = BorderStroke(2.dp, NeoBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bodyText,
                    fontSize = 12.sp,
                    color = PureWhite.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
            MascotIllustration(modifier = Modifier.size(90.dp))
        }
    }
}

// ==========================================
// 3. SEARCH BAR SECTION
// ==========================================
@Composable
fun SearchBarSection(
    selectedDestination: Destination?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = PureWhite,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeoBlack, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedDestination?.name ?: "Search stage or landmark",
                fontSize = 15.sp,
                color = if (selectedDestination != null) NeoBlack else NeoMutedGrey,
                fontWeight = if (selectedDestination != null) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ==========================================
// 4. SAVED STAGES SECTION
// ==========================================
@Composable
fun SavedStagesSection(
    sampleDestinations: List<Destination>,
    onSelectSavedPlace: (Destination) -> Unit,
    onOpenPicker: () -> Unit
) {
    Column {
        Text(
            text = "Saved stages",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = NeoBlack
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Karatina: Yellow #FFD84D, home icon
            SavedStageTile(
                title = "Karatina",
                icon = Icons.Default.Home,
                backgroundColor = NeoYellow,
                modifier = Modifier.weight(1f)
            ) {
                val dest = sampleDestinations.find { it.name.contains("Karatina", ignoreCase = true) }
                    ?: Destination("Karatina Stage", -0.4849, 37.1264, "Nyeri-Karatina Rd")
                onSelectSavedPlace(dest)
            }

            // Office: Sky Blue #7CC8F8, briefcase icon
            SavedStageTile(
                title = "Office",
                icon = Icons.Default.Work,
                backgroundColor = NeoSkyBlue,
                modifier = Modifier.weight(1f)
            ) {
                val dest = Destination("Office Stage (CBD)", -1.286389, 36.817223, "Haile Selassie Ave")
                onSelectSavedPlace(dest)
            }

            // Market: Pink #FFBDF2, storefront icon
            SavedStageTile(
                title = "Market",
                icon = Icons.Default.Store,
                backgroundColor = NeoPink,
                modifier = Modifier.weight(1f)
            ) {
                val dest = Destination("City Market Stage", -1.2831, 36.8202, "Muindi Mbingu St")
                onSelectSavedPlace(dest)
            }

            // Add: Transparent, dashed black border, plus icon
            SavedStageTile(
                title = "Add",
                icon = Icons.Default.Add,
                backgroundColor = Color.Transparent,
                isDashed = true,
                modifier = Modifier.weight(1f)
            ) {
                onOpenPicker()
            }
        }
    }
}

@Composable
fun SavedStageTile(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    isDashed: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = NeoBlack, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeoBlack)
        }
    }
}

// ==========================================
// 5. SHOUT SELECTOR SECTION
// ==========================================
@Composable
fun ShoutSelectorSection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shout before my stage",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = NeoBlack
            )
            Text(
                text = "by live speed",
                fontSize = 12.sp,
                color = NeoMutedGrey
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            color = PureWhite,
            border = BorderStroke(2.dp, NeoBlack)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("3 min", "5 min", "1 km", "Custom").forEach { option ->
                    val isSelected = selectedOption == option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onOptionSelected(option) },
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSelected) NeoBlack else PureWhite
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PureWhite else NeoBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. PRIMARY START TRIP BUTTON SECTION
// ==========================================
@Composable
fun StartTripButtonSection(
    destinationName: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = NeoBlack,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(36.dp))
            Text(
                text = "Start trip",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = NeoYellow,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Start", tint = NeoBlack, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ==========================================
// 8. BOTTOM NAVIGATION BAR SECTION
// ==========================================
@Composable
fun BottomNavBarSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(32.dp),
        color = NeoBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(title = "Home", icon = Icons.Default.Home, isSelected = selectedTab == 0) {
                onTabSelected(0)
            }
            BottomNavItem(title = "Trips", icon = Icons.Default.DirectionsBus, isSelected = selectedTab == 1) {
                onTabSelected(1)
            }
            BottomNavItem(title = "Play", icon = Icons.Default.SportsEsports, isSelected = selectedTab == 2) {
                onTabSelected(2)
            }
            BottomNavItem(title = "Profile", icon = Icons.Default.Person, isSelected = selectedTab == 3) {
                onTabSelected(3)
            }
        }
    }
}

// ==========================================
// MAIN HOME SCREEN COMPOSABLE
// ==========================================
@Composable
fun ShukishaHome(
    uiState: HomeUiState = HomeUiState.Idle,
    selectedDestination: Destination? = null,
    sampleDestinations: List<Destination> = emptyList(),
    onGetLocationClick: () -> Unit = {},
    onOpenDestinationPicker: () -> Unit = {},
    onSelectSavedPlace: (Destination) -> Unit = {},
    onStartTripClick: () -> Unit = {},
    onPlayTriviaClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    var selectedShoutOption by remember { mutableStateOf("3 min") }
    var selectedTab by remember { mutableStateOf(0) }

    val currentUserName = prefs.userName
    val currentCoinAmount = "${prefs.totalPoints}"
    val destName = selectedDestination?.name ?: "Nyeri Town"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Scrollable Content Layer
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Bar Section
                TopBarSection(
                    userName = currentUserName,
                    coinAmount = currentCoinAmount
                )

                // 2. Hero Card Section
                HeroCardSection()

                // 3. Search Bar Section
                SearchBarSection(
                    selectedDestination = selectedDestination,
                    onClick = onOpenDestinationPicker
                )

                // 4. Saved Stages Section
                SavedStagesSection(
                    sampleDestinations = sampleDestinations,
                    onSelectSavedPlace = onSelectSavedPlace,
                    onOpenPicker = onOpenDestinationPicker
                )

                // 5. Shout Selector Section
                ShoutSelectorSection(
                    selectedOption = selectedShoutOption,
                    onOptionSelected = { selectedShoutOption = it }
                )

                // 6. Primary Start Trip Button Section
                StartTripButtonSection(
                    destinationName = destName,
                    onClick = onStartTripClick
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 8. Bottom Navigation Bar Section
            BottomNavBarSection(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    when (index) {
                        1 -> {
                            val intent = Intent(context, TripsActivity::class.java)
                            context.startActivity(intent)
                        }
                        2 -> onPlayTriviaClick()
                        3 -> {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}

// ==========================================
// UNSELECTED DESTINATION MAKANGA PROMPT
// ==========================================
@Composable
fun UnselectedDestinationDialog(
    onDismiss: () -> Unit,
    onPickStage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Hujachagua stage!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NeoBlack)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotIllustration(modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pick your destination stage first so I know where to shout before we get there!",
                    fontSize = 13.sp,
                    color = NeoMutedGrey,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPickStage,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
            ) {
                Text("Pick Stage Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = NeoMutedGrey)
            }
        }
    )
}

@Composable
fun DestinationPickerDialog(
    sampleDestinations: List<Destination>,
    initialQuery: String = "",
    onDismiss: () -> Unit,
    onSelectDestination: (Destination) -> Unit,
    onSearchDestination: (String, (Boolean) -> Unit) -> Unit
) {
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = { Text(text = "Where are you getting off?", fontWeight = FontWeight.Black, color = NeoBlack) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Type any place name (e.g. Nyeri, Karatina) or pick a popular stage:", fontSize = 13.sp, color = NeoMutedGrey)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchError = false
                    },
                    label = { Text("Destination Name (e.g. Nyeri)", color = NeoMutedGrey) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NeoBlack,
                        unfocusedTextColor = NeoBlack,
                        focusedBorderColor = NeoBlack,
                        unfocusedBorderColor = NeoBlack
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            isSearching = true
                            searchError = false
                            onSearchDestination(searchQuery) { success ->
                                isSearching = false
                                if (!success) {
                                    searchError = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite),
                    enabled = searchQuery.isNotBlank() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Search & Set Destination")
                    }
                }

                if (searchError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Could not find location. Try another place name.", color = NeoCoral, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Popular Commuter Stages:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.height(180.dp)) {
                    items(sampleDestinations) { dest ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectDestination(dest) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NeoCreamBackground),
                            border = BorderStroke(1.5.dp, NeoBlack)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = dest.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeoBlack)
                                Text(text = dest.address, fontSize = 12.sp, color = NeoMutedGrey)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeoMutedGrey)
            }
        }
    )
}
