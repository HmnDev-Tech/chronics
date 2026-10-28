package com.hmndev.chronika.util

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap
import org.json.JSONArray
import org.json.JSONObject

data class ParsedMessage(
    val sender: String,
    val text: String,
    val time: Long
)

data class ParsedNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val subText: String,
    val category: String,
    val channelId: String,
    val channelName: String,
    val groupKey: String,
    val conversationTitle: String,
    val messages: List<ParsedMessage>,
    val actions: List<String>,
    val extrasJson: String,
    val images: List<Bitmap>,
    val postedAt: Long,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val priority: Int,
    val isSilent: Boolean,
    val fingerprint: String
) {
    val messagesJson: String
        get() {
            val array = JSONArray()
            messages.forEach { message ->
                array.put(
                    JSONObject().apply {
                        put("sender", message.sender)
                        put("text", message.text)
                        put("time", message.time)
                    }
                )
            }
            return array.toString()
        }

    val actionsJson: String
        get() {
            val array = JSONArray()
            actions.forEach { array.put(it) }
            return array.toString()
        }
}

object NotificationParser {
    private val skipExtraKeys = setOf(
        "android.picture",
        "android.largeIcon",
        "android.icon",
        "android.messages",
        "android.messagingStyleUser",
        "android.people",
        "android.people.list",
        "android.selfDisplayName",
        "android.template",
        "android.wearable.EXTENSIONS",
        "android.progress",
        "android.progressMax",
        "android.progressIndeterminate"
    )

    fun parse(context: Context, sbn: StatusBarNotification, dedupe: Boolean): ParsedNotification {
        val notification = sbn.notification
        val extras = notification.extras ?: Bundle()
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val conversationTitle =
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString().orEmpty()
        val messages = readMessages(extras)
        val actions = notification.actions
            ?.mapNotNull { it.title?.toString()?.takeIf { title -> title.isNotBlank() } }
            ?: emptyList()
        val channelName = runCatching {
            context.getSystemService(android.app.NotificationManager::class.java)
                ?.getNotificationChannel(notification.channelId)?.name?.toString()
        }.getOrNull().orEmpty()
        val images = readImages(context, notification, extras)
        val postedAt = sbn.postTime
        val timeKey = when {
            messages.isNotEmpty() -> messages.last().time.toString()
            dedupe -> (postedAt / 60_000L).toString()
            else -> postedAt.toString()
        }
        val fingerprint = Fingerprint.of(
            sbn.packageName,
            title,
            text,
            bigText,
            conversationTitle,
            messages.joinToString("|") { "${it.sender}:${it.text}:${it.time}" },
            timeKey
        )
        return ParsedNotification(
            packageName = sbn.packageName,
            title = title,
            text = text,
            bigText = bigText,
            subText = subText,
            category = notification.category.orEmpty(),
            channelId = notification.channelId.orEmpty(),
            channelName = channelName,
            groupKey = notification.group.orEmpty(),
            conversationTitle = conversationTitle,
            messages = messages,
            actions = actions,
            extrasJson = extrasToJson(extras),
            images = images,
            postedAt = postedAt,
            isOngoing = sbn.isOngoing,
            isClearable = sbn.isClearable,
            priority = notification.priority,
            isSilent = notification.priority <= Notification.PRIORITY_LOW,
            fingerprint = fingerprint
        )
    }

    private fun readMessages(extras: Bundle): List<ParsedMessage> {
        val raw = extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return emptyList()
        val result = ArrayList<ParsedMessage>()
        raw.forEach { item ->
            val bundle = item as? Bundle ?: return@forEach
            val sender = bundle.getCharSequence("sender")?.toString()
                ?: senderFromPerson(bundle)
            val messageText = bundle.getCharSequence("text")?.toString().orEmpty()
            if (messageText.isBlank() && sender.isNullOrBlank()) return@forEach
            result.add(
                ParsedMessage(
                    sender = sender.orEmpty(),
                    text = messageText,
                    time = bundle.getLong("time", 0L)
                )
            )
        }
        return result
    }

    @Suppress("DEPRECATION")
    private fun senderFromPerson(bundle: Bundle): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return (bundle.get("sender_person") as? android.app.Person)?.name?.toString()
    }

    private fun readImages(
        context: Context,
        notification: Notification,
        extras: Bundle
    ): List<Bitmap> {
        val result = ArrayList<Bitmap>()
        runCatching {
            val drawable = notification.getLargeIcon()?.loadDrawable(context) ?: return@runCatching
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 256
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 256
            result.add(drawable.toBitmap(width, height))
        }
        @Suppress("DEPRECATION")
        runCatching {
            extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)?.let { result.add(it) }
        }
        return result
    }

    private fun extrasToJson(extras: Bundle): String {
        val json = JSONObject()
        extras.keySet().forEach { key ->
            if (key in skipExtraKeys) return@forEach
            val value = runCatching { extras.get(key) }.getOrNull() ?: return@forEach
            val converted = convert(value) ?: return@forEach
            runCatching { json.put(key, converted) }
        }
        return json.toString()
    }

    private fun convert(value: Any?): Any? = when (value) {
        null -> null
        is CharSequence -> value.toString()
        is Number, is Boolean -> value
        is Bundle -> {
            val nested = JSONObject()
            value.keySet().forEach { key ->
                convert(runCatching { value.get(key) }.getOrNull())?.let { nested.put(key, it) }
            }
            nested
        }
        is Array<*> -> {
            val array = JSONArray()
            value.forEach { item -> convert(item)?.let { array.put(it) } }
            array
        }
        is IntArray -> JSONArray(value.toList())
        is LongArray -> JSONArray(value.toList())
        is DoubleArray -> JSONArray(value.toList())
        is BooleanArray -> JSONArray(value.toList())
        is Bitmap -> null
        else -> value.toString()
    }
}
