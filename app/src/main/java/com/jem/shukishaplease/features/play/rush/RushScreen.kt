package com.jem.shukishaplease.features.play.rush

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.play.rush.ui.*
import kotlinx.coroutines.delay

@Composable
fun RushScreen(
    viewModel: RushViewModel,
    onClose: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val journeyState by viewModel.journeyState.collectAsState()

    var showApproachingOverlay by remember { mutableStateOf(false) }
    var userDismissedOverlay by remember { mutableStateOf(false) }

    // Synchronize Journey State periodically
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.syncJourneyState()
            delay(1000)
        }
    }

    // Rising edge check for Approaching Stop
    LaunchedEffect(journeyState.approachingStop) {
        if (journeyState.approachingStop && !userDismissedOverlay) {
            showApproachingOverlay = true
        }
    }

    // Frame Loop using withFrameNanos
    LaunchedEffect(gameState) {
        if (gameState == GameState.Playing) {
            var lastTimeNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (lastTimeNanos != 0L) {
                        val deltaNanos = frameNanos - lastTimeNanos
                        viewModel.updateFrame(deltaNanos)
                    }
                    lastTimeNanos = frameNanos
                }
            }
        }
    }

    when (gameState) {
        is GameState.Idle -> {
            RushStart(
                journeyState = journeyState,
                selectedCar = viewModel.engine.selectedCar,
                onSelectCar = { viewModel.engine.selectCar(it) },
                onStartEngine = { viewModel.startGame() },
                onClose = onClose
            )
        }

        is GameState.Playing -> {
            Box(modifier = Modifier.fillMaxSize()) {
                RoadCanvas(engineState = viewModel.engine.state)

                RushHud(
                    score = viewModel.engine.state.score,
                    coins = viewModel.engine.state.coinsCollected,
                    journeyState = journeyState,
                    onPauseClick = { viewModel.pauseGame(PauseReason.USER) },
                    onSteerLeft = { viewModel.engine.moveLeft() },
                    onSteerRight = { viewModel.engine.moveRight() }
                )

                if (showApproachingOverlay) {
                    ApproachingOverlay(
                        destinationName = journeyState.destinationName ?: "Stop",
                        minutesRemaining = journeyState.minutesRemaining ?: 2,
                        onDismiss = {
                            showApproachingOverlay = false
                            userDismissedOverlay = true
                        }
                    )
                }
            }
        }

        is GameState.Paused -> {
            val pauseReason = (gameState as GameState.Paused).reason
            Box(modifier = Modifier.fillMaxSize()) {
                RoadCanvas(engineState = viewModel.engine.state)

                if (pauseReason == PauseReason.ALARM) {
                    // Alarm Active Paused Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(3.dp, NeoBlack)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    color = NeoYellow,
                                    border = BorderStroke(2.dp, NeoBlack)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Alarm", tint = NeoBlack, modifier = Modifier.size(28.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "ARRIVED AT YOUR STOP! 🚨",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeoBlack
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Shukisha Rush is paused at ${viewModel.engine.state.score} pts. Your run is saved.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoMutedGrey,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { viewModel.endGame() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                                ) {
                                    Text(text = "View Trip Results", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                } else {
                    // User / Background Paused Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(3.dp, NeoBlack)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GAME PAUSED",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeoBlack
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Current Score: ${viewModel.engine.state.score} pts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoMutedGrey
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onClose,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(25.dp),
                                        border = BorderStroke(2.dp, NeoBlack)
                                    ) {
                                        Text(text = "Exit", fontWeight = FontWeight.Black, color = NeoBlack)
                                    }

                                    Button(
                                        onClick = { viewModel.resumeGame() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeoYellow, contentColor = NeoBlack),
                                        border = BorderStroke(2.dp, NeoBlack)
                                    ) {
                                        Text(text = "Resume", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        is GameState.Finished -> {
            RushResults(
                score = viewModel.engine.state.score,
                coins = viewModel.engine.state.coinsCollected,
                distanceMeters = viewModel.engine.state.distanceMeters,
                journeyState = journeyState,
                onPlayAgain = { viewModel.startGame() },
                onBackToGames = onClose
            )
        }
    }
}
