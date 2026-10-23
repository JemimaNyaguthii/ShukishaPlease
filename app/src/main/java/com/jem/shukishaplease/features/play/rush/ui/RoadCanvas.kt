package com.jem.shukishaplease.features.play.rush.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.jem.shukishaplease.core.ui.theme.*
import com.jem.shukishaplease.features.play.rush.engine.GameEngineState
import com.jem.shukishaplease.features.play.rush.engine.ObstacleType

@Composable
fun RoadCanvas(
    engineState: GameEngineState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Purple Verge (Sides)
        drawRect(color = NeoPurple)

        // 2. Central Ink Road
        val roadWidth = w * 0.72f
        val roadLeft = w * 0.14f
        val roadRight = w * 0.86f
        drawRect(color = NeoBlack, topLeft = Offset(roadLeft, 0f), size = Size(roadWidth, h))

        // 3. Road Edges (Yellow Lines)
        val stroke3 = Stroke(width = 6f)
        drawLine(color = NeoYellow, start = Offset(roadLeft, 0f), end = Offset(roadLeft, h), strokeWidth = 8f)
        drawLine(color = NeoYellow, start = Offset(roadRight, 0f), end = Offset(roadRight, h), strokeWidth = 8f)

        // 4. Cream Dashed Lane Markers (Dividing 3 Lanes)
        val laneWidth = roadWidth / 3f
        val lane1X = roadLeft + laneWidth
        val lane2X = roadLeft + laneWidth * 2f

        val dashHeight = 40f
        val dashGap = 30f
        var currentY = (engineState.distanceMeters * 10f) % (dashHeight + dashGap) - (dashHeight + dashGap)

        while (currentY < h) {
            drawLine(color = PureWhite, start = Offset(lane1X, currentY), end = Offset(lane1X, currentY + dashHeight), strokeWidth = 4f)
            drawLine(color = PureWhite, start = Offset(lane2X, currentY), end = Offset(lane2X, currentY + dashHeight), strokeWidth = 4f)
            currentY += dashHeight + dashGap
        }

        // 5. Verge Trees
        var treeY = (engineState.distanceMeters * 15f) % 180f - 180f
        while (treeY < h) {
            // Left Tree
            drawCircle(color = NeoTeal, radius = 20f, center = Offset(w * 0.07f, treeY))
            drawCircle(color = NeoBlack, radius = 20f, center = Offset(w * 0.07f, treeY), style = stroke3)

            // Right Tree
            drawCircle(color = NeoTeal, radius = 20f, center = Offset(w * 0.93f, treeY + 90f))
            drawCircle(color = NeoBlack, radius = 20f, center = Offset(w * 0.93f, treeY + 90f), style = stroke3)

            treeY += 180f
        }

        // 6. Draw Traffic Obstacles
        for (obs in engineState.obstacles) {
            val obsX = roadLeft + (obs.lane + 0.5f) * laneWidth
            val obsY = obs.yFraction * h

            if (obs.type == ObstacleType.MATATU) {
                // Yellow Matatu
                val carW = laneWidth * 0.65f
                val carH = 70f
                val rect = RoundRect(Rect(obsX - carW / 2, obsY - carH / 2, obsX + carW / 2, obsY + carH / 2), CornerRadius(12f, 12f))
                val path = Path().apply { addRoundRect(rect) }
                drawPath(path, color = NeoYellow)
                drawPath(path, color = NeoBlack, style = stroke3)

                // Black Windshield
                drawRect(color = NeoBlack, topLeft = Offset(obsX - carW * 0.35f, obsY - carH * 0.25f), size = androidx.compose.ui.geometry.Size(carW * 0.7f, carH * 0.25f))
            } else {
                // Pink Cone
                val coneRadius = 18f
                drawCircle(color = NeoPink, radius = coneRadius, center = Offset(obsX, obsY))
                drawCircle(color = NeoBlack, radius = coneRadius, center = Offset(obsX, obsY), style = stroke3)
            }
        }

        // 7. Draw Coins
        for (coin in engineState.coins) {
            val coinX = roadLeft + (coin.lane + 0.5f) * laneWidth
            val coinY = coin.yFraction * h

            drawCircle(color = NeoYellow, radius = 16f, center = Offset(coinX, coinY))
            drawCircle(color = NeoBlack, radius = 16f, center = Offset(coinX, coinY), style = stroke3)
            drawCircle(color = PureWhite, radius = 6f, center = Offset(coinX, coinY))
        }

        // 8. Draw Player Car
        val playerLaneX = roadLeft + (1f + engineState.laneOffsetX + 0.5f) * laneWidth
        val playerY = h * 0.8f
        val carW = laneWidth * 0.68f
        val carH = 75f

        val playerCarColor = Color(engineState.selectedCar.colorHex)

        val playerRect = RoundRect(Rect(playerLaneX - carW / 2, playerY - carH / 2, playerLaneX + carW / 2, playerY + carH / 2), CornerRadius(14f, 14f))
        val playerPath = Path().apply { addRoundRect(playerRect) }
        drawPath(playerPath, color = playerCarColor)
        drawPath(playerPath, color = NeoBlack, style = stroke3)

        // Windshield
        drawRect(color = NeoBlack, topLeft = Offset(playerLaneX - carW * 0.35f, playerY - carH * 0.3f), size = androidx.compose.ui.geometry.Size(carW * 0.7f, carH * 0.25f))
    }
}
