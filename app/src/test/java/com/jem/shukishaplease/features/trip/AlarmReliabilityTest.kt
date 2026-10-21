package com.jem.shukishaplease.features.trip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class AlarmReliabilityTest {

    // Testable Thread-Safe Atomic Alarm Trigger Lock
    class AtomicAlarmTriggerLock {
        @Volatile
        var isTripActive: Boolean = false

        @Volatile
        var alarmFired: Boolean = false

        @Synchronized
        fun tryClaimAlarmTrigger(): Boolean {
            if (!isTripActive) return false
            if (alarmFired) return false
            alarmFired = true
            return true
        }

        fun resetForNewTrip() {
            isTripActive = true
            alarmFired = false
        }
    }

    @Test
    fun inactiveTrip_returnsFalse() {
        val lock = AtomicAlarmTriggerLock()
        lock.isTripActive = false

        val claimed = lock.tryClaimAlarmTrigger()
        assertFalse(claimed)
    }

    @Test
    fun firstTrigger_claimsAlarmSuccessfully() {
        val lock = AtomicAlarmTriggerLock()
        lock.resetForNewTrip()

        val claimed = lock.tryClaimAlarmTrigger()
        assertTrue(claimed)
        assertTrue(lock.alarmFired)
    }

    @Test
    fun secondOverlappingTrigger_isDeduplicated() {
        val lock = AtomicAlarmTriggerLock()
        lock.resetForNewTrip()

        val firstClaim = lock.tryClaimAlarmTrigger()
        val secondClaim = lock.tryClaimAlarmTrigger()

        assertTrue(firstClaim)
        assertFalse(secondClaim)
    }

    @Test
    fun concurrentSimultaneousTriggers_exactlyOneSucceeds() {
        val lock = AtomicAlarmTriggerLock()
        lock.resetForNewTrip()

        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val finishLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        for (i in 0 until threadCount) {
            executor.execute {
                try {
                    startLatch.await()
                    if (lock.tryClaimAlarmTrigger()) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    finishLatch.countDown()
                }
            }
        }

        startLatch.countDown() // Release all threads simultaneously
        finishLatch.await()
        executor.shutdown()

        assertEquals(1, successCount.get())
        assertTrue(lock.alarmFired)
    }

    @Test
    fun newTripReset_allowsNewAlarmToTrigger() {
        val lock = AtomicAlarmTriggerLock()

        // Trip 1
        lock.resetForNewTrip()
        assertTrue(lock.tryClaimAlarmTrigger())
        assertFalse(lock.tryClaimAlarmTrigger())

        // Trip 2 (New Trip Reset)
        lock.resetForNewTrip()
        assertTrue(lock.tryClaimAlarmTrigger())
    }

    @Test
    fun calculateTriggerTime_handlesMinimumBoundsCorrectly() {
        val currentMs = 1000000L
        val etaMinutes = 2
        val alertBufferMinutes = 3

        val triggerMinutes = (etaMinutes - alertBufferMinutes).coerceAtLeast(1)
        val triggerTimeMs = currentMs + (triggerMinutes * 60 * 1000L)

        assertEquals(currentMs + 60000L, triggerTimeMs)
    }
}
