package com.jem.shukishaplease.features.play.rush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.jem.shukishaplease.core.ui.theme.ShukishaPleaseTheme

class RushActivity : ComponentActivity() {
    private val viewModel: RushViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShukishaPleaseTheme {
                RushScreen(
                    viewModel = viewModel,
                    onClose = { finish() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame(PauseReason.BACKGROUND)
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncJourneyState()
    }
}
