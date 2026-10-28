package com.hmndev.chronika.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class ExportResult(
    val jsonPath: String = "",
    val txtPath: String = "",
    val imagePaths: List<String> = emptyList(),
    val error: String = ""
) {
    val ok: Boolean get() = error.isBlank() && (jsonPath.isNotBlank() || txtPath.isNotBlank())
}

object ExportManager {
    suspend fun export(
        context: Context,
        entry: JournalEntry,
        settings: AppSettings
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val folder = settings.folderName.ifBlank { "Chronika" }
            val day = TimeTools.fileDay(entry.postedAt)
            val base = "${entry.postedAt}_${safe(entry.packageName)}_${entry.id}"
            val images = if (settings.saveImages) {
                saveImages(context, entry, folder, base)
            } else {
                emptyList()
            }
            var jsonPath = ""
            var txtPath = ""
            if (settings.exportJson) {
                val json = entryJson(entry, images)
                jsonPath = writeDownload(
                    context,
                    "$folder/$day",
                    "$base.json",
                    "application/json",
                    json.toString(2).toByteArray(Charsets.UTF_8)
                )
            }
            if (settings.exportTxt) {
                txtPath = writeDownload(
                    context,
                    "$folder/$day",
                    "$base.txt",
                    "text/plain",
                    entryText(entry, images).toByteArray(Charsets.UTF_8)
                )
            }
            ExportResult(jsonPath = jsonPath, txtPath = txtPath, imagePaths = images)
        } catch (t: Throwable) {
            ExportResult(error = t.message ?: t.javaClass.simpleName)
        }
    }

    suspend fun exportAll(
        context: Context,
        entries: List<JournalEntry>,
        settings: AppSettings
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val folder = settings.folderName.ifBlank { "Chronika" }
            val stamp = TimeTools.nowStamp()
            val root = JSONObject().apply {
                put("app", "Chronika")
                put("format", 1)
                put("exportedAt", TimeTools.iso(System.currentTimeMillis()))
                put("count", entries.size)
                put("entries", JSONArray().apply {
                    entries.forEach { put(entryJson(it, emptyList())) }
                })
            }
            val jsonPath = writeDownload(
                context,
                "$folder/backup",
                "chronika-$stamp.json",
                "application/json",
                root.toString(2).toByteArray(Charsets.UTF_8)
            )
            val text = buildString {
                appendLine("ХРОНИКА · ЭКСПОРТ")
                appendLine("Записей: ${entries.size}")
                appendLine("Дата: ${TimeTools.full(System.currentTimeMillis())}")
                appendLine()
                entries.forEach { entry ->
                    appendLine("=".repeat(60))
                    appendLine(entryText(entry, emptyList()))
                }
            }
            val txtPath = writeDownload(
                context,
                "$folder/backup",
                "chronika-$stamp.txt",
                "text/plain",
                text.toByteArray(Charsets.UTF_8)
            )
            ExportResult(jsonPath = jsonPath, txtPath = txtPath)
        } catch (t: Throwable) {
            ExportResult(error = t.message ?: t.javaClass.simpleName)
        }
    }

    private fun entryJson(entry: JournalEntry, images: List<String>): JSONObject {
        val messages = runCatching { JSONArray(entry.messagesJson) }.getOrElse { JSONArray() }
        val actions = runCatching { JSONArray(entry.actionsJson) }.getOrElse { JSONArray() }
        val extras = runCatching { JSONObject(entry.extrasJson) }.getOrElse { JSONObject() }
        return JSONObject().apply {
            put("id", entry.id)
            put("app", entry.appLabel)
            put("package", entry.packageName)
            put("title", entry.title)
            put("text", entry.text)
            put("bigText", entry.bigText)
            put("subText", entry.subText)
            put("conversation", entry.conversationTitle)
            put("category", entry.category)
            put("channelId", entry.channelId)
            put("channelName", entry.channelName)
            put("groupKey", entry.groupKey)
            put("source", entry.source)
            put("postedAt", TimeTools.iso(entry.postedAt))
            put("postedAtMillis", entry.postedAt)
            put("capturedAt", TimeTools.iso(entry.capturedAt))
            put("capturedAtMillis", entry.capturedAt)
            put("ongoing", entry.isOngoing)
            put("clearable", entry.isClearable)
            put("priority", entry.priority)
            put("silent", entry.isSilent)
            put("fingerprint", entry.fingerprint)
            put("messages", messages)
            put("actions", actions)
            put("images", JSONArray().apply { images.forEach { put(it) } })
            put("extras", extras)
        }
    }

    private fun entryText(entry: JournalEntry, images: List<String>): String = buildString {
        appendLine("ХРОНИКА · ${entry.appLabel}")
        appendLine("Пакет: ${entry.packageName}")
        appendLine("Время: ${TimeTools.full(entry.postedAt)}")
        appendLine("Источник: ${entry.source}")
        if (entry.channelName.isNotBlank()) appendLine("Канал: ${entry.channelName}")
        if (entry.category.isNotBlank()) appendLine("Категория: ${entry.category}")
        if (entry.conversationTitle.isNotBlank()) appendLine("Беседа: ${entry.conversationTitle}")
        appendLine("Статус: ${if (entry.isOngoing) "постоянное" else "обычное"}, " +
            "${if (entry.isClearable) "снимаемое" else "неснимаемое"}")
        appendLine("-".repeat(60))
        if (entry.title.isNotBlank()) appendLine(entry.title)
        if (entry.displayBody.isNotBlank()) appendLine(entry.displayBody)
        val messages = runCatching { JSONArray(entry.messagesJson) }.getOrElse { JSONArray() }
        if (messages.length() > 0) {
            appendLine()
            appendLine("СООБЩЕНИЯ")
            for (i in 0 until messages.length()) {
                val message = messages.optJSONObject(i) ?: continue
                val sender = message.optString("sender")
                val text = message.optString("text")
                val time = message.optLong("time")
                appendLine("• ${if (sender.isBlank()) "Без имени" else sender}" +
                    if (time > 0) " · ${TimeTools.time(time)}" else "")
                if (text.isNotBlank()) appendLine("  $text")
            }
        }
        val actions = runCatching { JSONArray(entry.actionsJson) }.getOrElse { JSONArray() }
        if (actions.length() > 0) {
            appendLine()
            appendLine("ДЕЙСТВИЯ: " + (0 until actions.length()).joinToString(" · ") {
                actions.optString(it)
            })
        }
        if (images.isNotEmpty()) {
            appendLine()
            appendLine("ВЛОЖЕНИЯ:")
            images.forEach { appendLine(it) }
        }
        appendLine()
        appendLine("Отпечаток: ${entry.fingerprint}")
    }

    private fun saveImages(
        context: Context,
        entry: JournalEntry,
        folder: String,
        base: String
    ): List<String> {
        val paths = ArrayList<String>()
        val parsed = runCatching { JSONArray(entry.imagePaths) }.getOrNull()
        if (parsed != null && parsed.length() > 0) {
            for (i in 0 until parsed.length()) paths.add(parsed.optString(i))
            return paths
        }
        val images = CaptureImageCache.take(entry.fingerprint)
        images.forEachIndexed { index, bitmap ->
            val path = saveBitmap(context, folder, "${base}_$index.png", bitmap)
            if (path.isNotBlank()) paths.add(path)
        }
        return paths
    }

    private fun saveBitmap(context: Context, folder: String, name: String, bitmap: Bitmap): String {
        return try {
            val bytes = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            writeDownload(
                context,
                "$folder/media",
                name,
                "image/png",
                bytes.toByteArray(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        } catch (t: Throwable) {
            ""
        }
    }

    private fun writeDownload(
        context: Context,
        relativeDir: String,
        name: String,
        mime: String,
        bytes: ByteArray,
        collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$relativeDir"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(collection, values)
                ?: throw IllegalStateException("MediaStore отклонил файл $name")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("Нет доступа к $name")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri.toString()
        } else {
            val downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val dir = File(downloads, relativeDir)
            if (!dir.exists() && !dir.mkdirs()) {
                throw IllegalStateException("Не удалось создать ${dir.absolutePath}")
            }
            val file = File(dir, name)
            FileOutputStream(file).use { it.write(bytes) }
            return file.absolutePath
        }
    }

    private fun safe(value: String): String =
        value.replace(Regex("[^A-Za-z0-9._-]"), "_").take(48).ifBlank { "app" }
}
