package com.example

import com.example.data.model.Destination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun destination_defaultValues_areCorrect() {
        val dest = Destination(
            name = "สถานีสยาม",
            latitude = 13.7456,
            longitude = 100.5348,
            alertDistanceMeters = 500,
            category = "BTS"
        )
        assertEquals("สถานีสยาม", dest.name)
        assertEquals(13.7456, dest.latitude, 0.0001)
        assertEquals(100.5348, dest.longitude, 0.0001)
        assertEquals(500, dest.alertDistanceMeters)
        assertEquals("BTS", dest.category)
        assertTrue(dest.createdAt > 0)
    }

    @Test
    fun destination_progressCalculation_isAccurate() {
        val initialDist = 1000f
        val currentDist = 250f
        val progress = (1f - (currentDist / initialDist)).coerceIn(0f, 1f)
        assertEquals(0.75f, progress, 0.001f)
    }
}
