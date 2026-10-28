package com.hmndev.chronika.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class JournalDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL,
                app_label TEXT NOT NULL,
                title TEXT NOT NULL,
                text TEXT NOT NULL,
                big_text TEXT NOT NULL,
                sub_text TEXT NOT NULL,
                category TEXT NOT NULL,
                channel_id TEXT NOT NULL,
                channel_name TEXT NOT NULL,
                group_key TEXT NOT NULL,
                conversation_title TEXT NOT NULL,
                messages_json TEXT NOT NULL,
                actions_json TEXT NOT NULL,
                extras_json TEXT NOT NULL,
                image_paths TEXT NOT NULL,
                source TEXT NOT NULL,
                posted_at INTEGER NOT NULL,
                captured_at INTEGER NOT NULL,
                is_ongoing INTEGER NOT NULL,
                is_clearable INTEGER NOT NULL,
                priority INTEGER NOT NULL,
                is_silent INTEGER NOT NULL,
                is_read INTEGER NOT NULL,
                is_pinned INTEGER NOT NULL,
                is_archived INTEGER NOT NULL,
                fingerprint TEXT NOT NULL,
                export_path TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX idx_fingerprint ON $TABLE(fingerprint)")
        db.execSQL("CREATE INDEX idx_posted ON $TABLE(posted_at DESC)")
        db.execSQL("CREATE INDEX idx_package ON $TABLE(package_name)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE")
            onCreate(db)
        }
    }

    @Synchronized
    fun insert(entry: JournalEntry): Long {
        val db = writableDatabase
        return db.insertWithOnConflict(TABLE, null, valuesOf(entry), SQLiteDatabase.CONFLICT_IGNORE)
    }

    @Synchronized
    fun updateEnrichment(
        id: Long,
        title: String,
        text: String,
        bigText: String,
        subText: String,
        conversationTitle: String,
        messagesJson: String,
        actionsJson: String,
        extrasJson: String,
        imagePaths: String,
        channelName: String,
        capturedAt: Long
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("text", text)
            put("big_text", bigText)
            put("sub_text", subText)
            put("conversation_title", conversationTitle)
            put("messages_json", messagesJson)
            put("actions_json", actionsJson)
            put("extras_json", extrasJson)
            put("image_paths", imagePaths)
            put("channel_name", channelName)
            put("captured_at", capturedAt)
        }
        db.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun setExportPath(id: Long, path: String) {
        val values = ContentValues().apply { put("export_path", path) }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun setImagePaths(id: Long, paths: String) {
        val values = ContentValues().apply { put("image_paths", paths) }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun findById(id: Long): JournalEntry? {
        readableDatabase.query(
            TABLE, null, "id = ?", arrayOf(id.toString()),
            null, null, null, "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toEntry() else null
        }
    }

    @Synchronized
    fun findByFingerprint(fingerprint: String): JournalEntry? {
        readableDatabase.query(
            TABLE, null, "fingerprint = ?", arrayOf(fingerprint),
            null, null, null, "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toEntry() else null
        }
    }

    @Synchronized
    fun findRecent(packageName: String, title: String, text: String, since: Long): JournalEntry? {
        readableDatabase.query(
            TABLE, null,
            "package_name = ? AND title = ? AND text = ? AND posted_at >= ?",
            arrayOf(packageName, title, text, since.toString()),
            null, null, "posted_at DESC", "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toEntry() else null
        }
    }

    @Synchronized
    fun query(limit: Int = 5000): List<JournalEntry> {
        val result = ArrayList<JournalEntry>()
        readableDatabase.query(
            TABLE, null, null, null, null, null, "posted_at DESC", limit.toString()
        ).use { cursor ->
            while (cursor.moveToNext()) result.add(cursor.toEntry())
        }
        return result
    }

    @Synchronized
    fun stats(): JournalStats {
        val total = scalar("SELECT COUNT(*) FROM $TABLE")
        val exported = scalar("SELECT COUNT(*) FROM $TABLE WHERE export_path != ''")
        val unread = scalar("SELECT COUNT(*) FROM $TABLE WHERE is_read = 0")
        val pinned = scalar("SELECT COUNT(*) FROM $TABLE WHERE is_pinned = 1")
        val apps = scalar("SELECT COUNT(DISTINCT package_name) FROM $TABLE")
        val oldest = scalarLong("SELECT COALESCE(MIN(posted_at), 0) FROM $TABLE")
        val newest = scalarLong("SELECT COALESCE(MAX(posted_at), 0) FROM $TABLE")
        val todayStart = startOfToday()
        val today = scalar("SELECT COUNT(*) FROM $TABLE WHERE posted_at >= $todayStart")
        val hours = MutableList(24) { 0 }
        readableDatabase.rawQuery(
            "SELECT posted_at FROM $TABLE WHERE posted_at >= ?",
            arrayOf(todayStart.toString())
        ).use { cursor ->
            val zone = ZoneId.systemDefault()
            while (cursor.moveToNext()) {
                val hour = Instant.ofEpochMilli(cursor.getLong(0)).atZone(zone).hour
                hours[hour] = hours[hour] + 1
            }
        }
        return JournalStats(
            total = total,
            today = today,
            unread = unread,
            pinned = pinned,
            apps = apps,
            exported = exported,
            oldest = oldest,
            newest = newest,
            todayByHour = hours
        )
    }

    @Synchronized
    fun topApps(limit: Int = 6): List<AppCount> {
        val result = ArrayList<AppCount>()
        readableDatabase.rawQuery(
            "SELECT package_name, app_label, COUNT(*) AS c FROM $TABLE " +
                "GROUP BY package_name ORDER BY c DESC LIMIT ?",
            arrayOf(limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(AppCount(cursor.getString(0), cursor.getString(1), cursor.getInt(2)))
            }
        }
        return result
    }

    @Synchronized
    fun dailyCounts(days: Int = 7): List<DayCount> {
        val zone = ZoneId.systemDefault()
        val labels = DateTimeFormatter.ofPattern("dd.MM", Locale("ru"))
        val counts = LinkedHashMap<LocalDate, Int>()
        val today = LocalDate.now(zone)
        for (i in days - 1 downTo 0) counts[today.minusDays(i.toLong())] = 0
        val since = today.minusDays((days - 1).toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
        readableDatabase.rawQuery(
            "SELECT posted_at FROM $TABLE WHERE posted_at >= ?",
            arrayOf(since.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val date = Instant.ofEpochMilli(cursor.getLong(0)).atZone(zone).toLocalDate()
                if (counts.containsKey(date)) counts[date] = (counts[date] ?: 0) + 1
            }
        }
        return counts.map { (date, count) -> DayCount(date.toString(), labels.format(date), count) }
    }

    @Synchronized
    fun markRead(id: Long, read: Boolean = true) {
        val values = ContentValues().apply { put("is_read", if (read) 1 else 0) }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun setPinned(id: Long, pinned: Boolean) {
        val values = ContentValues().apply { put("is_pinned", if (pinned) 1 else 0) }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun setArchived(id: Long, archived: Boolean) {
        val values = ContentValues().apply { put("is_archived", if (archived) 1 else 0) }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun delete(id: Long) {
        writableDatabase.delete(TABLE, "id = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun deleteAll() {
        writableDatabase.delete(TABLE, null, null)
    }

    @Synchronized
    fun cleanup(retentionDays: Int, maxEntries: Int) {
        if (retentionDays > 0) {
            val cutoff = System.currentTimeMillis() - retentionDays.toLong() * 86_400_000L
            writableDatabase.delete(TABLE, "posted_at < ? AND is_pinned = 0", arrayOf(cutoff.toString()))
        }
        if (maxEntries > 0) {
            val total = scalar("SELECT COUNT(*) FROM $TABLE")
            if (total > maxEntries) {
                val overflow = total - maxEntries
                writableDatabase.execSQL(
                    "DELETE FROM $TABLE WHERE id IN " +
                        "(SELECT id FROM $TABLE WHERE is_pinned = 0 ORDER BY posted_at ASC LIMIT $overflow)"
                )
            }
        }
    }

    private fun scalar(sql: String): Int {
        readableDatabase.rawQuery(sql, null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun scalarLong(sql: String): Long {
        readableDatabase.rawQuery(sql, null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }
    }

    private fun startOfToday(): Long =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    private fun valuesOf(entry: JournalEntry): ContentValues = ContentValues().apply {
        put("package_name", entry.packageName)
        put("app_label", entry.appLabel)
        put("title", entry.title)
        put("text", entry.text)
        put("big_text", entry.bigText)
        put("sub_text", entry.subText)
        put("category", entry.category)
        put("channel_id", entry.channelId)
        put("channel_name", entry.channelName)
        put("group_key", entry.groupKey)
        put("conversation_title", entry.conversationTitle)
        put("messages_json", entry.messagesJson)
        put("actions_json", entry.actionsJson)
        put("extras_json", entry.extrasJson)
        put("image_paths", entry.imagePaths)
        put("source", entry.source)
        put("posted_at", entry.postedAt)
        put("captured_at", entry.capturedAt)
        put("is_ongoing", if (entry.isOngoing) 1 else 0)
        put("is_clearable", if (entry.isClearable) 1 else 0)
        put("priority", entry.priority)
        put("is_silent", if (entry.isSilent) 1 else 0)
        put("is_read", if (entry.isRead) 1 else 0)
        put("is_pinned", if (entry.isPinned) 1 else 0)
        put("is_archived", if (entry.isArchived) 1 else 0)
        put("fingerprint", entry.fingerprint)
        put("export_path", entry.exportPath)
    }

    private fun Cursor.toEntry(): JournalEntry = JournalEntry(
        id = getLong(getColumnIndexOrThrow("id")),
        packageName = getString(getColumnIndexOrThrow("package_name")),
        appLabel = getString(getColumnIndexOrThrow("app_label")),
        title = getString(getColumnIndexOrThrow("title")),
        text = getString(getColumnIndexOrThrow("text")),
        bigText = getString(getColumnIndexOrThrow("big_text")),
        subText = getString(getColumnIndexOrThrow("sub_text")),
        category = getString(getColumnIndexOrThrow("category")),
        channelId = getString(getColumnIndexOrThrow("channel_id")),
        channelName = getString(getColumnIndexOrThrow("channel_name")),
        groupKey = getString(getColumnIndexOrThrow("group_key")),
        conversationTitle = getString(getColumnIndexOrThrow("conversation_title")),
        messagesJson = getString(getColumnIndexOrThrow("messages_json")),
        actionsJson = getString(getColumnIndexOrThrow("actions_json")),
        extrasJson = getString(getColumnIndexOrThrow("extras_json")),
        imagePaths = getString(getColumnIndexOrThrow("image_paths")),
        source = getString(getColumnIndexOrThrow("source")),
        postedAt = getLong(getColumnIndexOrThrow("posted_at")),
        capturedAt = getLong(getColumnIndexOrThrow("captured_at")),
        isOngoing = getInt(getColumnIndexOrThrow("is_ongoing")) == 1,
        isClearable = getInt(getColumnIndexOrThrow("is_clearable")) == 1,
        priority = getInt(getColumnIndexOrThrow("priority")),
        isSilent = getInt(getColumnIndexOrThrow("is_silent")) == 1,
        isRead = getInt(getColumnIndexOrThrow("is_read")) == 1,
        isPinned = getInt(getColumnIndexOrThrow("is_pinned")) == 1,
        isArchived = getInt(getColumnIndexOrThrow("is_archived")) == 1,
        fingerprint = getString(getColumnIndexOrThrow("fingerprint")),
        exportPath = getString(getColumnIndexOrThrow("export_path"))
    )

    companion object {
        private const val DB_NAME = "chronika.db"
        private const val DB_VERSION = 1
        private const val TABLE = "entries"

        @Volatile
        private var instance: JournalDb? = null

        fun get(context: Context): JournalDb {
            return instance ?: synchronized(this) {
                instance ?: JournalDb(context).also { instance = it }
            }
        }
    }
}
