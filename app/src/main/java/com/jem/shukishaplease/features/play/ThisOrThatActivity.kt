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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.core.model.ThisOrThatItem
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.trivia.SegmentedProgressBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThisOrThatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = PlayRepository(applicationContext)
        val items = repo.getThisOrThatItems().shuffled()

        setContent {
            ShukishaPleaseTheme {
                ThisOrThatScreen(
                    itemList = items,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun ThisOrThatScreen(
    itemList: List<ThisOrThatItem>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { CommuterPrefs(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isLocked by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(42) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timerSeconds++
        }
    }

    val currentItem = itemList.getOrNull(currentIndex)
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
                totalSegments = itemList.size.coerceAtLeast(1)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (currentItem != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Category Chip (Pink)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeoPink,
                        border = BorderStroke(2.dp, NeoBlack)
                    ) {
                        Text(
                            text = "This or That · Choice ${currentIndex + 1} of ${itemList.size}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentItem.prompt,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options Container (Yellow Outer Card)
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
                            // Option A
                            val isSelA = selectedOption == "A"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isLocked) {
                                            selectedOption = "A"
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isLocked && isSelA) NeoTeal else PureWhite,
                                border = BorderStroke(if (isSelA) 3.dp else 2.dp, NeoBlack)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = PureWhite,
                                        border = BorderStroke(2.dp, NeoBlack)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("A", color = NeoBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = currentItem.optionA,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocked && isSelA) PureWhite else NeoBlack
                                    )
                                }
                            }

                            // Option B
                            val isSelB = selectedOption == "B"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isLocked) {
                                            selectedOption = "B"
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isLocked && isSelB) NeoTeal else PureWhite,
                                border = BorderStroke(if (isSelB) 3.dp else 2.dp, NeoBlack)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = PureWhite,
                                        border = BorderStroke(2.dp, NeoBlack)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("B", color = NeoBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = currentItem.optionB,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocked && isSelB) PureWhite else NeoBlack
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All choices completed!", fontWeight = FontWeight.Bold, color = NeoBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedOption != null && !isLocked) {
                        isLocked = true
                        prefs.addPoints(10)

                        coroutineScope.launch {
                            delay(1200)
                            if (currentIndex < itemList.size - 1) {
                                currentIndex += 1
                                selectedOption = null
                                isLocked = false
                            } else {
                                onClose()
                            }
                        }
                    }
                },
                enabled = selectedOption != null && !isLocked,
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
