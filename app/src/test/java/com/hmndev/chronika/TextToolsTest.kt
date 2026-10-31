package com.hmndev.chronika

import com.hmndev.chronika.util.TextTools
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextToolsTest {

    @Test
    fun initialsHandlesSingleAndMultipleWords() {
        assertEquals("A", TextTools.initials("alpha"))
        assertEquals("AB", TextTools.initials("alpha beta"))
        assertEquals("?", TextTools.initials("   "))
    }

    @Test
    fun compactPreviewTruncatesLongText() {
        val text = "a".repeat(300)
        val preview = TextTools.compactPreview(text, max = 40)
        assertEquals(41, preview.length)
        assertTrue(preview.endsWith("…"))
    }

    @Test
    fun compactPreviewKeepsShortText() {
        assertEquals("короткий текст", TextTools.compactPreview("  короткий   текст  "))
    }

    @Test
    fun maskSecretsHidesEmailAndCard() {
        val masked = TextTools.maskSecrets("mail test@example.com card 4111 1111 1111 1111")
        assertFalse(masked.contains("test@example.com"))
        assertFalse(masked.contains("4111 1111 1111 1111"))
        assertTrue(masked.contains("1111"))
    }

    @Test
    fun maskSecretsKeepsOrdinaryText() {
        val text = "Просто сообщение без секретов"
        assertEquals(text, TextTools.maskSecrets(text))
    }

    @Test
    fun maskJsonStringsMasksValuesWhileKeepingKeysAndNumbers() {
        val raw = """{"android.title":"Ваш код: 482910","email":"user@example.org","time":1710000000000}"""
        val masked = TextTools.maskJsonStrings(raw)
        assertTrue(masked.contains("\"android.title\":"))
        assertTrue(masked.contains("\"email\":"))
        assertTrue(masked.contains("1710000000000"))
        assertFalse(masked.contains("482910"))
        assertFalse(masked.contains("user@example.org"))
    }

    @Test
    fun prettyJsonIndentsObjectCleanly() {
        val formatted = TextTools.prettyJson("""{"a":"1","b":2}""")
        assertTrue(formatted.contains("\n  \"a\": \"1\""))
        assertTrue(formatted.contains("\n  \"b\": 2"))
    }
}
