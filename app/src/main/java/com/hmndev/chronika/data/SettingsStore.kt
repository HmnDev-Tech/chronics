package com.hmndev.chronika.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsStore {
    private const val PREFS = "chronika_settings"
    private lateinit var prefs: SharedPreferences
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _settings.value = load()
        prefs.registerOnSharedPreferenceChangeListener { _, _ ->
            _settings.value = load()
        }
    }

    fun current(): AppSettings {
        if (!::prefs.isInitialized) return AppSettings()
        return _settings.value
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
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
            putStringSet("excludedApps", next.excludedApps)
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
        _settings.value = next
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
            retentionDays = prefs.getInt("retentionDays", defaults.retentionDays),
            maxEntries = prefs.getInt("maxEntries", defaults.maxEntries),
            folderName = prefs.getString("folderName", defaults.folderName)
                .orEmpty()
                .ifBlank { defaults.folderName }
                .let { if (it == "Chronika") defaults.folderName else it },
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
