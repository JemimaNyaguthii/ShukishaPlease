package com.jem.shukishaplease.features.trip

import org.junit.Assert.assertEquals
import org.junit.Test

class GpsWatchdogTest {

    enum class GpsHealthStatus { STRONG, WEAK, LOST }

    private fun evaluateGpsStatus(timeSinceLastUpdateMs: Long): GpsHealthStatus {
        return when {
            timeSinceLastUpdateMs > 180000L -> GpsHealthStatus.LOST  // 3 minutes
            timeSinceLastUpdateMs > 60000L -> GpsHealthStatus.WEAK   // 60 seconds
            else -> GpsHealthStatus.STRONG
        }
    }

    @Test
    fun recentUpdate_returnsStrongStatus() {
        val status = evaluateGpsStatus(5000L) // 5 seconds
        assertEquals(GpsHealthStatus.STRONG, status)
    }

    @Test
    fun updateOlderThan60s_returnsWeakStatus() {
        val status = evaluateGpsStatus(75000L) // 75 seconds
        assertEquals(GpsHealthStatus.WEAK, status)
    }

    @Test
    fun updateOlderThan3m_returnsLostStatus() {
        val status = evaluateGpsStatus(200000L) // 200 seconds
        assertEquals(GpsHealthStatus.LOST, status)
    }
}
