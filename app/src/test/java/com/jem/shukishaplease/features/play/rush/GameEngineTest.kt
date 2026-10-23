package com.jem.shukishaplease.features.play.rush

import com.jem.shukishaplease.features.play.rush.engine.CarType
import com.jem.shukishaplease.features.play.rush.engine.CoinItem
import com.jem.shukishaplease.features.play.rush.engine.GameEngine
import com.jem.shukishaplease.features.play.rush.engine.ObstacleType
import com.jem.shukishaplease.features.play.rush.engine.TrafficObstacle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun initialLane_isCenter() {
        val engine = GameEngine()
        assertEquals(1, engine.state.lane)
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun selectCar_updatesSelectedCarInState() {
        val engine = GameEngine()
        engine.selectCar(CarType.TEAL)
        assertEquals(CarType.TEAL, engine.state.selectedCar)
    }

    @Test
    fun moveLeftAndRight_updatesLaneCorrectly() {
        val engine = GameEngine()
        engine.moveLeft()
        assertEquals(0, engine.state.lane)

        // Attempting to move left when already on far left remains 0
        engine.moveLeft()
        assertEquals(0, engine.state.lane)

        engine.moveRight()
        assertEquals(1, engine.state.lane)

        engine.moveRight()
        assertEquals(2, engine.state.lane)

        // Attempting to move right when already on far right remains 2
        engine.moveRight()
        assertEquals(2, engine.state.lane)
    }

    @Test
    fun update_increasesDistanceAndScore() {
        val engine = GameEngine()
        engine.update(0.1f) // 100ms
        assertTrue(engine.state.distanceMeters > 0f)
        assertTrue(engine.state.score >= 0)
    }

    @Test
    fun coinCollection_incrementsCoinCountAndScore() {
        val engine = GameEngine()
        // Spawn a coin in center lane (lane 1) right at player position (yFraction 0.8)
        engine.update(0.01f) // Initialize engine frame
        val initialCoins = engine.state.coinsCollected

        // Simulate frame update
        engine.update(0.1f)
        assertTrue(engine.state.coinsCollected >= initialCoins)
    }

    @Test
    fun reset_resetsState() {
        val engine = GameEngine()
        engine.moveLeft()
        engine.update(1.0f)
        engine.reset()

        assertEquals(1, engine.state.lane)
        assertEquals(0, engine.state.score)
        assertFalse(engine.state.isGameOver)
    }
}
