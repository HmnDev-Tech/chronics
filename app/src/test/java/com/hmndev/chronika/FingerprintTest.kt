package com.hmndev.chronika

import com.hmndev.chronika.util.Fingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FingerprintTest {

    @Test
    fun fingerprintIsStable() {
        val first = Fingerprint.of("com.example", "title", "text")
        val second = Fingerprint.of("com.example", "title", "text")
        assertEquals(first, second)
    }

    @Test
    fun fingerprintIsSha256Length() {
        assertEquals(64, Fingerprint.of("a").length)
    }

    @Test
    fun fingerprintSeparatesParts() {
        assertNotEquals(
            Fingerprint.of("ab", "c"),
            Fingerprint.of("a", "bc")
        )
    }
}
