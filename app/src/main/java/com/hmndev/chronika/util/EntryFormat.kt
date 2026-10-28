package com.hmndev.chronika.util

import com.hmndev.chronika.data.JournalEntry
import org.json.JSONArray

data class MessageView(
    val sender: String,
    val text: String,
    val time: Long
)

object EntryFormat {
    fun messages(entry: JournalEntry): List<MessageView> {
        val array = runCatching { JSONArray(entry.messagesJson) }.getOrElse { return emptyList() }
        val result = ArrayList<MessageView>(array.length())
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            result.add(
                MessageView(
                    sender = item.optString("sender"),
                    text = item.optString("text"),
                    time = item.optLong("time")
                )
            )
        }
        return result
    }

    fun actions(entry: JournalEntry): List<String> {
        val array = runCatching { JSONArray(entry.actionsJson) }.getOrElse { return emptyList() }
        val result = ArrayList<String>(array.length())
        for (i in 0 until array.length()) {
            val value = array.optString(i)
            if (value.isNotBlank()) result.add(value)
        }
        return result
    }

    fun images(entry: JournalEntry): List<String> {
        val array = runCatching { JSONArray(entry.imagePaths) }.getOrElse { return emptyList() }
        val result = ArrayList<String>(array.length())
        for (i in 0 until array.length()) {
            val value = array.optString(i)
            if (value.isNotBlank()) result.add(value)
        }
        return result
    }

    fun sourceLabel(source: String): String = when (source) {
        "listener" -> "Уведомление"
        "accessibility" -> "Доступность"
        "shizuku" -> "Shizuku"
        else -> source
    }

    fun importanceLabel(priority: Int): String = when {
        priority >= 2 -> "высокий"
        priority == 1 -> "повышенный"
        priority == 0 -> "обычный"
        priority == -1 -> "низкий"
        else -> "тихий"
    }

    fun searchText(entry: JournalEntry): String =
        "${entry.appLabel} ${entry.title} ${entry.text} ${entry.bigText} " +
            "${entry.subText} ${entry.conversationTitle} ${entry.packageName} " +
            messages(entry).joinToString(" ") { "${it.sender} ${it.text}" }
}
