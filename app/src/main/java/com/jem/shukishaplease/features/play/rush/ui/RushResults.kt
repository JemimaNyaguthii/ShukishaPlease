package com.jem.shukishaplease.features.play.rush.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
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
fun RushResults(
    score: Int,
    coins: Int,
    distanceMeters: Float,
    journeyState: JourneySnapshot,
    onPlayAgain: () -> Unit,
    onBackToGames: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (journeyState.tripActive && journeyState.destinationName != null) "Welcome to ${journeyState.destinationName}" else "Nice ride, driver!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Score Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoYellow),
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = NeoBlack, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$score pts", fontSize = 36.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite,
                            border = BorderStroke(1.5.dp, NeoBlack)
                        ) {
                            Text(
                                text = "Personal Best",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeoBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2x2 Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Coins Earned
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PureWhite,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Coins Earned", fontSize = 11.sp, color = NeoMutedGrey, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "+$coins", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        }
                    }

                    // Distance Travelled
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PureWhite,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Distance", fontSize = 11.sp, color = NeoMutedGrey, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "%.1f km".format(distanceMeters / 1000f), fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoYellow, contentColor = NeoBlack),
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(text = "Play Again Next Trip 🚀", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = onBackToGames,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(text = "Back to Games", fontWeight = FontWeight.Black, fontSize = 15.sp, color = NeoBlack)
                }
            }
        }
    }
}
