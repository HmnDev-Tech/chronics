package com.hmndev.chronika.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val labelKey: String, val locale: Locale) {
    EN("en", "language.en", Locale.ENGLISH),
    RU("ru", "language.ru", Locale("ru", "RU")),
    KK("kk", "language.kk", Locale("kk", "KZ")),
    ES("es", "language.es", Locale("es", "ES")),
    ZH("zh", "language.zh", Locale.SIMPLIFIED_CHINESE),
    PT("pt", "language.pt", Locale("pt", "BR"));

    companion object {
        fun byCode(code: String?): AppLanguage? =
            AppLanguage.entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        fun fromSystem(): AppLanguage {
            val tag = Locale.getDefault().language.lowercase()
            return AppLanguage.entries.firstOrNull { it.code == tag } ?: EN
        }

        /** Empty or unknown code means auto-detect from the system locale. */
        fun resolve(code: String?): AppLanguage =
            if (code.isNullOrBlank()) fromSystem() else byCode(code) ?: fromSystem()
    }
}

/**
 * Runtime string catalog. Screens read strings through [S] so that switching the
 * language only requires updating [language] and recreating the activity.
 */
object S {
    @Volatile
    var language: AppLanguage = AppLanguage.EN
        private set

    val locale: Locale get() = language.locale

    fun setLanguage(value: AppLanguage) {
        language = value
    }

    private fun catalog(): Map<String, String> = when (language) {
        AppLanguage.EN -> EN_STRINGS
        AppLanguage.RU -> RU_STRINGS
        AppLanguage.KK -> KK_STRINGS
        AppLanguage.ES -> ES_STRINGS
        AppLanguage.ZH -> ZH_STRINGS
        AppLanguage.PT -> PT_STRINGS
    }

    /** Returns the localized string for [key], falling back to English and then the key itself. */
    operator fun get(key: String): String =
        catalog()[key] ?: EN_STRINGS[key] ?: key

    /**
     * Formats a localized template. Templates use plain [String.format] placeholders
     * in argument order: %d, %s, %.1f and %% for a literal percent sign.
     */
    fun f(key: String, vararg args: Any?): String {
        val template = get(key)
        if (args.isEmpty()) return template
        return try {
            String.format(locale, template, *args)
        } catch (_: Exception) {
            template
        }
    }
}
