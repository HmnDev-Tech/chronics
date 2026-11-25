package com.hmndev.chronika.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppCount
import com.hmndev.chronika.data.DayCount
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime

@Composable
fun HourlyChart(
    hours: List<Int>,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val max = (hours.maxOrNull() ?: 0).coerceAtLeast(1)
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(hours) { revealed = true }
    val growth by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.08f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "hourlyGrowth"
    )
    val peakHour = remember(hours) {
        hours.indices.maxByOrNull { hours[it] } ?: -1
    }

    Column(modifier = modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(114.dp)
        ) {
            val gap = size.width / 24f
            val barWidth = gap * 0.62f
            hours.forEachIndexed { index, value ->
                val fraction = (value.toFloat() / max) * growth
                val height = (size.height - 10.dp.toPx()) * fraction
                val x = index * gap + (gap - barWidth) / 2f
                val y = size.height - height
                val isPeak = value > 0 && index == peakHour
                val color = when {
                    value == 0 -> scheme.surfaceContainerHighest
                    isPeak -> Lime
                    index in 9..18 -> scheme.primary
                    else -> Coral.copy(alpha = 0.82f)
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y.coerceAtLeast(0f)),
                    size = Size(barWidth, height.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
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
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(days) { revealed = true }
    val growth by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.1f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "dayGrowth"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            val isToday = index == days.lastIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (isToday && day.count > 0) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(MaterialShapes.Sunny.toShape())
                            .background(Lime)
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = day.count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isToday) scheme.primary else scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((10 + 68 * (day.count.toFloat() / max) * growth).dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = 6.dp,
                                bottomEnd = 6.dp
                            )
                        )
                        .background(
                            if (isToday) {
                                Brush.verticalGradient(listOf(Lime, scheme.primary))
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        scheme.primary.copy(alpha = 0.72f),
                                        scheme.primary.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        )
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) scheme.onSurface else scheme.onSurfaceVariant
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
    val targetFraction = (app.count.toFloat() / total.coerceAtLeast(1)).coerceIn(0.04f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "topAppProgress"
    )
    val isDominant = app.count > total / 2

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            packageName = app.packageName,
            label = app.appLabel,
            size = 36.dp,
            highlighted = isDominant
        )
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appLabel,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = if (isDominant) scheme.primaryContainer else scheme.surfaceContainerHigh
                ) {
                    Text(
                        text = app.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDominant) scheme.onPrimaryContainer else scheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHighest)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animatedFraction)
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDominant) {
                                Brush.horizontalGradient(listOf(scheme.primary, Lime))
                            } else {
                                Brush.horizontalGradient(listOf(scheme.primary, scheme.secondary))
                            }
                        )
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
    val rawFraction = (used.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f)
    val fraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "storageFraction"
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 34.dp,
            topEnd = 18.dp,
            bottomEnd = 34.dp,
            bottomStart = 24.dp
        ),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box {
            Box(
                Modifier
                    .size(92.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 26.dp, y = (-26).dp)
                    .clip(MaterialShapes.Clover4Leaf.toShape())
                    .background(scheme.primaryContainer.copy(alpha = 0.45f))
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        progress = { fraction.coerceAtLeast(0.06f) },
                        modifier = Modifier.size(56.dp),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest
                    )
                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = scheme.primary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = S["charts.storage"],
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearWavyProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest,
                        amplitude = { progress -> (1f - progress).coerceIn(0.15f, 1f) }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = S.f("charts.storage_line", used, max),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
