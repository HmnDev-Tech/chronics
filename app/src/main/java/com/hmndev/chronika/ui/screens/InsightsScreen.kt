package com.hmndev.chronika.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.DayCount
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.components.DayBars
import com.hmndev.chronika.ui.components.HourlyChart
import com.hmndev.chronika.ui.components.MetaPill
import com.hmndev.chronika.ui.components.SectionHeading
import com.hmndev.chronika.ui.components.StorageWave
import com.hmndev.chronika.ui.components.TopAppRow

@Composable
fun InsightsScreen(
    stats: JournalStats,
    topApps: List<AppCount>,
    daily: List<DayCount>,
    entries: List<JournalEntry>,
    settings: AppSettings,
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val sources = remember(stats.sources, entries) {
        if (stats.sources.isNotEmpty()) {
            stats.sources
        } else {
            entries.groupingBy { it.source }.eachCount()
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(
            start = device.screenPadding,
            end = device.screenPadding,
            top = device.dp(14.dp),
            bottom = device.dp(126.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(device.dp(14.dp))
    ) {
        item(key = "heading") {
            SectionHeading(
                eyebrow = S["insights.eyebrow"],
                title = S["insights.title"]
            )
        }

        item(key = "mosaic") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightStat(
                    value = stats.total.toString(),
                    label = S["insights.entries"],
                    icon = Icons.Rounded.Layers,
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer,
                    modifier = Modifier.weight(1.15f)
                )
                Column(
                    modifier = Modifier.weight(0.85f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InsightStat(
                        value = stats.unread.toString(),
                        label = S["insights.unread"],
                        icon = Icons.Rounded.Bolt,
                        container = scheme.tertiaryContainer,
                        content = scheme.onTertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InsightStat(
                        value = stats.apps.toString(),
                        label = S["insights.apps"],
                        icon = Icons.Rounded.AutoAwesome,
                        container = scheme.secondaryContainer,
                        content = scheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "hourly") {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = scheme.primary
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = S["insights.hourly"],
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    HourlyChart(hours = stats.todayByHour)
                }
            }
        }

        item(key = "week") {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = S["insights.week"],
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    DayBars(days = daily)
                }
            }
        }

        item(key = "sources") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                MetaPill(
                    text = S.f("insights.source_listener", sources["listener"] ?: 0),
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer
                )
                MetaPill(
                    text = S.f("insights.source_accessibility", sources["accessibility"] ?: 0),
                    container = scheme.tertiaryContainer,
                    content = scheme.onTertiaryContainer
                )
                MetaPill(
                    text = S.f("insights.source_shizuku", sources["shizuku"] ?: 0),
                    container = scheme.secondaryContainer,
                    content = scheme.onSecondaryContainer
                )
            }
        }

        item(key = "storage") {
            StorageWave(used = stats.total, max = settings.maxEntries)
        }

        if (topApps.isNotEmpty()) {
            item(key = "appsHeading") {
                SectionHeading(
                    eyebrow = S["insights.top_eyebrow"],
                    title = S["insights.top_title"]
                )
            }
            item(key = "apps") {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = scheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        topApps.forEach { app ->
                            TopAppRow(app = app, total = stats.total)
                        }
                    }
                }
            }
        }

        item(key = "footnote") {
            Text(
                text = S.f("insights.footnote", settings.safeFolderName),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightStat(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = content
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
