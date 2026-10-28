package com.hmndev.chronika

import com.hmndev.chronika.util.TimeTools
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TimeToolsTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    @Test
    fun dayKeyMatchesLocalDate() {
        val millis = Instant.parse("2026-03-04T10:15:30Z").toEpochMilli()
        val expected = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toString()
        assertEquals(expected, TimeTools.dayKey(millis))
    }

    @Test
    fun startOfDayIsMidnight() {
        val millis = Instant.parse("2026-03-04T10:15:30Z").toEpochMilli()
        val start = TimeTools.startOfDay(millis)
        val local = Instant.ofEpochMilli(start).atZone(zone)
        assertEquals(0, local.hour)
        assertEquals(0, local.minute)
        assertEquals(0, local.second)
    }

    @Test
    fun todayLabelIsRecognised() {
        val now = System.currentTimeMillis()
        assertEquals("Сегодня", TimeTools.dayLabel(now))
        assertTrue(TimeTools.isToday(now))
        assertFalse(TimeTools.isToday(now - 3L * 86_400_000L))
    }

    @Test
    fun fileStampIsSortable() {
        val millis = Instant.parse("2026-03-04T10:15:30Z").toEpochMilli()
        val stamp = TimeTools.fileStamp(millis)
        assertTrue(stamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")))
        val expected = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        assertEquals(expected, LocalDate.parse(stamp.substring(0, 10)))
    }

    @Test
    fun relativeForFreshTimestamp() {
        assertEquals("только что", TimeTools.relative(System.currentTimeMillis()))
    }
}
