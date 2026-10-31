package com.hmndev.chronika.data

import android.content.Context

class JournalRepository(context: Context) {
    private val db = JournalDb.get(context)

    fun entries(): List<JournalEntry> = db.query()

    fun stats(): JournalStats = db.stats()

    fun topApps(): List<AppCount> = db.topApps()

    fun dailyCounts(): List<DayCount> = db.dailyCounts()

    fun markRead(id: Long, read: Boolean = true) {
        db.markRead(id, read)
        JournalStore.changed()
    }

    fun markAllRead(): Int {
        val count = db.markAllRead()
        if (count > 0) JournalStore.changed()
        return count
    }

    fun togglePinned(entry: JournalEntry) {
        db.setPinned(entry.id, !entry.isPinned)
        JournalStore.changed()
    }

    fun setArchived(entry: JournalEntry, archived: Boolean) {
        db.setArchived(entry.id, archived)
        JournalStore.changed()
    }

    fun delete(id: Long) {
        db.delete(id)
        JournalStore.changed()
    }

    fun deleteAll() {
        db.deleteAll()
        JournalStore.changed()
    }

    fun cleanup(settings: AppSettings) {
        val removed = db.cleanup(settings.retentionDays, settings.maxEntries)
        if (removed > 0) JournalStore.changed()
    }

    fun findById(id: Long): JournalEntry? = db.findById(id)

    fun insert(entry: JournalEntry): Long {
        val id = db.insert(entry)
        if (id > 0) JournalStore.changed()
        return id
    }

    fun importEntries(entries: List<JournalEntry>): Int {
        val count = db.insertBatch(entries)
        if (count > 0) JournalStore.changed()
        return count
    }

    fun findByFingerprint(fingerprint: String): JournalEntry? = db.findByFingerprint(fingerprint)

    fun findRecent(packageName: String, title: String, text: String, since: Long): JournalEntry? =
        db.findRecent(packageName, title, text, since)

    fun enrich(entry: JournalEntry) {
        db.updateEnrichment(
            id = entry.id,
            title = entry.title,
            text = entry.text,
            bigText = entry.bigText,
            subText = entry.subText,
            category = entry.category,
            channelId = entry.channelId,
            channelName = entry.channelName,
            groupKey = entry.groupKey,
            conversationTitle = entry.conversationTitle,
            messagesJson = entry.messagesJson,
            actionsJson = entry.actionsJson,
            extrasJson = entry.extrasJson,
            imagePaths = entry.imagePaths,
            source = entry.source,
            capturedAt = entry.capturedAt,
            fingerprint = entry.fingerprint
        )
        JournalStore.changed()
    }

    fun setExportPath(id: Long, path: String) {
        db.setExportPath(id, path)
        JournalStore.changed()
    }

    fun setImagePaths(id: Long, paths: String) {
        db.setImagePaths(id, paths)
        JournalStore.changed()
    }
}
