package com.jem.shukishaplease.features.trivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TriviaActivity : ComponentActivity() {
    private val viewModel: TriviaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadQuestions(applicationContext)
        setContent {
            ShukishaPleaseTheme {
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                TriviaScreen(
                    state = state,
                    onAnswerSelected = viewModel::selectAnswer,
                    onSubmitAnswer = viewModel::submitAnswer,
                    onNextQuestion = viewModel::nextQuestion,
                    onRestart = { viewModel.loadQuestions(context) },
                    onClose = { finish() }
                )
            }
        }
    }
}

// ==========================================
// 10-SEGMENT PROGRESS BAR COMPONENT
// ==========================================
@Composable
fun SegmentedProgressBar(
    currentIndex: Int,
    totalSegments: Int = 10
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until totalSegments) {
            val segmentColor = when {
                i < currentIndex -> NeoTeal      // Answered
                i == currentIndex -> NeoBlack     // Current
                else -> Color(0xFFD0D0D0)        // Upcoming
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(4.dp),
                color = segmentColor
            ) {}
        }
    }
}

// ==========================================
// MAIN QUIZ SCREEN COMPOSABLE
// ==========================================
@Composable
fun TriviaScreen(
    state: TriviaUiState,
    onAnswerSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    val coroutineScope = rememberCoroutineScope()
    var showQuitConfirm by remember { mutableStateOf(false) }

    // Timer seconds counter
    var timerSeconds by remember { mutableStateOf(42) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timerSeconds++
        }
    }

    val timerFormatted = "⏱ %02d:%02d".format(timerSeconds / 60, timerSeconds % 60)
    val stopEtaText = if (prefs.isTripActive && prefs.activeTripDestination.isNotBlank()) "Stop in 9 min" else "Stop in 9 min"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular ✕ Back Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { showQuitConfirm = true },
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Quit", tint = NeoBlack, modifier = Modifier.size(24.dp))
                    }
                }

                // Black "Stop in 9 min" Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NeoBlack,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(
                        text = stopEtaText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoYellow
                    )
                }

                // Outlined Timer Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Text(
                        text = timerFormatted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 10-Segment Progress Bar
            SegmentedProgressBar(
                currentIndex = if (state.questions.isNotEmpty()) state.currentIndex else 2,
                totalSegments = 10
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeoBlack)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Preparing Kenya Quiz...", color = NeoMutedGrey, fontSize = 14.sp)
                    }
                }
            } else if (state.isFinished) {
                // Result Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = NeoYellow,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Quiz Finished! 🎉", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeoBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "You scored ${state.score} out of ${state.questions.size}", fontSize = 16.sp, color = NeoMutedGrey)
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                        ) {
                            Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } else if (state.questions.isNotEmpty()) {
                val q = state.questions[state.currentIndex.coerceIn(0, state.questions.size - 1)]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // 3. Category Chip (Sky Blue)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeoSkyBlue,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Text(
                            text = "${q.category} · Q${state.currentIndex + 1} of ${state.questions.size}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Question Text
                    Text(
                        text = q.questionText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Options Container (Yellow Outer Card)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = NeoYellow),
                        border = BorderStroke(2.dp, NeoBlack),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            q.options.forEachIndexed { index, option ->
                                val isSelected = state.selectedAnswerIndex == index
                                val isCorrectOption = index == q.correctAnswerIndex

                                val rowBorder = when {
                                    state.isAnswerSubmitted && isCorrectOption -> BorderStroke(3.dp, NeoTeal)
                                    state.isAnswerSubmitted && isSelected && !isCorrectOption -> BorderStroke(3.dp, NeoCoral)
                                    isSelected -> BorderStroke(3.dp, NeoBlack)
                                    else -> BorderStroke(2.dp, NeoBlack)
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAnswerSelected(index) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = PureWhite,
                                    border = rowBorder
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape,
                                            color = PureWhite,
                                            border = BorderStroke(2.dp, NeoBlack)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = ('A' + index).toString(),
                                                    color = NeoBlack,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Text(
                                            text = option,
                                            fontSize = 15.sp,
                                            color = NeoBlack,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Bottom "Lock it in" Button (Auto-advances after 1s on answer submission)
            val isEnabled = state.selectedAnswerIndex != null
            Button(
                onClick = {
                    if (!state.isAnswerSubmitted) {
                        onSubmitAnswer()
                        coroutineScope.launch {
                            delay(1200)
                            onNextQuestion()
                        }
                    } else {
                        onNextQuestion()
                    }
                },
                enabled = isEnabled || state.isAnswerSubmitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeoBlack,
                    contentColor = PureWhite,
                    disabledContainerColor = Color(0xFFDCD8CD),
                    disabledContentColor = Color(0xFF808080)
                )
            ) {
                Text(
                    text = if (!state.isAnswerSubmitted) "Lock it in" else "Next Question →",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // Quit Game Confirmation Dialog
        if (showQuitConfirm) {
            AlertDialog(
                onDismissRequest = { showQuitConfirm = false },
                containerColor = PureWhite,
                title = { Text(text = "Quit game?", fontWeight = FontWeight.Black, color = NeoBlack) },
                text = {
                    Text(
                        text = "Are you sure you want to exit? Your game points won't be saved.",
                        fontSize = 13.sp,
                        color = NeoMutedGrey
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showQuitConfirm = false
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                    ) {
                        Text("Quit Game", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuitConfirm = false }) {
                        Text("Keep Playing", color = NeoMutedGrey)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TriviaScreenPreview() {
    ShukishaPleaseTheme {
        TriviaScreen(
            state = TriviaUiState(),
            onAnswerSelected = {},
            onSubmitAnswer = {},
            onNextQuestion = {},
            onRestart = {},
            onClose = {}
        )
    }
}
