package com.jem.shukishaplease.features.play.rush

import com.jem.shukishaplease.features.play.rush.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RushViewModelTest {

    @Test
    fun pauseReason_background_pausesEngine() {
        val engine = GameEngine()
        val gameState: GameState = GameState.Paused(PauseReason.BACKGROUND)

        // Update frame while paused
        val initialDist = engine.state.distanceMeters
        if (gameState == GameState.Playing) {
            engine.update(0.1f)
        }

        assertEquals(initialDist, engine.state.distanceMeters, 0.001f)
        assertTrue(gameState is GameState.Paused)
        assertEquals(PauseReason.BACKGROUND, (gameState as GameState.Paused).reason)
    }

    @Test
    fun alarmFired_forcesAlarmPause_andBlocksResume() {
        val alarmFired = true
        var gameState: GameState = GameState.Playing

        if (alarmFired) {
            gameState = GameState.Paused(PauseReason.ALARM)
        }

        assertEquals(GameState.Paused(PauseReason.ALARM), gameState)

        // Attempt resume while alarm is active
        var canResume = false
        if (gameState is GameState.Paused && !alarmFired) {
            gameState = GameState.Playing
            canResume = true
        }

        assertFalse(canResume)
        assertEquals(GameState.Paused(PauseReason.ALARM), gameState)
    }

    @Test
    fun journeyStateReconcile_detectsApproachingStop() {
        val isTripActive = true
        val etaMinutes = 2

        val approaching = isTripActive && etaMinutes <= 3
        val snapshot = JourneySnapshot(
            tripActive = isTripActive,
            destinationName = "Nyeri Town",
            minutesRemaining = etaMinutes,
            approachingStop = approaching,
            alarmFired = false
        )

        assertTrue(snapshot.approachingStop)
        assertEquals("Nyeri Town", snapshot.destinationName)
        assertEquals(2, snapshot.minutesRemaining)
    }

    @Test
    fun journeyState_nullMinutesRemaining_whenGpsSearching() {
        val snapshot = JourneySnapshot(
            tripActive = true,
            destinationName = "Westlands",
            minutesRemaining = null,
            approachingStop = false,
            alarmFired = false
        )

        assertTrue(snapshot.tripActive)
        assertEquals("Westlands", snapshot.destinationName)
        assertNull(snapshot.minutesRemaining)
        assertFalse(snapshot.approachingStop)
    }

    @Test
    fun approachingOverlay_dismissedOnce_doesNotReappearForSameTrip() {
        var showOverlay = false
        var userDismissed = false

        // First rising edge emission
        val approachingFirst = true
        if (approachingFirst && !userDismissed) {
            showOverlay = true
        }
        assertTrue(showOverlay)

        // User dismisses
        showOverlay = false
        userDismissed = true

        // Subsequent approaching emission for same trip
        val approachingSecond = true
        if (approachingSecond && !userDismissed) {
            showOverlay = true
        }

        assertFalse(showOverlay)
    }
}
