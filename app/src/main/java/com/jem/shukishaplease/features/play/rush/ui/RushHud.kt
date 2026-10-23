package com.jem.shukishaplease.features.play.rush.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.play.rush.JourneySnapshot

@Composable
fun RushHud(
    score: Int,
    coins: Int,
    journeyState: JourneySnapshot,
    onPauseClick: () -> Unit,
    onSteerLeft: () -> Unit,
    onSteerRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onPauseClick() },
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = NeoBlack)
                    }
                }

                // Score Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NeoYellow,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(
                        text = "$score pts",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                }

                // Coins Pill
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
                            modifier = Modifier.size(16.dp),
                            shape = CircleShape,
                            color = NeoYellow,
                            border = BorderStroke(1.5.dp, NeoBlack)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = NeoBlack
                        )
                    }
                }
            }

            // Trip Bar
            val tripText = when {
                journeyState.tripActive && journeyState.minutesRemaining != null -> {
                    "${journeyState.minutesRemaining} min to ${journeyState.destinationName ?: "destination"} · games pause at your stop"
                }
                journeyState.tripActive -> {
                    "Waiting for GPS to ${journeyState.destinationName ?: "destination"}"
                }
                else -> {
                    "No active trip · Drive & collect coins"
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = if (journeyState.approachingStop) NeoAmberWarning else NeoBlack,
                border = BorderStroke(2.dp, NeoBlack)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Clock", tint = if (journeyState.approachingStop) NeoBlack else NeoYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tripText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (journeyState.approachingStop) NeoBlack else PureWhite
                    )
                }
            }
        }

        // Bottom Steer Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Button
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { onSteerLeft() },
                shape = CircleShape,
                color = PureWhite,
                border = BorderStroke(3.dp, NeoBlack)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Steer Left", tint = NeoBlack, modifier = Modifier.size(32.dp))
                }
            }

            // Right Button
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { onSteerRight() },
                shape = CircleShape,
                color = NeoYellow,
                border = BorderStroke(3.dp, NeoBlack)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Steer Right", tint = NeoBlack, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
