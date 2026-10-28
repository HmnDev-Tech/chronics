package com.hmndev.chronika.util

object TextTools {
    private val email = Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})")
    private val phone = Regex("(\\+?\\d[\\d\\s()\\-]{7,}\\d)")
    private val otpContext = Regex(
        "(?i)((?:код|code|otp|pin|пароль|password|passcode)[^\\d]{0,16})(\\d{4,8})"
    )
    private val card = Regex("\\b(\\d{4})[ -]?(\\d{4})[ -]?(\\d{4})[ -]?(\\d{4})\\b")

    fun maskSecrets(input: String): String {
        if (input.isBlank()) return input
        var value = input
        value = email.replace(value) { m -> "•••@${m.groupValues[2].take(1)}•••" }
        value = card.replace(value) { m -> "•••• •••• •••• ${m.groupValues[4]}" }
        value = otpContext.replace(value) { m -> "${m.groupValues[1]}••••" }
        value = phone.replace(value) { m ->
            val digits = m.value.count { it.isDigit() }
            if (digits >= 9) "••• ••• •• ${m.value.takeLast(2)}" else m.value
        }
        return value
    }

    fun compactPreview(text: String, max: Int = 180): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        return if (clean.length <= max) clean else clean.take(max).trimEnd() + "…"
    }

    fun initials(label: String): String {
        val parts = label.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
}
