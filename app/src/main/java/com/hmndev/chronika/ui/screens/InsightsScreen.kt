package com.hmndev.chronika.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.DayCount
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.components.ContinuousMorphBlob
import com.hmndev.chronika.ui.components.DayBars
import com.hmndev.chronika.ui.components.HourlyChart
import com.hmndev.chronika.ui.components.SectionHeading
import com.hmndev.chronika.ui.components.StorageWave
import com.hmndev.chronika.ui.components.TopAppRow
import com.hmndev.chronika.ui.theme.Lime

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
                Surface(
                    modifier = Modifier.weight(1.18f),
                    shape = RoundedCornerShape(
                        topStart = 38.dp,
                        topEnd = 16.dp,
                        bottomEnd = 38.dp,
                        bottomStart = 22.dp
                    ),
                    color = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                    border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.35f))
                ) {
                    Box {
                        ContinuousMorphBlob(
                            polygons = listOf(
                                MaterialShapes.Cookie12Sided,
                                MaterialShapes.Flower,
                                MaterialShapes.Sunny
                            ),
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Lime.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            ),
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 34.dp, y = 34.dp)
                        )
                        Column(Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(MaterialShapes.Cookie9Sided.toShape())
                                    .background(scheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = scheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stats.total.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = S["insights.entries"],
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(0.85f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InsightStat(
                        value = stats.unread.toString(),
                        label = S["insights.unread"],
                        icon = Icons.Rounded.Bolt,
                        badgePolygon = MaterialShapes.SoftBurst,
                        container = scheme.tertiaryContainer,
                        content = scheme.onTertiaryContainer,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 30.dp,
                            bottomEnd = 18.dp,
                            bottomStart = 30.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    InsightStat(
                        value = stats.apps.toString(),
                        label = S["insights.apps"],
                        icon = Icons.Rounded.AutoAwesome,
                        badgePolygon = MaterialShapes.Clover4Leaf,
                        container = scheme.secondaryContainer,
                        content = scheme.onSecondaryContainer,
                        shape = RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 16.dp,
                            bottomEnd = 30.dp,
                            bottomStart = 16.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "hourly") {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 34.dp,
                    topEnd = 18.dp,
                    bottomEnd = 34.dp,
                    bottomStart = 24.dp
                ),
                color = scheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(MaterialShapes.Cookie6Sided.toShape())
                                .background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = scheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = S["insights.hourly"],
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    HourlyChart(hours = stats.todayByHour)
                }
            }
        }

        item(key = "week") {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 34.dp,
                    bottomEnd = 20.dp,
                    bottomStart = 34.dp
                ),
                color = scheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(MaterialShapes.Sunny.toShape())
                                .background(Lime)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = S["insights.week"],
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    DayBars(days = daily)
                }
            }
        }

        item(key = "sources") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SourceStatPill(
                    text = S.f("insights.source_listener", sources["listener"] ?: 0),
                    icon = Icons.Rounded.NotificationsActive,
                    polygon = MaterialShapes.Cookie4Sided,
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer
                )
                SourceStatPill(
                    text = S.f("insights.source_accessibility", sources["accessibility"] ?: 0),
                    icon = Icons.Rounded.Accessibility,
                    polygon = MaterialShapes.Clover4Leaf,
                    container = scheme.tertiaryContainer,
                    content = scheme.onTertiaryContainer
                )
                SourceStatPill(
                    text = S.f("insights.source_shizuku", sources["shizuku"] ?: 0),
                    icon = Icons.Rounded.Terminal,
                    polygon = MaterialShapes.Diamond,
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
                    shape = RoundedCornerShape(
                        topStart = 34.dp,
                        topEnd = 18.dp,
                        bottomEnd = 34.dp,
                        bottomStart = 24.dp
                    ),
                    color = scheme.surfaceContainerLow,
                    border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
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
    icon: ImageVector,
    badgePolygon: RoundedPolygon,
    container: Color,
    content: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        contentColor = content
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(badgePolygon.toShape())
                    .background(content.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SourceStatPill(
    text: String,
    icon: ImageVector,
    polygon: RoundedPolygon,
    container: Color,
    content: Color
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 12.dp, bottomEnd = 24.dp, bottomStart = 18.dp),
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(polygon.toShape())
                    .background(content.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
