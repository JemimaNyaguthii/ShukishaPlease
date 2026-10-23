package com.jem.shukishaplease.features.play.rush.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.play.rush.JourneySnapshot
import com.jem.shukishaplease.features.play.rush.engine.CarType

@Composable
fun RushStart(
    journeyState: JourneySnapshot,
    selectedCar: CarType,
    onSelectCar: (CarType) -> Unit,
    onStartEngine: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onClose() },
                        shape = CircleShape,
                        color = PureWhite,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = NeoBlack)
                        }
                    }

                    Text(text = "SHUKISHA RUSH", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeoBlack)

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trip Bar
                val tripText = if (journeyState.tripActive && journeyState.minutesRemaining != null) {
                    "${journeyState.minutesRemaining} min to ${journeyState.destinationName ?: "destination"} · games pause at your stop"
                } else if (journeyState.tripActive) {
                    "Waiting for GPS to ${journeyState.destinationName ?: "destination"}"
                } else {
                    "No active trip · Drive & collect coins"
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = NeoBlack,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Clock", tint = NeoYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = tripText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Purple Card with Road Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoPurple),
                    border = BorderStroke(2.dp, NeoBlack),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏎️ 2D TOP-DOWN CAR RUSH", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PureWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Dodge traffic matatus, collect coins & race your commute!", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Car Selection Section
                Text(text = "CHOOSE YOUR RIDE", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarType.values().forEach { car ->
                        val isSel = selectedCar == car
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clickable { onSelectCar(car) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) NeoYellow else PureWhite,
                            border = BorderStroke(if (isSel) 3.dp else 2.dp, NeoBlack)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = car.displayName, fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartEngine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoYellow, contentColor = NeoBlack),
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(text = "Start Engine 🚀", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap left or right to switch lanes · works offline",
                    fontSize = 11.sp,
                    color = NeoMutedGrey,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
