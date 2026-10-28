package com.hmndev.chronika.util

import java.security.MessageDigest

object Fingerprint {
    fun of(vararg parts: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(parts.joinToString("\u0001").toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
