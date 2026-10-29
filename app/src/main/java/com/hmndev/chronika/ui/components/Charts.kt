package com.hmndev.chronika.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.data.DayCount
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime

@Composable
fun HourlyChart(
    hours: List<Int>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val max = (hours.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(modifier = modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(108.dp)
        ) {
            val gap = size.width / 24f
            val barWidth = gap * 0.58f
            hours.forEachIndexed { index, value ->
                val fraction = value.toFloat() / max
                val height = (size.height - 8.dp.toPx()) * fraction
                val x = index * gap + (gap - barWidth) / 2f
                val y = size.height - height
                val color = when {
                    value == 0 -> scheme.surfaceContainerHighest
                    index in 9..18 -> scheme.primary
                    else -> Coral.copy(alpha = 0.75f)
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y.coerceAtLeast(0f)),
                    size = Size(barWidth, height.coerceAtLeast(3.dp.toPx())),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("00", "06", "12", "18", "23").forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DayBars(
    days: List<DayCount>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth().height(128.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = day.count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((8 + 66 * (day.count.toFloat() / max)).dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(if (index == days.lastIndex) Lime else scheme.primary.copy(alpha = 0.55f))
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TopAppRow(
    app: AppCount,
    total: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, app.appLabel, size = 34.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appLabel,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = app.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.surfaceContainerHighest)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((app.count.toFloat() / total.coerceAtLeast(1)).coerceIn(0.03f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (app.count > total / 2) Lime else scheme.primary)
                )
            }
        }
    }
}

@Composable
fun StorageWave(
    used: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fraction = (used.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = scheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = S["charts.storage"],
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                LinearWavyProgressIndicator(
                    progress = { fraction },
                    color = scheme.primary,
                    trackColor = scheme.surfaceContainerHighest,
                    amplitude = { progress -> (1f - progress).coerceIn(0f, 1f) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = S.f("charts.storage_line", used, max),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
