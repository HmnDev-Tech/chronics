package com.hmndev.chronika.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime

@Composable
fun ChronikaHero(
    stats: JournalStats,
    modifier: Modifier = Modifier,
    onOpenInsights: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val interaction = remember { MutableInteractionSource() }
    val transition = rememberInfiniteTransition(label = "liveHero")
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val pulseScaleVal by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val barWaveA by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(680),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barA"
    )
    val barWaveB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(820),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barB"
    )
    val todayShare by animateFloatAsState(
        targetValue = if (stats.total <= 0) 0.14f else (stats.today.toFloat() / stats.total).coerceIn(0.12f, 1f),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "todayShare"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(device.heroHeight)
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onOpenInsights
            ),
        shape = RoundedCornerShape(
            topStart = device.dp(44.dp),
            topEnd = device.dp(24.dp),
            bottomEnd = device.dp(44.dp),
            bottomStart = device.dp(20.dp)
        ),
        color = scheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            scheme.surfaceContainerHigh,
                            scheme.surfaceContainerLow,
                            scheme.primaryContainer.copy(alpha = 0.58f)
                        ),
                        start = Offset.Zero,
                        end = Offset(1100f, 750f)
                    )
                )
        ) {
            ContinuousMorphBlob(
                polygons = listOf(
                    MaterialShapes.Cookie9Sided,
                    MaterialShapes.Sunny,
                    MaterialShapes.Clover8Leaf,
                    MaterialShapes.Flower,
                    MaterialShapes.SoftBurst
                ),
                brush = Brush.radialGradient(
                    colors = listOf(
                        Lime.copy(alpha = 0.72f),
                        scheme.primaryContainer.copy(alpha = 0.38f),
                        Color.Transparent
                    )
                ),
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 64.dp, y = (-58).dp)
            )

            ContinuousMorphBlob(
                polygons = listOf(
                    MaterialShapes.Clover4Leaf,
                    MaterialShapes.Cookie6Sided,
                    MaterialShapes.PuffyDiamond
                ),
                brush = Brush.radialGradient(
                    colors = listOf(
                        Coral.copy(alpha = 0.22f),
                        Color.Transparent
                    )
                ),
                cycleDurationMs = 4600,
                rotationDurationMs = 22000,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-42).dp, y = 44.dp)
            )

            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.78f)
                    cubicTo(
                        size.width * 0.18f, size.height * 0.60f,
                        size.width * 0.32f, size.height * 0.94f,
                        size.width * 0.50f, size.height * 0.78f
                    )
                    cubicTo(
                        size.width * 0.68f, size.height * 0.62f,
                        size.width * 0.80f, size.height * 0.90f,
                        size.width, size.height * 0.68f
                    )
                }
                drawPath(
                    path = path,
                    color = scheme.primary.copy(alpha = 0.24f),
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = device.dp(24.dp), top = device.dp(22.dp), end = device.dp(150.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .size(11.dp)
                            .graphicsLayer {
                                scaleX = pulseScaleVal
                                scaleY = pulseScaleVal
                            }
                            .clip(MaterialShapes.Sunny.toShape())
                            .background(Coral.copy(alpha = pulse))
                    )
                    Text(
                        text = S["hero.eyebrow"],
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                AnimatedContent(
                    targetState = stats.today,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.82f, animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()) + fadeIn())
                            .togetherWith(scaleOut(targetScale = 1.1f) + fadeOut())
                    },
                    label = "heroCount"
                ) { todayCount ->
                    Text(
                        text = todayCount.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.displayLarge,
                        color = scheme.onSurface
                    )
                }
                Text(
                    text = if (stats.today == 0) S["hero.quiet"] else S["hero.saved"],
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = device.dp(18.dp), end = device.dp(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    progress = { todayShare },
                    modifier = Modifier.size(54.dp),
                    color = scheme.primary,
                    trackColor = scheme.onSurface.copy(alpha = 0.12f)
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(MaterialShapes.Cookie4Sided.toShape())
                        .background(scheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(todayShare * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = device.dp(18.dp), bottom = device.dp(16.dp)),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroStat(
                    value = stats.total.toString(),
                    label = S["hero.total"],
                    container = scheme.surface.copy(alpha = 0.86f),
                    content = scheme.onSurface,
                    badgePolygon = MaterialShapes.Cookie4Sided,
                    badgeColor = scheme.primaryContainer
                )
                HeroStat(
                    value = stats.apps.toString(),
                    label = S["hero.apps"],
                    container = scheme.secondaryContainer.copy(alpha = 0.96f),
                    content = scheme.onSecondaryContainer,
                    badgePolygon = MaterialShapes.PuffyDiamond,
                    badgeColor = scheme.secondary.copy(alpha = 0.22f),
                    modifier = Modifier.offset(x = (-14).dp)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = device.dp(22.dp), bottom = device.dp(20.dp)),
                shape = CircleShape,
                color = scheme.primary,
                contentColor = scheme.onPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.height(12.dp)
                    ) {
                        val heights = listOf(barWaveA, barWaveB, (barWaveA + barWaveB) * 0.5f)
                        heights.forEach { h ->
                            Box(
                                Modifier
                                    .width(2.5.dp)
                                    .height((4 + 8 * h).dp)
                                    .clip(CircleShape)
                                    .background(Lime)
                            )
                        }
                    }
                    Text(
                        text = S["hero.pulse"],
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(MaterialShapes.Sunny.toShape())
                            .background(Lime)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    container: Color,
    content: Color,
    badgePolygon: RoundedPolygon,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 12.dp, bottomEnd = 24.dp, bottomStart = 20.dp),
        color = container,
        contentColor = content,
        border = BorderStroke(0.5.dp, content.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(badgePolygon.toShape())
                    .background(badgeColor)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = content
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.74f)
                )
            }
        }
    }
}
