package com.example

import com.example.data.model.Destination
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun destination_creation_isCorrect() {
        val dest = Destination(
            name = "สถานีอโศก",
            latitude = 13.7370,
            longitude = 100.5604,
            alertDistanceMeters = 500,
            category = "BTS/MRT"
        )
        assertEquals("สถานีอโศก", dest.name)
        assertEquals(500, dest.alertDistanceMeters)
    }
}
