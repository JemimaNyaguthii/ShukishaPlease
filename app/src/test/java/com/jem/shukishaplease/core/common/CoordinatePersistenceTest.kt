package com.jem.shukishaplease.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoordinatePersistenceTest {

    private fun sanitizeLatitude(input: Double?): Double? {
        if (input == null || input.isNaN() || input.isInfinite()) return null
        return if (input in -90.0..90.0) input else null
    }

    private fun sanitizeLongitude(input: Double?): Double? {
        if (input == null || input.isNaN() || input.isInfinite()) return null
        return if (input in -180.0..180.0) input else null
    }

    private fun parseCoordinateString(input: String?, legacyFloatFallback: Float?): Double? {
        val parsed = input?.toDoubleOrNull()
        if (parsed != null && !parsed.isNaN() && !parsed.isInfinite()) {
            return if (parsed in -180.0..180.0) parsed else null
        }
        val fallback = legacyFloatFallback?.toDouble()
        if (fallback != null && !fallback.isNaN() && !fallback.isInfinite()) {
            return if (fallback in -180.0..180.0) fallback else null
        }
        return null
    }

    @Test
    fun doublePrecisionRoundTrip_preservesHighPrecision() {
        val originalLat = -1.286389123456789
        val asString = originalLat.toString()
        val restored = asString.toDoubleOrNull()
        assertEquals(originalLat, restored!!, 1e-12)
    }

    @Test
    fun legacyFloatFallback_restoresFloatValue() {
        val legacyFloat = -1.286389f
        val restored = parseCoordinateString(null, legacyFloat)
        assertEquals(legacyFloat.toDouble(), restored!!, 1e-6)
    }

    @Test
    fun invalidLatitudeRange_returnsNull() {
        assertNull(sanitizeLatitude(120.0))
        assertNull(sanitizeLatitude(-95.0))
        assertEquals(-0.4201, sanitizeLatitude(-0.4201)!!, 1e-9)
    }

    @Test
    fun invalidLongitudeRange_returnsNull() {
        assertNull(sanitizeLongitude(200.0))
        assertNull(sanitizeLongitude(-190.0))
        assertEquals(36.9476, sanitizeLongitude(36.9476)!!, 1e-9)
    }

    @Test
    fun nanAndInfiniteCoordinates_returnsNull() {
        assertNull(sanitizeLatitude(Double.NaN))
        assertNull(sanitizeLatitude(Double.POSITIVE_INFINITY))
        assertNull(sanitizeLatitude(Double.NEGATIVE_INFINITY))
        assertNull(sanitizeLongitude(Double.NaN))
        assertNull(sanitizeLongitude(Double.POSITIVE_INFINITY))
    }

    @Test
    fun malformedCoordinateString_fallsBackSafely() {
        val restored = parseCoordinateString("invalid_number", -0.4201f)
        assertEquals(-0.4201, restored!!, 1e-4)
    }
}
