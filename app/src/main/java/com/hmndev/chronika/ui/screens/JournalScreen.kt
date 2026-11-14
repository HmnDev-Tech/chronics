package com.hmndev.chronika.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.components.AppIcon
import com.hmndev.chronika.ui.components.ChronikaHero
import com.hmndev.chronika.ui.components.EmptyJournal
import com.hmndev.chronika.ui.components.FilterLens
import com.hmndev.chronika.ui.components.JournalFilter
import com.hmndev.chronika.ui.components.SectionHeading
import com.hmndev.chronika.ui.components.TimelineEntry
import com.hmndev.chronika.ui.components.pressScale
import com.hmndev.chronika.ui.components.scrollMotionBlur
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
    onMarkAllRead: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onTest: () -> Unit,
    onOpenInsights: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    var query by rememberSaveable { mutableStateOf("") }
    var filterName by rememberSaveable { mutableStateOf(JournalFilter.ALL.name) }
    var selectedPackage by rememberSaveable { mutableStateOf("") }
    val filter = JournalFilter.entries.firstOrNull { it.name == filterName } ?: JournalFilter.ALL

    val scopeEntries = remember(entries, filter) {
        entries.filter { entry ->
            when (filter) {
                JournalFilter.ALL -> !entry.isArchived
                JournalFilter.IMPORTANT -> !entry.isArchived && (entry.priority >= 1 || !entry.isRead)
                JournalFilter.PINNED -> !entry.isArchived && entry.isPinned
                JournalFilter.ARCHIVED -> entry.isArchived
            }
        }
    }

    val appOptions = remember(scopeEntries) {
        scopeEntries
            .groupBy { it.packageName }
            .map { (pkg, list) ->
                AppCount(
                    packageName = pkg,
                    appLabel = list.first().appLabel.ifBlank { pkg },
                    count = list.size
                )
            }
            .sortedByDescending { it.count }
    }

    val activePackage = if (appOptions.any { it.packageName == selectedPackage }) {
        selectedPackage
    } else {
        ""
    }

    val filtered = remember(scopeEntries, query, activePackage) {
        scopeEntries.filter { entry ->
            val matchesPackage = activePackage.isBlank() || entry.packageName == activePackage
            val matchesQuery = query.isBlank() ||
                EntryFormat.searchText(entry).contains(query, ignoreCase = true)
            matchesPackage && matchesQuery
        }
    }

    val pinned = remember(entries) {
        entries.filter { it.isPinned && !it.isArchived }
    }
    val groups = remember(filtered) {
        filtered.groupBy { TimeTools.dayKey(it.postedAt) }
    }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.scrollMotionBlur(listState, enabled = settings.motionBlur),
        contentPadding = PaddingValues(bottom = device.dp(126.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "hero") {
            Column(Modifier.padding(horizontal = device.screenPadding, vertical = device.dp(12.dp))) {
                ChronikaHero(
                    stats = stats,
                    onOpenInsights = onOpenInsights
                )
                Spacer(Modifier.height(14.dp))
                FilterLens(
                    selected = filter,
                    onSelected = { filterName = it.name }
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
                            text = S["journal.search"],
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(30.dp)
                                .clip(MaterialShapes.Cookie9Sided.toShape())
                                .background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = scheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = query.isNotBlank(),
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = S["common.close"])
                            }
                        }
                    }
                )
            }
        }

        if (appOptions.size > 1 || stats.unread > 0) {
            item(key = "quickChips") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = device.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stats.unread > 0) {
                        item(key = "markAllRead") {
                            AssistChip(
                                onClick = onMarkAllRead,
                                label = { Text("${S["journal.mark_all_read"]} (${stats.unread})") },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(MaterialShapes.Sunny.toShape())
                                            .background(Lime),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DoneAll,
                                            contentDescription = null,
                                            tint = scheme.onPrimaryContainer,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                },
                                shape = CircleShape
                            )
                        }
                    }
                    if (appOptions.size > 1) {
                        item(key = "app-all") {
                            val selectedAll = activePackage.isBlank()
                            ToggleButton(
                                checked = selectedAll,
                                onCheckedChange = { selectedPackage = "" },
                                shapes = ToggleButtonDefaults.shapes(
                                    shape = RoundedCornerShape(14.dp),
                                    pressedShape = RoundedCornerShape(8.dp),
                                    checkedShape = CircleShape
                                ),
                                colors = ToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = scheme.surfaceContainerLow,
                                    contentColor = scheme.onSurfaceVariant,
                                    checkedContainerColor = scheme.secondaryContainer,
                                    checkedContentColor = scheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${S["filter.all"]} · ${scopeEntries.size}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        items(appOptions, key = { "app-${it.packageName}" }) { app ->
                            val isSelected = activePackage == app.packageName
                            ToggleButton(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedPackage = if (isSelected) "" else app.packageName
                                },
                                shapes = ToggleButtonDefaults.shapes(
                                    shape = RoundedCornerShape(14.dp),
                                    pressedShape = RoundedCornerShape(8.dp),
                                    checkedShape = CircleShape
                                ),
                                colors = ToggleButtonDefaults.filledTonalToggleButtonColors(
                                    containerColor = scheme.surfaceContainerLow,
                                    contentColor = scheme.onSurfaceVariant,
                                    checkedContainerColor = scheme.primaryContainer,
                                    checkedContentColor = scheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(start = 7.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
                            ) {
                                AppIcon(
                                    packageName = app.packageName,
                                    label = app.appLabel,
                                    size = 18.dp,
                                    highlighted = isSelected
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${app.appLabel} · ${app.count}",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
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

        if (pinned.isNotEmpty() && filter != JournalFilter.ARCHIVED) {
            item(key = "pinnedHeading") {
                Column(Modifier.padding(horizontal = device.screenPadding)) {
                    Spacer(Modifier.height(14.dp))
                    SectionHeading(
                        eyebrow = S["journal.pinned_eyebrow"],
                        title = S["journal.pinned_title"]
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            item(key = "pinnedRow") {
                Column {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = device.screenPadding),
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
        }

        if (filtered.isEmpty()) {
            item(key = "empty") {
                Column(Modifier.padding(horizontal = device.screenPadding, vertical = device.dp(8.dp))) {
                    if (entries.isEmpty()) {
                        EmptyJournal(onTest = onTest, onOpenSettings = onOpenSettings)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 34.dp,
                                topEnd = 16.dp,
                                bottomEnd = 34.dp,
                                bottomStart = 24.dp
                            ),
                            color = scheme.surfaceContainerLow,
                            border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(22.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(MaterialShapes.Clover4Leaf.toShape())
                                        .background(scheme.secondaryContainer)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = S["journal.empty_title"],
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = S["journal.empty_body"],
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
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
            itemsIndexed(dayEntries, key = { _, it -> it.id }) { index, entry ->
                TimelineEntry(
                    entry = entry,
                    index = index,
                    onClick = onOpenEntry,
                    onPin = onPin,
                    compact = settings.compact
                )
            }
        }
    }
}

@Composable
private fun DayHeader(label: String, count: Int) {
    val device = LocalDeviceProfile.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = device.screenPadding,
                end = device.screenPadding,
                top = device.dp(18.dp),
                bottom = device.dp(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(scheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = scheme.surfaceContainerHighest
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.5.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(scheme.outlineVariant.copy(alpha = 0.7f), scheme.background)
                    )
                )
        )
    }
}

@Composable
private fun CaptureBanner(
    listener: Boolean,
    enabled: Boolean,
    onOpenSettings: () -> Unit,
) {
    val device = LocalDeviceProfile.current
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = device.screenPadding, vertical = device.dp(6.dp)),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 14.dp,
            bottomEnd = 28.dp,
            bottomStart = 20.dp
        ),
        color = scheme.tertiaryContainer,
        border = BorderStroke(1.dp, Coral.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialShapes.SoftBurst.toShape())
                    .background(Coral.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = scheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (!enabled) S["journal.capture_off"] else S["journal.no_access"],
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onTertiaryContainer
                )
                Text(
                    text = if (!enabled) {
                        S["journal.capture_off_body"]
                    } else {
                        S["journal.no_access_body"]
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
            TextButton(onClick = onOpenSettings) {
                Text(S["journal.open"])
            }
        }
    }
}

@Composable
private fun PinnedMiniCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onPin: () -> Unit,
) {
    val device = LocalDeviceProfile.current
    val scheme = MaterialTheme.colorScheme
    val interaction = remember(entry.id) { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .width(device.dp(222.dp))
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 14.dp,
            bottomEnd = 30.dp,
            bottomStart = 20.dp
        ),
        color = scheme.primaryContainer,
        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.35f))
    ) {
        Box {
            Box(
                Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 18.dp, y = 18.dp)
                    .clip(MaterialShapes.Sunny.toShape())
                    .background(scheme.primary.copy(alpha = 0.14f))
            )
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        packageName = entry.packageName,
                        label = entry.appLabel,
                        size = 22.dp,
                        highlighted = true
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = entry.appLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(MaterialShapes.Sunny.toShape())
                            .background(scheme.primary)
                            .clickable(onClick = onPin),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = S["common.unpin"],
                            modifier = Modifier.size(13.dp),
                            tint = scheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.displayTitle.ifBlank { entry.appLabel },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = TextTools.compactPreview(entry.displayBody, 100),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(MaterialShapes.Sunny.toShape())
                            .background(Lime)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = TimeTools.time(entry.postedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = S["journal.pin_badge"],
                        style = MaterialTheme.typography.labelSmall,
                        color = Coral
                    )
                }
            }
        }
    }
}
