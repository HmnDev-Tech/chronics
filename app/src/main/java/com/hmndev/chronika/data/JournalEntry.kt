package com.hmndev.chronika.data

data class JournalEntry(
    val id: Long = 0L,
    val packageName: String = "",
    val appLabel: String = "",
    val title: String = "",
    val text: String = "",
    val bigText: String = "",
    val subText: String = "",
    val category: String = "",
    val channelId: String = "",
    val channelName: String = "",
    val groupKey: String = "",
    val conversationTitle: String = "",
    val messagesJson: String = "[]",
    val actionsJson: String = "[]",
    val extrasJson: String = "{}",
    val imagePaths: String = "[]",
    val source: String = "listener",
    val postedAt: Long = 0L,
    val capturedAt: Long = 0L,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val priority: Int = 0,
    val isSilent: Boolean = false,
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val fingerprint: String = "",
    val exportPath: String = ""
) {
    val displayTitle: String
        get() = title.ifBlank { appLabel }

    val displayBody: String
        get() = bigText.ifBlank { text }.ifBlank { subText }

    val hasMessages: Boolean
        get() = messagesJson.length > 4
}

data class JournalStats(
    val total: Int = 0,
    val today: Int = 0,
    val unread: Int = 0,
    val pinned: Int = 0,
    val apps: Int = 0,
    val exported: Int = 0,
    val oldest: Long = 0L,
    val newest: Long = 0L,
    val todayByHour: List<Int> = List(24) { 0 }
)

data class AppCount(
    val packageName: String,
    val appLabel: String,
    val count: Int
)

data class DayCount(
    val dayKey: String,
    val label: String,
    val count: Int
)
