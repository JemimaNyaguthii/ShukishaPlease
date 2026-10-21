package com.jem.shukishaplease.features.play

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.model.WordItem
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.trivia.SegmentedProgressBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UnscrambleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = PlayRepository(applicationContext)
        val wordList = repo.getWordItems().shuffled()

        setContent {
            ShukishaPleaseTheme {
                UnscrambleScreen(
                    wordList = wordList,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun UnscrambleScreen(
    wordList: List<WordItem>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentIndex by remember { mutableStateOf(0) }
    var userGuess by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(42) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timerSeconds++
        }
    }

    val currentItem = wordList.getOrNull(currentIndex)
    val scrambledWord = remember(currentItem) {
        currentItem?.word?.toList()?.shuffled()?.joinToString(" ") ?: ""
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
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onClose() },
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = NeoBlack, modifier = Modifier.size(24.dp))
                    }
                }

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
                currentIndex = currentIndex,
                totalSegments = wordList.size.coerceAtLeast(1)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (currentItem != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Category Chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeoPurple,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Text(
                            text = "Unscramble It · ${currentItem.category} (${currentIndex + 1}/${wordList.size})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Rearrange the letters to reveal the Kenya word:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoMutedGrey
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Yellow Card with Scrambled Word
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
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = scrambledWord,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack,
                                letterSpacing = 4.sp,
                                textAlign = TextAlign.Center
                            )

                            if (showHint) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PureWhite,
                                    border = BorderStroke(1.5.dp, NeoBlack)
                                ) {
                                    Text(
                                        text = "Hint: ${currentItem.hint}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeoBlack
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(onClick = { showHint = true }) {
                                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Hint", tint = NeoBlack)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Show Hint", color = NeoBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userGuess,
                        onValueChange = { userGuess = it.uppercase() },
                        label = { Text("Your Answer", color = NeoMutedGrey) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NeoBlack,
                            unfocusedTextColor = NeoBlack,
                            focusedBorderColor = NeoBlack,
                            unfocusedBorderColor = NeoBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedbackMessage!!,
                            color = if (isCorrect) NeoTeal else NeoCoral,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All words completed!", fontWeight = FontWeight.Bold, color = NeoBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentItem != null && !isCorrect) {
                        if (userGuess.trim().equals(currentItem.word, ignoreCase = true)) {
                            feedbackMessage = "Correct! (+10 Points)"
                            isCorrect = true
                            prefs.addPoints(10)
                        } else {
                            feedbackMessage = "Incorrect! It was ${currentItem.word}"
                            isCorrect = false
                        }

                        coroutineScope.launch {
                            delay(1200)
                            if (currentIndex < wordList.size - 1) {
                                currentIndex += 1
                                userGuess = ""
                                showHint = false
                                feedbackMessage = null
                                isCorrect = false
                            } else {
                                feedbackMessage = "All words completed! +10 Points earned."
                            }
                        }
                    }
                },
                enabled = userGuess.isNotBlank() && !isCorrect,
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
                Text("Lock it in", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
