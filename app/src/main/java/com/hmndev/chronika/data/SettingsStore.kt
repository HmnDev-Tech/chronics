package com.hmndev.chronika.data

import android.content.Context
import android.content.SharedPreferences
import com.hmndev.chronika.i18n.AppLanguage
import com.hmndev.chronika.i18n.S
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsStore {
    private const val PREFS = "chronika_settings"
    private lateinit var prefs: SharedPreferences
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Hold a strong reference so SharedPreferences' WeakHashMap does not GC the listener.
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            val next = load()
            _settings.value = next
            if (key == "language") {
                S.setLanguage(AppLanguage.resolve(next.language))
            }
        }

    @Synchronized
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val initial = load()
        _settings.value = initial
        S.setLanguage(AppLanguage.resolve(initial.language))
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun current(): AppSettings {
        if (!::prefs.isInitialized) return AppSettings()
        return _settings.value
    }

    @Synchronized
    fun update(transform: (AppSettings) -> AppSettings) {
        val rawNext = transform(_settings.value)
        val next = rawNext.copy(
            folderName = AppSettings.sanitizeFolder(rawNext.folderName),
            maxEntries = rawNext.maxEntries.coerceIn(500, 200000),
            retentionDays = rawNext.retentionDays.coerceIn(0, 365),
            uiScale = rawNext.uiScale.coerceIn(0.85f, 1.15f),
            excludedApps = rawNext.excludedApps.toSet()
        )
        _settings.value = next
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putBoolean("onboardingDone", next.onboardingDone)
            putBoolean("captureEnabled", next.captureEnabled)
            putBoolean("captureListener", next.captureListener)
            putBoolean("captureAccessibility", next.captureAccessibility)
            putBoolean("deepAccessibility", next.deepAccessibility)
            putBoolean("autoExport", next.autoExport)
            putBoolean("exportJson", next.exportJson)
            putBoolean("exportTxt", next.exportTxt)
            putBoolean("saveImages", next.saveImages)
            putBoolean("saveRawExtras", next.saveRawExtras)
            putBoolean("ignoreOngoing", next.ignoreOngoing)
            putBoolean("ignoreSilent", next.ignoreSilent)
            putBoolean("dedupe", next.dedupe)
            putInt("minPriority", next.minPriority)
            putInt("retentionDays", next.retentionDays)
            putInt("maxEntries", next.maxEntries)
            putString("folderName", next.folderName)
            putBoolean("maskSecrets", next.maskSecrets)
            putStringSet("excludedApps", HashSet(next.excludedApps))
            putBoolean("includedOnly", next.includedOnly)
            putString("excludedKeywords", next.excludedKeywords)
            putString("requiredKeywords", next.requiredKeywords)
            putString("themeMode", next.themeMode)
            putBoolean("dynamicColor", next.dynamicColor)
            putBoolean("compact", next.compact)
            putBoolean("showRawPayload", next.showRawPayload)
            putString("language", next.language)
            putBoolean("adaptiveLayout", next.adaptiveLayout)
            putFloat("uiScale", next.uiScale)
            apply()
        }
    }

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            onboardingDone = prefs.getBoolean("onboardingDone", defaults.onboardingDone),
            captureEnabled = prefs.getBoolean("captureEnabled", defaults.captureEnabled),
            captureListener = prefs.getBoolean("captureListener", defaults.captureListener),
            captureAccessibility = prefs.getBoolean("captureAccessibility", defaults.captureAccessibility),
            deepAccessibility = prefs.getBoolean("deepAccessibility", defaults.deepAccessibility),
            autoExport = prefs.getBoolean("autoExport", defaults.autoExport),
            exportJson = prefs.getBoolean("exportJson", defaults.exportJson),
            exportTxt = prefs.getBoolean("exportTxt", defaults.exportTxt),
            saveImages = prefs.getBoolean("saveImages", defaults.saveImages),
            saveRawExtras = prefs.getBoolean("saveRawExtras", defaults.saveRawExtras),
            ignoreOngoing = prefs.getBoolean("ignoreOngoing", defaults.ignoreOngoing),
            ignoreSilent = prefs.getBoolean("ignoreSilent", defaults.ignoreSilent),
            dedupe = prefs.getBoolean("dedupe", defaults.dedupe),
            minPriority = prefs.getInt("minPriority", defaults.minPriority),
            retentionDays = prefs.getInt("retentionDays", defaults.retentionDays).coerceIn(0, 365),
            maxEntries = prefs.getInt("maxEntries", defaults.maxEntries).coerceIn(500, 200000),
            folderName = AppSettings.sanitizeFolder(prefs.getString("folderName", defaults.folderName)),
            maskSecrets = prefs.getBoolean("maskSecrets", defaults.maskSecrets),
            excludedApps = prefs.getStringSet("excludedApps", defaults.excludedApps)?.toSet() ?: emptySet(),
            includedOnly = prefs.getBoolean("includedOnly", defaults.includedOnly),
            excludedKeywords = prefs.getString("excludedKeywords", defaults.excludedKeywords).orEmpty(),
            requiredKeywords = prefs.getString("requiredKeywords", defaults.requiredKeywords).orEmpty(),
            themeMode = prefs.getString("themeMode", defaults.themeMode).orEmpty().ifBlank { defaults.themeMode },
            dynamicColor = prefs.getBoolean("dynamicColor", defaults.dynamicColor),
            compact = prefs.getBoolean("compact", defaults.compact),
            showRawPayload = prefs.getBoolean("showRawPayload", defaults.showRawPayload),
            language = prefs.getString("language", defaults.language).orEmpty(),
            adaptiveLayout = prefs.getBoolean("adaptiveLayout", defaults.adaptiveLayout),
            uiScale = prefs.getFloat("uiScale", defaults.uiScale).coerceIn(0.85f, 1.15f)
        )
    }
}
