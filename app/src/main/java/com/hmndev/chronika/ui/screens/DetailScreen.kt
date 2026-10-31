package com.hmndev.chronika.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.components.AppIcon
import com.hmndev.chronika.ui.components.MetaPill
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.EntryFormat
import com.hmndev.chronika.util.TextTools
import com.hmndev.chronika.util.TimeTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DetailScreen(
    entry: JournalEntry,
    showRawByDefault: Boolean = false,
    onBack: () -> Unit,
    onPin: () -> Unit,
    onToggleRead: () -> Unit = {},
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onCopied: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    var showRaw by rememberSaveable(entry.id) { mutableStateOf(showRawByDefault) }
    val messages = remember(entry.messagesJson) { EntryFormat.messages(entry) }
    val actions = remember(entry.actionsJson) { EntryFormat.actions(entry) }
    val images = remember(entry.imagePaths) { EntryFormat.images(entry) }
    val canOpenApp = remember(entry.packageName) {
        runCatching {
            context.packageManager.getLaunchIntentForPackage(entry.packageName) != null
        }.getOrDefault(false)
    }
    val rawFormatted = remember(entry.extrasJson) {
        TextTools.prettyJson(entry.extrasJson.ifBlank { "{}" })
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (showRaw) 180f else 0f,
        label = "rawExpand"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = device.dp(10.dp), vertical = device.dp(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = S["common.back"])
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleRead) {
                    Icon(
                        imageVector = if (entry.isRead) {
                            Icons.Rounded.MarkEmailUnread
                        } else {
                            Icons.Rounded.MarkEmailRead
                        },
                        contentDescription = if (entry.isRead) {
                            S["detail.mark_unread"]
                        } else {
                            S["detail.mark_read"]
                        },
                        tint = if (!entry.isRead) scheme.primary else scheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onPin) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = if (entry.isPinned) S["common.unpin"] else S["common.pin"],
                        tint = if (entry.isPinned) scheme.primary else scheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = if (entry.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                        contentDescription = if (entry.isArchived) S["detail.unarchive"] else S["detail.archive"],
                        tint = if (entry.isArchived) scheme.tertiary else scheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { shareEntry(context, entry) }) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = S["detail.share"],
                        tint = scheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showDelete = true }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = S["common.delete"],
                        tint = scheme.error
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = device.screenPadding,
                    end = device.screenPadding,
                    bottom = device.dp(20.dp)
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "header") {
                    Row(verticalAlignment = Alignment.Top) {
                        AppIcon(
                            packageName = entry.packageName,
                            label = entry.appLabel,
                            size = 54.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = entry.appLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.primary
                            )
                            Text(
                                text = entry.displayTitle.ifBlank { S["common.no_title"] },
                                style = MaterialTheme.typography.headlineMedium,
                                color = scheme.onSurface
                            )
                            Text(
                                text = TimeTools.full(entry.postedAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        if (canOpenApp) {
                            OutlinedButton(
                                onClick = { openSourceApp(context, entry.packageName) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = S["detail.open_app"],
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                item(key = "chips") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetaPill(
                            text = EntryFormat.sourceLabel(entry.source),
                            container = scheme.primaryContainer,
                            content = scheme.onPrimaryContainer
                        )
                        MetaPill(
                            text = EntryFormat.importanceLabel(entry.priority),
                            container = scheme.surfaceContainerHigh,
                            content = scheme.onSurfaceVariant
                        )
                        if (entry.isPinned) {
                            MetaPill(
                                text = S["detail.pinned"],
                                container = scheme.primaryContainer,
                                content = scheme.onPrimaryContainer
                            )
                        }
                        if (entry.isArchived) {
                            MetaPill(
                                text = S["detail.archived"],
                                container = scheme.secondaryContainer,
                                content = scheme.onSecondaryContainer
                            )
                        }
                        if (entry.isOngoing) {
                            MetaPill(
                                text = S["detail.ongoing"],
                                container = scheme.tertiaryContainer,
                                content = scheme.onTertiaryContainer
                            )
                        }
                    }
                }

                if (entry.conversationTitle.isNotBlank()) {
                    item(key = "conversation") {
                        Text(
                            text = S.f("detail.conversation", entry.conversationTitle),
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.tertiary
                        )
                    }
                }

                if (entry.displayBody.isNotBlank()) {
                    item(key = "body") {
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = scheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SelectionContainer {
                                Text(
                                    text = entry.displayBody,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(18.dp),
                                    color = scheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (messages.isNotEmpty()) {
                    item(key = "messagesTitle") {
                        Text(
                            text = S.f("detail.messages", messages.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    items(messages, key = { "${it.time}-${it.sender}-${it.text.hashCode()}" }) { message ->
                        MessageTape(message)
                    }
                }

                if (images.isNotEmpty()) {
                    item(key = "imagesTitle") {
                        Text(
                            text = S.f("detail.attachments", images.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    items(images, key = { it }) { path ->
                        LocalImage(path)
                    }
                }

                if (actions.isNotEmpty()) {
                    item(key = "actions") {
                        Column {
                            Text(
                                text = S["detail.actions"],
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                actions.forEach { action ->
                                    MetaPill(
                                        text = action,
                                        container = scheme.secondaryContainer,
                                        content = scheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "meta") {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = scheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MetaRow(S["detail.package"], entry.packageName)
                            MetaRow(S["detail.channel"], entry.channelName.ifBlank { entry.channelId })
                            MetaRow(S["detail.category"], entry.category.ifBlank { "—" })
                            MetaRow(S["detail.captured"], TimeTools.full(entry.capturedAt))
                            if (entry.exportPath.isNotBlank()) {
                                MetaRow(S["detail.file"], entry.exportPath)
                            }
                            MetaRow(S["detail.fingerprint"], entry.fingerprint.take(24) + "…")
                        }
                    }
                }

                item(key = "raw") {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = scheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRaw = !showRaw },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = S["detail.raw"],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showRaw = !showRaw }) {
                                    Icon(
                                        imageVector = Icons.Rounded.ExpandMore,
                                        contentDescription = S["detail.show"],
                                        tint = scheme.onSurfaceVariant,
                                        modifier = Modifier.rotate(chevronRotation)
                                    )
                                }
                            }
                            if (showRaw) {
                                SelectionContainer {
                                    Text(
                                        text = rawFormatted,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        color = scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = device.screenPadding, vertical = device.dp(10.dp)),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        copyToClipboard(context, entry)
                        onCopied()
                    },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(S["detail.copy"])
                }
                Button(
                    onClick = onExport,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(S["detail.downloads"])
                }
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(S["detail.delete_title"]) },
            text = { Text(S["detail.delete_body"]) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onDelete()
                    }
                ) { Text(S["common.delete"], color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text(S["common.cancel"]) }
            }
        )
    }
}

@Composable
private fun MessageTape(message: com.hmndev.chronika.util.MessageView) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Lime)
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.sender.ifBlank { S["common.unnamed"] },
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    if (message.time > 0) {
                        Text(
                            text = TimeTools.time(message.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                SelectionContainer {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(86.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalImage(path: String) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (path.startsWith("content://") || path.startsWith("file://")) {
                    val uri = Uri.parse(path)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } else {
                    val file = File(path)
                    if (file.isFile) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else {
                        null
                    }
                }
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = S["common.attachment"],
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        )
    } else {
        Text(
            text = path,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatShareText(entry: JournalEntry): String = buildString {
    appendLine("${entry.appLabel} · ${entry.displayTitle}")
    appendLine(TimeTools.full(entry.postedAt))
    if (entry.displayBody.isNotBlank()) {
        appendLine()
        appendLine(entry.displayBody)
    }
    val messages = EntryFormat.messages(entry)
    if (messages.isNotEmpty()) {
        appendLine()
        messages.forEach { appendLine("${it.sender.ifBlank { S["common.unnamed"] }}: ${it.text}") }
    }
}.trimEnd()

private fun copyToClipboard(context: Context, entry: JournalEntry) {
    val text = formatShareText(entry)
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(S["app.name"], text))
}

private fun shareEntry(context: Context, entry: JournalEntry) {
    runCatching {
        val text = formatShareText(entry)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${entry.appLabel} · ${entry.displayTitle}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(send, S["detail.share"]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

private fun openSourceApp(context: Context, packageName: String) {
    runCatching {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }
}
