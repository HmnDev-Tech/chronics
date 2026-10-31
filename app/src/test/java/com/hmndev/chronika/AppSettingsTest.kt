package com.hmndev.chronika

import com.hmndev.chronika.data.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun excludedAppIsRejected() {
        val settings = AppSettings(excludedApps = setOf("com.spam.app"))
        assertFalse(settings.appAllowed("com.spam.app"))
        assertTrue(settings.appAllowed("com.chat.app"))
    }

    @Test
    fun whitelistOnlyAllowsSelected() {
        val settings = AppSettings(includedOnly = true, excludedApps = setOf("com.chat.app"))
        assertTrue(settings.appAllowed("com.chat.app"))
        assertFalse(settings.appAllowed("com.other.app"))
    }

    @Test
    fun stopWordsRejectNotification() {
        val settings = AppSettings(excludedKeywords = "реклама, скидка")
        assertFalse(settings.keywordAllowed("Большая СКИДКА на всё"))
        assertTrue(settings.keywordAllowed("Новое сообщение"))
    }

    @Test
    fun requiredWordsFilterNotification() {
        val settings = AppSettings(requiredKeywords = "код, заказ")
        assertTrue(settings.keywordAllowed("Ваш код: 1234"))
        assertFalse(settings.keywordAllowed("Просто привет"))
    }

    @Test
    fun emptyFiltersAllowEverything() {
        val settings = AppSettings()
        assertTrue(settings.keywordAllowed("любой текст"))
        assertTrue(settings.appAllowed("any.package"))
    }

    @Test
    fun sanitizeFolderBlocksPathTraversalAndLegacyName() {
        assertEquals("Chronics", AppSettings.sanitizeFolder(""))
        assertEquals("Chronics", AppSettings.sanitizeFolder("   "))
        assertEquals("Chronics", AppSettings.sanitizeFolder("Chronika"))
        assertEquals("Chronics", AppSettings.sanitizeFolder("../.."))
        assertEquals("etc_passwd", AppSettings.sanitizeFolder("/etc/passwd"))
        assertEquals("My_Archive-2026", AppSettings.sanitizeFolder("My_Archive-2026"))
    }
}
