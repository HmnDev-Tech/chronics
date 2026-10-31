package com.hmndev.chronika.util

import com.hmndev.chronika.i18n.S
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeTools {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private fun timeFmt() = DateTimeFormatter.ofPattern("HH:mm", S.locale)
    private fun dayFmt() = DateTimeFormatter.ofPattern("d MMMM", S.locale)
    private fun fullFmt() = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", S.locale)
    private fun fileFmt() = DateTimeFormatter.ofPattern("yyyy-MM-dd", S.locale)
    private fun fileTimeFmt() = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", S.locale)

    fun time(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt())

    fun full(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(fullFmt())

    fun dayLabel(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when (date) {
            today -> S["time.today"]
            today.minusDays(1) -> S["time.yesterday"]
            else -> dayFmt().format(date)
        }
    }

    fun dayKey(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toString()

    fun fileDay(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(fileFmt())

    fun fileStamp(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(fileTimeFmt())

    fun nowStamp(): String = fileStamp(System.currentTimeMillis())

    fun isToday(millis: Long): Boolean =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate() == LocalDate.now(zone)

    fun startOfDay(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()

    fun relative(millis: Long): String {
        val diff = (System.currentTimeMillis() - millis).coerceAtLeast(0L)
        return when {
            diff < 60_000 -> S["time.just_now"]
            diff < 3_600_000 -> S.f("time.minutes_ago", diff / 60_000)
            diff < 86_400_000 -> S.f("time.hours_ago", diff / 3_600_000)
            else -> dayLabel(millis)
        }
    }

    fun iso(millis: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone).toString()
}
