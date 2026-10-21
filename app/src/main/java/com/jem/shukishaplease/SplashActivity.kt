package com.jem.shukishaplease

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.home.MascotIllustration
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShukishaPleaseTheme {
                LaunchedEffect(Unit) {
                    delay(1500)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NeoCreamBackground
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            MascotIllustration(modifier = Modifier.size(140.dp))
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Shukisha Please",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Smart Commuter Stop Alerts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeoMutedGrey
                            )
                        }
                    }
                }
            }
        }
    }
}
