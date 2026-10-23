package com.jem.shukishaplease.features.play.rush

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jem.shukishaplease.core.common.CommuterPrefs
import com.jem.shukishaplease.features.play.rush.engine.GameEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PauseReason {
    USER, BACKGROUND, ALARM
}

sealed class GameState {
    object Idle : GameState()
    object Playing : GameState()
    data class Paused(val reason: PauseReason) : GameState()
    object Finished : GameState()
}

data class JourneySnapshot(
    val tripActive: Boolean = false,
    val destinationName: String? = null,
    val minutesRemaining: Int? = null,
    val approachingStop: Boolean = false,
    val alarmFired: Boolean = false
)

class RushViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = CommuterPrefs(application)
    val engine = GameEngine()

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _journeyState = MutableStateFlow(JourneySnapshot())
    val journeyState: StateFlow<JourneySnapshot> = _journeyState.asStateFlow()

    init {
        syncJourneyState()
    }

    fun syncJourneyState() {
        val active = prefs.isTripActive
        val dest = if (prefs.activeTripDestination.isNotBlank()) prefs.activeTripDestination else null
        val storedEta = prefs.activeTripEtaMinutes
        val eta = if (active && storedEta > 0) storedEta else null
        val approaching = active && eta != null && eta <= 3
        val alarm = prefs.alarmFired

        _journeyState.value = JourneySnapshot(
            tripActive = active,
            destinationName = dest,
            minutesRemaining = eta,
            approachingStop = approaching,
            alarmFired = alarm
        )

        if (alarm && _gameState.value == GameState.Playing) {
            _gameState.value = GameState.Paused(PauseReason.ALARM)
        }
    }

    fun startGame() {
        engine.reset()
        _gameState.value = GameState.Playing
    }

    fun pauseGame(reason: PauseReason) {
        if (_gameState.value == GameState.Playing) {
            _gameState.value = GameState.Paused(reason)
        }
    }

    fun resumeGame() {
        if (_gameState.value is GameState.Paused && !prefs.alarmFired) {
            _gameState.value = GameState.Playing
        }
    }

    fun endGame() {
        if (_gameState.value != GameState.Finished) {
            _gameState.value = GameState.Finished
            prefs.addPoints(engine.state.coinsCollected * 10)
        }
    }

    fun updateFrame(deltaNanos: Long) {
        if (_gameState.value == GameState.Playing) {
            val deltaSeconds = deltaNanos / 1_000_000_000f
            engine.update(deltaSeconds)
            if (engine.state.isGameOver) {
                endGame()
            }
        }
    }
}
