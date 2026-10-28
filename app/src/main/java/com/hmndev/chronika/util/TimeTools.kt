package com.hmndev.chronika.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeTools {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val ru = Locale("ru", "RU")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm", ru)
    private val dayFmt = DateTimeFormatter.ofPattern("d MMMM", ru)
    private val fullFmt = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", ru)
    private val fileFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", ru)
    private val fileTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", ru)

    fun time(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt)

    fun full(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(fullFmt)

    fun dayLabel(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Сегодня"
            today.minusDays(1) -> "Вчера"
            else -> dayFmt.format(date)
        }
    }

    fun dayKey(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toString()

    fun fileDay(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(fileFmt)

    fun fileStamp(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(fileTimeFmt)

    fun nowStamp(): String = fileStamp(System.currentTimeMillis())

    fun isToday(millis: Long): Boolean =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate() == LocalDate.now(zone)

    fun startOfDay(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()

    fun relative(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        return when {
            diff < 60_000 -> "только что"
            diff < 3_600_000 -> "${diff / 60_000} мин назад"
            diff < 86_400_000 -> "${diff / 3_600_000} ч назад"
            else -> dayLabel(millis)
        }
    }

    fun iso(millis: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone).toString()
}
