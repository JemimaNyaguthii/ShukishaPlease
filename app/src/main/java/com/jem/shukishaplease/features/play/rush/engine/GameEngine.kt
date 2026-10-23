package com.jem.shukishaplease.features.play.rush.engine

import kotlin.math.abs

enum class CarType(val id: String, val displayName: String, val colorHex: Long) {
    PURPLE("purple", "Matatu Express", 0xFFA594FF),
    TEAL("teal", "Teal Cruiser", 0xFF008C6E),
    BLUE("blue", "Sky Dasher", 0xFF7CC8F8)
}

enum class ObstacleType {
    MATATU, CONE
}

data class TrafficObstacle(
    val id: Long,
    val lane: Int, // 0, 1, 2
    val yFraction: Float, // 0.0 (top) to 1.2 (bottom)
    val type: ObstacleType
)

data class CoinItem(
    val id: Long,
    val lane: Int,
    val yFraction: Float
)

data class GameEngineState(
    val lane: Int = 1, // 0 = Left, 1 = Center, 2 = Right
    val laneOffsetX: Float = 0f, // Interpolated offset (-1f to 1f)
    val selectedCar: CarType = CarType.PURPLE,
    val score: Int = 0,
    val coinsCollected: Int = 0,
    val distanceMeters: Float = 0f,
    val speed: Float = 0.35f,
    val isGameOver: Boolean = false,
    val obstacles: List<TrafficObstacle> = emptyList(),
    val coins: List<CoinItem> = emptyList()
)

class GameEngine(
    var selectedCar: CarType = CarType.PURPLE
) {
    var state = GameEngineState(selectedCar = selectedCar)
        private set

    private var nextEntityId = 1L
    private var spawnTimer = 0f
    private var coinSpawnTimer = 0f

    fun selectCar(car: CarType) {
        selectedCar = car
        state = state.copy(selectedCar = car)
    }

    fun moveLeft() {
        if (state.isGameOver) return
        if (state.lane > 0) {
            state = state.copy(lane = state.lane - 1)
        }
    }

    fun moveRight() {
        if (state.isGameOver) return
        if (state.lane < 2) {
            state = state.copy(lane = state.lane + 1)
        }
    }

    fun reset() {
        nextEntityId = 1L
        spawnTimer = 0f
        coinSpawnTimer = 0f
        state = GameEngineState(selectedCar = selectedCar)
    }

    fun update(deltaSeconds: Float) {
        if (state.isGameOver) return

        // 1. Interpolate Lane Offset X towards Target Lane
        val targetOffset = (state.lane - 1).toFloat()
        val currentOffset = state.laneOffsetX
        val newOffset = currentOffset + (targetOffset - currentOffset) * (deltaSeconds * 12f).coerceAtMost(1f)

        // 2. Speed & Distance Calculation
        val newSpeed = (0.35f + (state.distanceMeters / 1000f) * 0.15f).coerceAtMost(0.85f)
        val newDistance = state.distanceMeters + newSpeed * 80f * deltaSeconds

        // 3. Update Obstacles
        val updatedObstacles = state.obstacles.map { obs ->
            obs.copy(yFraction = obs.yFraction + newSpeed * deltaSeconds)
        }.filter { it.yFraction < 1.2f }.toMutableList()

        // 4. Update Coins & Collection
        var coinsGathered = state.coinsCollected
        val updatedCoins = state.coins.map { coin ->
            coin.copy(yFraction = coin.yFraction + newSpeed * deltaSeconds)
        }.filter { coin ->
            val isPlayerLane = abs(newOffset - (coin.lane - 1)) < 0.4f
            val isPlayerY = abs(coin.yFraction - 0.8f) < 0.08f
            if (isPlayerLane && isPlayerY) {
                coinsGathered++
                false
            } else {
                coin.yFraction < 1.2f
            }
        }.toMutableList()

        val newScore = newDistance.toInt() + (coinsGathered * 10)

        // 5. Spawn Traffic (Guarantees at least 1 free lane)
        spawnTimer += deltaSeconds
        if (spawnTimer > (1.8f / newSpeed).coerceAtLeast(0.8f)) {
            spawnTimer = 0f
            val freeLane = (0..2).random()
            val occupiedLanes = (0..2).filter { it != freeLane }.shuffled()
            val count = if (Math.random() < 0.6) 1 else 2

            for (i in 0 until count) {
                val lane = occupiedLanes[i]
                val type = if (Math.random() < 0.5) ObstacleType.MATATU else ObstacleType.CONE
                updatedObstacles.add(TrafficObstacle(nextEntityId++, lane, -0.2f, type))
            }
        }

        // 6. Spawn Coins
        coinSpawnTimer += deltaSeconds
        if (coinSpawnTimer > 1.2f) {
            coinSpawnTimer = 0f
            val lane = (0..2).random()
            if (updatedObstacles.none { it.lane == lane && it.yFraction < 0.2f }) {
                updatedCoins.add(CoinItem(nextEntityId++, lane, -0.2f))
            }
        }

        // 7. Check Collision with Obstacles
        var gameOver = false
        for (obs in updatedObstacles) {
            val isSameLane = abs(newOffset - (obs.lane - 1)) < 0.45f
            val isSameY = abs(obs.yFraction - 0.8f) < 0.09f
            if (isSameLane && isSameY) {
                gameOver = true
                break
            }
        }

        state = state.copy(
            laneOffsetX = newOffset,
            distanceMeters = newDistance,
            score = newScore,
            speed = newSpeed,
            coinsCollected = coinsGathered,
            obstacles = updatedObstacles,
            coins = updatedCoins,
            isGameOver = gameOver
        )
    }
}
