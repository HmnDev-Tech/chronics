package com.hmndev.chronika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.ui.components.ChronikaHero
import com.hmndev.chronika.ui.components.EmptyJournal
import com.hmndev.chronika.ui.components.FilterLens
import com.hmndev.chronika.ui.components.JournalFilter
import com.hmndev.chronika.ui.components.SectionHeading
import com.hmndev.chronika.ui.components.TimelineEntry
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.AccessState
import com.hmndev.chronika.util.EntryFormat
import com.hmndev.chronika.util.TextTools
import com.hmndev.chronika.util.TimeTools

@Composable
fun JournalScreen(
    entries: List<JournalEntry>,
    stats: JournalStats,
    settings: AppSettings,
    access: AccessState,
    onOpenEntry: (JournalEntry) -> Unit,
    onPin: (JournalEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onTest: () -> Unit,
    onOpenInsights: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by remember { mutableStateOf(JournalFilter.ALL) }

    val filtered = remember(entries, query, filter) {
        entries.filter { entry ->
            val matchesFilter = when (filter) {
                JournalFilter.ALL -> true
                JournalFilter.IMPORTANT -> entry.priority >= 1 || !entry.isRead
                JournalFilter.PINNED -> entry.isPinned
            }
            val matchesQuery = query.isBlank() ||
                EntryFormat.searchText(entry).contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }
    val pinned = remember(entries) { entries.filter { it.isPinned } }
    val groups = remember(filtered) {
        filtered.groupBy { TimeTools.dayKey(it.postedAt) }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "hero") {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                ChronikaHero(
                    stats = stats,
                    onOpenInsights = onOpenInsights
                )
                Spacer(Modifier.height(14.dp))
                FilterLens(
                    selected = filter,
                    onSelected = { filter = it }
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CircleShape,
                    placeholder = {
                        Text(
                            text = "Найти в архиве…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Очистить")
                            }
                        }
                    }
                )
            }
        }

        if (!access.listener || !settings.captureEnabled) {
            item(key = "captureBanner") {
                CaptureBanner(
                    listener = access.listener,
                    enabled = settings.captureEnabled,
                    onOpenSettings = onOpenSettings
                )
            }
        }

        if (pinned.isNotEmpty()) {
            item(key = "pinnedHeading") {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    Spacer(Modifier.height(12.dp))
                    SectionHeading(
                        eyebrow = "ЗАКРЕПЛЕНО",
                        title = "Держим под рукой"
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            item(key = "pinnedRow") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pinned, key = { "pin-${it.id}" }) { entry ->
                        PinnedMiniCard(
                            entry = entry,
                            onClick = { onOpenEntry(entry) },
                            onPin = { onPin(entry) }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (filtered.isEmpty()) {
            item(key = "empty") {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    if (entries.isEmpty()) {
                        EmptyJournal(onTest = onTest, onOpenSettings = onOpenSettings)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(22.dp)) {
                                Text(
                                    text = "Ничего не найдено",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Попробуйте другой запрос или снимите фильтр.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        groups.forEach { (dayKey, dayEntries) ->
            item(key = "day-$dayKey") {
                DayHeader(
                    label = TimeTools.dayLabel(dayEntries.first().postedAt),
                    count = dayEntries.size
                )
            }
            items(dayEntries, key = { it.id }) { entry ->
                TimelineEntry(
                    entry = entry,
                    index = dayEntries.indexOf(entry),
                    onClick = onOpenEntry,
                    onPin = onPin
                )
            }
        }
    }
}

@Composable
private fun DayHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .height(1.dp)
                .width(44.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun CaptureBanner(
    listener: Boolean,
    enabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (!enabled) "Захват выключен" else "Нет доступа к уведомлениям",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (!enabled) {
                        "Включите его в настройках, чтобы журнал пополнялся."
                    } else {
                        "Разрешите Хронике слушать уведомления."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onOpenSettings) {
                Text("Открыть")
            }
        }
    }
}

@Composable
private fun PinnedMiniCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onPin: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(216.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.appLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onPin, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Открепить",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.displayTitle.ifBlank { entry.appLabel },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = TextTools.compactPreview(entry.displayBody, 100),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Lime)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = TimeTools.time(entry.postedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "ЗАКРЕП",
                    style = MaterialTheme.typography.labelSmall,
                    color = Coral
                )
            }
        }
    }
}
