package com.jem.shukishaplease.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*

enum class MakangaEmotion {
    HAPPY,      // Greeting / Games
    DRIVING,    // En route
    EXCITED,    // Near stop ("Tumefika karibu!")
    PANIC       // Arrived at stop ("Shukisha hapa!")
}

@Composable
fun MakangaMascot(
    emotion: MakangaEmotion = MakangaEmotion.HAPPY,
    speechText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Makanga Avatar Box
        Surface(
            shape = CircleShape,
            color = MatatuGold,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val mascotEmoji = when (emotion) {
                    MakangaEmotion.HAPPY -> "🧢👨🏾‍🦱"
                    MakangaEmotion.DRIVING -> "🚐👨🏾‍🦱"
                    MakangaEmotion.EXCITED -> "📣👨🏾‍🦱"
                    MakangaEmotion.PANIC -> "🚨👨🏾‍🦱"
                }
                Text(text = mascotEmoji, fontSize = 24.sp)
            }
        }

        if (speechText != null) {
            Spacer(modifier = Modifier.width(10.dp))

            // Speech Bubble
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
                color = PureWhite,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = speechText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatatuBrownText
                )
            }
        }
    }
}
