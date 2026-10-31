package com.hmndev.chronika.util

object TextTools {
    private val email = Regex("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})")
    private val phone = Regex("(\\+?\\d[\\d\\s()\\-]{7,}\\d)")
    private val otpContext = Regex(
        "(?i)((?:код|code|otp|pin|пароль|password|passcode)[^\\d]{0,16})(\\d{4,8})"
    )
    private val card = Regex("\\b(\\d{4})[ -]?(\\d{4})[ -]?(\\d{4})[ -]?(\\d{4})\\b")
    private val jsonStringLiteral = Regex("\"((?:\\\\.|[^\"\\\\])*)\"(\\s*:)?")

    fun maskSecrets(input: String): String {
        if (input.isBlank()) return input
        var value = input
        value = email.replace(value) { m -> "•••@${m.groupValues[2].take(1)}•••" }
        value = card.replace(value) { m -> "•••• •••• •••• ${m.groupValues[4]}" }
        value = otpContext.replace(value) { m -> "${m.groupValues[1]}••••" }
        value = phone.replace(value) { m ->
            val digits = m.value.count { it.isDigit() }
            if (digits in 9..15) "••• ••• •• ${m.value.takeLast(2)}" else m.value
        }
        return value
    }

    /**
     * Masks secrets inside JSON string values while preserving JSON keys, numeric fields,
     * booleans and structural syntax.
     */
    fun maskJsonStrings(json: String): String {
        if (json.isBlank() || json == "{}" || json == "[]") return json
        return jsonStringLiteral.replace(json) { match ->
            val isKey = match.groupValues[2].isNotEmpty()
            if (isKey) {
                match.value
            } else {
                val rawContent = unescapeJsonString(match.groupValues[1])
                val masked = maskSecrets(rawContent)
                "\"${escapeJsonString(masked)}\""
            }
        }
    }

    fun compactPreview(text: String, max: Int = 180): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        return if (clean.length <= max) clean else clean.take(max).trimEnd() + "…"
    }

    fun initials(label: String): String {
        val parts = label.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> firstCodePointUpper(parts[0])
            else -> firstCodePointUpper(parts[0]) + firstCodePointUpper(parts[1])
        }
    }

    fun prettyJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "{}"
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return trimmed
        val out = StringBuilder(trimmed.length + 64)
        var indent = 0
        var inString = false
        var escaped = false

        fun appendIndent() {
            repeat(indent) { out.append("  ") }
        }

        for (i in trimmed.indices) {
            val ch = trimmed[i]
            if (escaped) {
                out.append(ch)
                escaped = false
                continue
            }
            if (ch == '\\' && inString) {
                out.append(ch)
                escaped = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                out.append(ch)
                continue
            }
            if (inString) {
                out.append(ch)
                continue
            }
            when (ch) {
                '{', '[' -> {
                    out.append(ch)
                    val nextNonWs = (i + 1 until trimmed.length)
                        .firstOrNull { !trimmed[it].isWhitespace() }
                        ?.let { trimmed[it] }
                    if (nextNonWs != '}' && nextNonWs != ']') {
                        indent += 1
                        out.append('\n')
                        appendIndent()
                    }
                }
                '}', ']' -> {
                    val prevNonWs = (i - 1 downTo 0)
                        .firstOrNull { !trimmed[it].isWhitespace() }
                        ?.let { trimmed[it] }
                    if (prevNonWs != '{' && prevNonWs != '[') {
                        indent = (indent - 1).coerceAtLeast(0)
                        out.append('\n')
                        appendIndent()
                    }
                    out.append(ch)
                }
                ',' -> {
                    out.append(ch)
                    out.append('\n')
                    appendIndent()
                }
                ':' -> {
                    out.append(": ")
                }
                else -> {
                    if (!ch.isWhitespace()) out.append(ch)
                }
            }
        }
        return out.toString()
    }

    private fun firstCodePointUpper(word: String): String {
        if (word.isEmpty()) return ""
        val cp = word.codePointAt(0)
        val count = Character.charCount(cp)
        return word.substring(0, count).uppercase()
    }

    private fun unescapeJsonString(value: String): String =
        value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")

    private fun escapeJsonString(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
    }
}
