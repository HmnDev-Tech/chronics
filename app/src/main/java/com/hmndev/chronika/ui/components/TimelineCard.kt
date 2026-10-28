package com.hmndev.chronika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.EntryFormat
import com.hmndev.chronika.util.TextTools
import com.hmndev.chronika.util.TimeTools

@Composable
fun TimelineEntry(
    entry: JournalEntry,
    index: Int,
    onClick: (JournalEntry) -> Unit,
    onPin: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember(entry.id) { MutableInteractionSource() }
    val offset = when (index % 5) {
        2 -> 8.dp
        4 -> 4.dp
        else -> 0.dp
    }
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Column(
            modifier = Modifier.width(52.dp).padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = TimeTools.time(entry.postedAt),
                style = MaterialTheme.typography.labelSmall,
                color = if (entry.isRead) scheme.onSurfaceVariant else scheme.primary
            )
            Spacer(Modifier.height(7.dp))
            Box(
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (entry.isPinned) Lime else scheme.primary.copy(alpha = 0.55f))
            )
            Box(
                Modifier
                    .width(1.dp)
                    .height(72.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.7f))
            )
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp, start = offset)
                .pressScale(interaction)
                .clickable(
                    interactionSource = interaction,
                    indication = null
                ) { onClick(entry) },
            shape = RoundedCornerShape(28.dp),
            color = if (entry.isPinned) {
                scheme.primaryContainer.copy(alpha = 0.72f)
            } else {
                scheme.surfaceContainerLow
            },
            tonalElevation = 0.dp,
            shadowElevation = if (entry.isPinned) 0.dp else 1.dp
        ) {
            Box {
                if (entry.isPinned) {
                    Box(
                        Modifier
                            .padding(start = 0.dp, top = 18.dp, bottom = 18.dp)
                            .width(4.dp)
                            .height(48.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 4.dp,
                                    bottomEnd = 4.dp,
                                    bottomStart = 0.dp
                                )
                            )
                            .background(scheme.primary)
                            .align(Alignment.CenterStart)
                    )
                }
                Column(Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            packageName = entry.packageName,
                            label = entry.appLabel,
                            size = 38.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = entry.displayTitle.ifBlank { entry.appLabel },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = scheme.onSurface
                            )
                            Text(
                                text = entry.appLabel + if (entry.conversationTitle.isNotBlank()) {
                                    " · ${entry.conversationTitle}"
                                } else {
                                    ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { onPin(entry) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = if (entry.isPinned) "Открепить" else "Закрепить",
                                modifier = Modifier.size(16.dp),
                                tint = if (entry.isPinned) scheme.primary else scheme.outline
                            )
                        }
                    }
                    if (entry.displayBody.isNotBlank()) {
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = TextTools.compactPreview(entry.displayBody, 220),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = if (entry.hasMessages) 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val messages = if (entry.hasMessages) EntryFormat.messages(entry).takeLast(2) else emptyList()
                    if (messages.isNotEmpty()) {
                        Spacer(Modifier.height(7.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            messages.forEach { message ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        Modifier
                                            .padding(top = 6.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(scheme.tertiary)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = buildString {
                                            if (message.sender.isNotBlank()) append("${message.sender}: ")
                                            append(message.text)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaPill(
                            text = EntryFormat.sourceLabel(entry.source),
                            icon = Icons.Rounded.GraphicEq,
                            container = scheme.surfaceContainerHigh,
                            content = scheme.onSurfaceVariant
                        )
                        if (entry.isOngoing) {
                            MetaPill(text = "постоянное", container = scheme.tertiaryContainer, content = scheme.onTertiaryContainer)
                        }
                        if (entry.isSilent) {
                            MetaPill(text = "тихое", container = scheme.surfaceContainerHighest, content = scheme.onSurfaceVariant)
                        }
                        if (entry.exportPath.isNotBlank()) {
                            MetaPill(
                                text = "в Downloads",
                                icon = Icons.Rounded.DownloadDone,
                                container = scheme.primaryContainer,
                                content = scheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        if (!entry.isRead) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(scheme.tertiary)
                            )
                        }
                    }
                }
            }
        }
    }
}
