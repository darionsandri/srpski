package com.srpski.app.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeApiTest {

    @Test fun parseIso8601MinutesAndSeconds() {
        assertEquals(215, YouTubeApi.parseIso8601Seconds("PT3M35S"))
    }

    @Test fun parseIso8601HoursOnly() {
        assertEquals(3600, YouTubeApi.parseIso8601Seconds("PT1H"))
    }

    @Test fun parseIso8601Full() {
        assertEquals(3665, YouTubeApi.parseIso8601Seconds("PT1H1M5S"))
    }

    @Test fun parseIso8601MalformedReturnsNull() {
        assertNull(YouTubeApi.parseIso8601Seconds("garbage"))
    }
}
