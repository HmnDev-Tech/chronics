package com.hmndev.chronika.data

data class AppSettings(
    val onboardingDone: Boolean = false,
    val captureEnabled: Boolean = true,
    val captureListener: Boolean = true,
    val captureAccessibility: Boolean = false,
    val deepAccessibility: Boolean = false,
    val autoExport: Boolean = true,
    val exportJson: Boolean = true,
    val exportTxt: Boolean = true,
    val saveImages: Boolean = true,
    val saveRawExtras: Boolean = true,
    val ignoreOngoing: Boolean = true,
    val ignoreSilent: Boolean = false,
    val dedupe: Boolean = true,
    val minPriority: Int = -2,
    val retentionDays: Int = 0,
    val maxEntries: Int = 20000,
    val folderName: String = "Chronics",
    val maskSecrets: Boolean = false,
    val excludedApps: Set<String> = emptySet(),
    val includedOnly: Boolean = false,
    val excludedKeywords: String = "",
    val requiredKeywords: String = "",
    val themeMode: String = "system",
    val dynamicColor: Boolean = true,
    val compact: Boolean = false,
    val showRawPayload: Boolean = false,
    val language: String = "",
    val adaptiveLayout: Boolean = true,
    val uiScale: Float = 1f
) {
    val safeFolderName: String
        get() = sanitizeFolder(folderName)

    fun appAllowed(packageName: String): Boolean {
        if (includedOnly) return excludedApps.contains(packageName)
        return !excludedApps.contains(packageName)
    }

    fun keywordAllowed(haystack: String): Boolean {
        val hay = haystack.lowercase()
        val excluded = excludedKeywords.split(',', '\n')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        if (excluded.any { hay.contains(it) }) return false

        val required = requiredKeywords.split(',', '\n')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        if (required.isNotEmpty() && required.none { hay.contains(it) }) return false
        return true
    }

    companion object {
        const val DEFAULT_FOLDER = "Chronics"

        fun sanitizeFolder(raw: String?): String {
            val cleaned = raw.orEmpty()
                .replace("..", "")
                .replace('\\', '_')
                .replace('/', '_')
                .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                .trim { it.isWhitespace() || it == '.' || it == '_' }
                .take(48)
                .trim { it.isWhitespace() || it == '.' }
            return when {
                cleaned.isBlank() -> DEFAULT_FOLDER
                cleaned == "Chronika" -> DEFAULT_FOLDER
                else -> cleaned
            }
        }
    }
}
