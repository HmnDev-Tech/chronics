package com.hmndev.chronika.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import com.hmndev.chronika.MainActivity
import com.hmndev.chronika.R
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalRepository
import com.hmndev.chronika.data.SettingsStore
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.util.CaptureImageCache
import com.hmndev.chronika.util.ExportManager
import com.hmndev.chronika.util.Fingerprint
import com.hmndev.chronika.util.NotificationParser
import com.hmndev.chronika.util.ParsedNotification
import com.hmndev.chronika.util.TextTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object CaptureEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val labels = ConcurrentHashMap<String, String>()

    fun captureNotification(context: Context, sbn: StatusBarNotification) {
        val settings = SettingsStore.current()
        if (!settings.captureEnabled || !settings.captureListener) return
        if (sbn.packageName == context.packageName &&
            sbn.notification.channelId != "chronika_test"
        ) return
        if (!settings.appAllowed(sbn.packageName)) return

        val parsed = NotificationParser.parse(context, sbn, settings.dedupe)
        if (parsed.isOngoing && settings.ignoreOngoing) return
        if (parsed.isSilent && settings.ignoreSilent) return
        if (parsed.priority < settings.minPriority) return
        val haystack = "${parsed.title} ${parsed.text} ${parsed.bigText} ${parsed.conversationTitle} " +
            parsed.messages.joinToString(" ") { it.text }
        if (!settings.keywordAllowed(haystack)) return

        val app = context.applicationContext
        scope.launch {
            storeNotification(app, parsed, settings)
        }
    }

    fun captureAccessibility(
        context: Context,
        packageName: String,
        texts: List<String>,
        contentDescription: String?
    ) {
        val settings = SettingsStore.current()
        if (!settings.captureEnabled || !settings.captureAccessibility) return
        if (packageName == context.packageName) return
        if (!settings.appAllowed(packageName)) return
        val cleaned = texts.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (cleaned.isEmpty()) return
        val title = cleaned.first()
        val body = cleaned.drop(1).joinToString(" · ")
            .ifBlank { contentDescription?.trim().orEmpty() }
        if (title.length < 2 && body.isBlank()) return
        if (!settings.keywordAllowed("$title $body")) return

        val now = System.currentTimeMillis()
        val fingerprint = Fingerprint.of(
            packageName,
            title,
            body,
            if (settings.dedupe) (now / 60_000L).toString() else now.toString()
        )
        val app = context.applicationContext
        scope.launch {
            val repo = JournalRepository(app)
            if (repo.findByFingerprint(fingerprint) != null) return@launch
            if (repo.findRecent(packageName, title, body, now - 120_000L) != null) return@launch
            val entry = JournalEntry(
                packageName = packageName,
                appLabel = label(app, packageName),
                title = mask(title, settings),
                text = mask(body, settings),
                bigText = "",
                subText = "",
                category = "accessibility",
                channelId = "accessibility",
                channelName = S["capture.accessibility_channel"],
                groupKey = "",
                conversationTitle = "",
                messagesJson = "[]",
                actionsJson = "[]",
                extrasJson = "{}",
                imagePaths = "[]",
                source = "accessibility",
                postedAt = now,
                capturedAt = now,
                isOngoing = false,
                isClearable = true,
                priority = 0,
                isSilent = false,
                fingerprint = fingerprint
            )
            val id = repo.insert(entry)
            if (id > 0 && settings.autoExport) {
                exportNow(app, repo, id, settings)
            }
        }
    }

    fun importDump(context: Context, dump: String): Int {
        val settings = SettingsStore.current()
        val app = context.applicationContext
        val repo = JournalRepository(app)
        var imported = 0
        parseDump(dump).take(500).forEach { record ->
            if (record.packageName.isBlank()) return@forEach
            if (record.packageName == app.packageName) return@forEach
            if (!settings.appAllowed(record.packageName)) return@forEach
            if (!settings.keywordAllowed("${record.title} ${record.text} ${record.bigText}")) return@forEach
            val postedAt = if (record.postedAt > 0) record.postedAt else System.currentTimeMillis()
            val fingerprint = Fingerprint.of(
                record.packageName,
                record.title,
                record.text,
                record.bigText,
                "shizuku",
                (postedAt / 60_000L).toString()
            )
            if (repo.findByFingerprint(fingerprint) != null) return@forEach
            val entry = JournalEntry(
                packageName = record.packageName,
                appLabel = label(app, record.packageName),
                title = mask(record.title, settings),
                text = mask(record.text, settings),
                bigText = mask(record.bigText, settings),
                subText = "",
                category = "shizuku",
                channelId = "shizuku",
                channelName = S["capture.shizuku_channel"],
                groupKey = "",
                conversationTitle = "",
                messagesJson = "[]",
                actionsJson = "[]",
                extrasJson = "{}",
                imagePaths = "[]",
                source = "shizuku",
                postedAt = postedAt,
                capturedAt = System.currentTimeMillis(),
                isOngoing = false,
                isClearable = true,
                priority = 0,
                isSilent = false,
                fingerprint = fingerprint
            )
            val id = repo.insert(entry)
            if (id > 0) {
                imported += 1
                if (settings.autoExport) exportNow(app, repo, id, settings)
            }
        }
        repo.cleanup(settings)
        return imported
    }

    fun postTestNotification(context: Context): Boolean {
        return try {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "chronika_test",
                    S["capture.test_channel"],
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = S["capture.test_channel_desc"]
                }
                manager.createNotificationChannel(channel)
            }
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = Notification.Builder(context, "chronika_test")
                .setSmallIcon(R.drawable.ic_stat_chronika)
                .setContentTitle(S["capture.test_title"])
                .setContentText(S["capture.test_text"])
                .setStyle(
                    Notification.BigTextStyle().bigText(
                        S["capture.test_big"]
                    )
                )
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
            manager.notify(9001, notification)
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun storeNotification(
        context: Context,
        parsed: ParsedNotification,
        settings: AppSettings
    ) {
        val repo = JournalRepository(context)
        val masked = maskParsed(parsed, settings)
        val now = System.currentTimeMillis()
        val existing = repo.findByFingerprint(masked.fingerprint)
        val base = JournalEntry(
            packageName = masked.packageName,
            appLabel = label(context, masked.packageName),
            title = masked.title,
            text = masked.text,
            bigText = masked.bigText,
            subText = masked.subText,
            category = masked.category,
            channelId = masked.channelId,
            channelName = masked.channelName,
            groupKey = masked.groupKey,
            conversationTitle = masked.conversationTitle,
            messagesJson = masked.messagesJson,
            actionsJson = masked.actionsJson,
            extrasJson = if (settings.saveRawExtras) masked.extrasJson else "{}",
            imagePaths = "[]",
            source = "listener",
            postedAt = masked.postedAt,
            capturedAt = now,
            isOngoing = masked.isOngoing,
            isClearable = masked.isClearable,
            priority = masked.priority,
            isSilent = masked.isSilent,
            fingerprint = masked.fingerprint
        )
        if (masked.images.isNotEmpty()) {
            CaptureImageCache.put(masked.fingerprint, masked.images)
        }
        val id = if (existing != null) {
            val merged = base.copy(
                id = existing.id,
                title = base.title.ifBlank { existing.title },
                text = base.text.ifBlank { existing.text },
                bigText = base.bigText.ifBlank { existing.bigText },
                subText = base.subText.ifBlank { existing.subText },
                conversationTitle = base.conversationTitle.ifBlank { existing.conversationTitle },
                messagesJson = if (base.messagesJson == "[]") existing.messagesJson else base.messagesJson,
                actionsJson = if (base.actionsJson == "[]") existing.actionsJson else base.actionsJson,
                extrasJson = if (base.extrasJson == "{}") existing.extrasJson else base.extrasJson,
                imagePaths = existing.imagePaths,
                postedAt = existing.postedAt,
                capturedAt = now
            )
            repo.enrich(merged)
            existing.id
        } else {
            repo.insert(base)
        }
        if (id > 0 && settings.autoExport) {
            exportNow(context, repo, id, settings)
        }
        repo.cleanup(settings)
    }

    private fun exportNow(
        context: Context,
        repo: JournalRepository,
        id: Long,
        settings: AppSettings
    ) {
        val fresh = repo.findById(id) ?: return
        scope.launch {
            val result = ExportManager.export(context, fresh, settings)
            if (result.imagePaths.isNotEmpty()) {
                repo.setImagePaths(
                    id,
                    org.json.JSONArray().apply { result.imagePaths.forEach { put(it) } }.toString()
                )
            }
            if (result.jsonPath.isNotBlank() || result.txtPath.isNotBlank()) {
                repo.setExportPath(id, result.jsonPath.ifBlank { result.txtPath })
            }
        }
    }

    private fun maskParsed(parsed: ParsedNotification, settings: AppSettings): ParsedNotification {
        if (!settings.maskSecrets) return parsed
        return parsed.copy(
            title = TextTools.maskSecrets(parsed.title),
            text = TextTools.maskSecrets(parsed.text),
            bigText = TextTools.maskSecrets(parsed.bigText),
            subText = TextTools.maskSecrets(parsed.subText),
            conversationTitle = TextTools.maskSecrets(parsed.conversationTitle),
            messages = parsed.messages.map { it.copy(text = TextTools.maskSecrets(it.text)) }
        )
    }

    private fun mask(value: String, settings: AppSettings): String =
        if (settings.maskSecrets) TextTools.maskSecrets(value) else value

    private fun label(context: Context, packageName: String): String {
        return labels.getOrPut(packageName) {
            runCatching {
                val info = context.packageManager.getApplicationInfo(packageName, 0)
                context.packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(packageName)
        }
    }

    private data class DumpRecord(
        var packageName: String = "",
        var title: String = "",
        var text: String = "",
        var bigText: String = "",
        var postedAt: Long = 0L
    ) {
        fun hasContent(): Boolean =
            title.isNotBlank() || text.isNotBlank() || bigText.isNotBlank()
    }

    private fun parseDump(dump: String): List<DumpRecord> {
        val result = ArrayList<DumpRecord>()
        var current: DumpRecord? = null
        val pkgRegex = Regex("pkg=([A-Za-z0-9._]+)")
        val timeRegex = Regex("(?:postTime|when)=(\\d{10,})")
        fun flush() {
            current?.let { if (it.hasContent()) result.add(it) }
            current = null
        }
        dump.lineSequence().forEach { line ->
            if (line.contains("NotificationRecord(")) {
                flush()
                current = DumpRecord(
                    packageName = pkgRegex.find(line)?.groupValues?.getOrNull(1).orEmpty()
                )
            }
            val record = current ?: return@forEach
            when {
                line.contains("android.title=") -> record.title = dumpValue(line)
                line.contains("android.text=") -> record.text = dumpValue(line)
                line.contains("android.bigText=") -> record.bigText = dumpValue(line)
                line.contains("tickerText=") && record.text.isBlank() -> record.text = dumpValue(line)
                timeRegex.containsMatchIn(line) -> {
                    timeRegex.find(line)?.groupValues?.getOrNull(1)?.toLongOrNull()?.let {
                        if (it > 0) record.postedAt = it
                    }
                }
            }
        }
        flush()
        return result
    }

    private fun dumpValue(line: String): String {
        var value = line.substringAfter('=', "").trim()
        value = value.removePrefix("String (").removePrefix("SpannableString (")
        if (value.endsWith(")")) value = value.dropLast(1)
        if (value == "null") return ""
        return value.trim()
    }
}
