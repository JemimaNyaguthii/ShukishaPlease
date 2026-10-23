package com.jem.shukishaplease.features.play.rush.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*

@Composable
fun ApproachingOverlay(
    destinationName: String,
    minutesRemaining: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = NeoAmberWarning),
            border = BorderStroke(3.dp, NeoBlack),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = NeoBlack
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Bell", tint = NeoYellow, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    border = BorderStroke(1.5.dp, NeoBlack)
                ) {
                    Text(
                        text = "${destinationName.uppercase()} · $minutesRemaining MIN",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your stop is coming up!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Get ready to alight.",
                    fontSize = 13.sp,
                    color = NeoBlack.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoBlack, contentColor = PureWhite)
                ) {
                    Text(text = "Got it, keep playing", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}
