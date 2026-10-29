package com.hmndev.chronika

import com.hmndev.chronika.i18n.AppLanguage
import com.hmndev.chronika.i18n.EN_STRINGS
import com.hmndev.chronika.i18n.ES_STRINGS
import com.hmndev.chronika.i18n.KK_STRINGS
import com.hmndev.chronika.i18n.PT_STRINGS
import com.hmndev.chronika.i18n.RU_STRINGS
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.i18n.ZH_STRINGS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringsTest {

    private val catalogs = mapOf(
        "ru" to RU_STRINGS,
        "kk" to KK_STRINGS,
        "es" to ES_STRINGS,
        "zh" to ZH_STRINGS,
        "pt" to PT_STRINGS
    )

    @Test
    fun allCatalogsShareTheEnglishKeySet() {
        val expected = EN_STRINGS.keys
        catalogs.forEach { (code, catalog) ->
            assertEquals("$code has missing or extra keys", expected, catalog.keys)
        }
    }

    @Test
    fun noTranslationIsBlank() {
        catalogs.forEach { (code, catalog) ->
            catalog.forEach { (key, value) ->
                assertTrue("$code/$key is blank", value.isNotBlank())
            }
        }
    }

    @Test
    fun placeholdersMatchEnglish() {
        val placeholder = Regex("%%|%[-+0-9.]*[dsf]")
        catalogs.forEach { (code, catalog) ->
            catalog.forEach { (key, value) ->
                val expected = placeholder.findAll(EN_STRINGS.getValue(key)).map { it.value }.toList()
                val actual = placeholder.findAll(value).map { it.value }.toList()
                assertEquals("$code/$key placeholder mismatch", expected, actual)
            }
        }
    }

    @Test
    fun everyLanguageResolvesItsOwnCatalog() {
        AppLanguage.entries.forEach { language ->
            S.setLanguage(language)
            assertEquals(language.code, S.language.code)
        }
        S.setLanguage(AppLanguage.EN)
    }

    @Test
    fun formatReplacesArgumentsInOrder() {
        S.setLanguage(AppLanguage.EN)
        assertEquals(
            "3 of 10 entries · files are in Downloads",
            S.f("charts.storage_line", 3, 10)
        )
    }
}
