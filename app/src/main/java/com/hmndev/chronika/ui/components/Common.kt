package com.hmndev.chronika.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.ui.theme.MorphPolygonShape
import com.hmndev.chronika.ui.theme.appPolygon
import com.hmndev.chronika.ui.theme.rememberMorphPolygonShape
import com.hmndev.chronika.util.TextTools

@Composable
fun AppIcon(
    packageName: String,
    label: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val bitmap = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName)
                .toBitmap(128, 128)
                .asImageBitmap()
        }.getOrNull()
    }
    val basePolygon = remember(packageName) { appPolygon(packageName) }
    val morphProgress by animateFloatAsState(
        targetValue = if (highlighted) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "appIconMorph"
    )
    val rotation by animateFloatAsState(
        targetValue = if (highlighted) 12f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "appIconRotate"
    )
    val shape = rememberMorphPolygonShape(
        start = basePolygon,
        end = MaterialShapes.Sunny,
        progress = morphProgress,
        rotationDegrees = rotation
    )
    val haloColor by animateColorAsState(
        targetValue = if (highlighted) {
            scheme.primary.copy(alpha = 0.28f)
        } else {
            scheme.outlineVariant.copy(alpha = 0.35f)
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "appIconHalo"
    )

    Box(
        modifier = modifier
            .size(size + 4.dp)
            .clip(shape)
            .background(haloColor)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(shape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        if (highlighted) scheme.primaryContainer else scheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = TextTools.initials(label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (highlighted) scheme.onPrimaryContainer else scheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Reusable M3 Expressive morphing backdrop that cycles smoothly through a list
 * of [RoundedPolygon]s while rotating continuously.
 */
@Composable
fun ContinuousMorphBlob(
    polygons: List<RoundedPolygon>,
    brush: Brush,
    modifier: Modifier = Modifier,
    cycleDurationMs: Int = 5200,
    rotationDurationMs: Int = 28000
) {
    if (polygons.isEmpty()) return
    val pairs = remember(polygons) {
        if (polygons.size == 1) {
            listOf(Morph(polygons.first(), polygons.first()))
        } else {
            polygons.indices.map { i ->
                Morph(polygons[i], polygons[(i + 1) % polygons.size])
            }
        }
    }
    val transition = rememberInfiniteTransition(label = "continuousMorph")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = pairs.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs * pairs.size, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphPhase"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphRotation"
    )

    val pairIndex = phase.toInt().coerceIn(0, pairs.lastIndex)
    val localProgress = (phase - pairIndex).coerceIn(0f, 1f)
    val shape = MorphPolygonShape(
        morph = pairs[pairIndex],
        progress = localProgress,
        rotationDegrees = rotation
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = brush, shape = shape)
    )
}

@Composable
fun ExpressivePolygonBadge(
    icon: ImageVector,
    active: Boolean,
    restPolygon: RoundedPolygon = MaterialShapes.Cookie6Sided,
    activePolygon: RoundedPolygon = MaterialShapes.Sunny,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    activeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    activeContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 40.dp,
    iconSize: Dp = 19.dp,
    modifier: Modifier = Modifier
) {
    val morphProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "badgeMorph"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (active) 14f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "badgeRotate"
    )
    val bg by animateColorAsState(
        targetValue = if (active) activeContainerColor else containerColor,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "badgeBg"
    )
    val fg by animateColorAsState(
        targetValue = if (active) activeContentColor else contentColor,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "badgeFg"
    )
    val shape = rememberMorphPolygonShape(
        start = restPolygon,
        end = activePolygon,
        progress = morphProgress,
        rotationDegrees = if (active) 18f else 0f
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

@Composable
fun MorphingActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    restPolygon: RoundedPolygon = MaterialShapes.Circle,
    activePolygon: RoundedPolygon = MaterialShapes.Sunny,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    activeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    activeContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val morphProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "actionMorph"
    )
    val shapeRot by animateFloatAsState(
        targetValue = if (active) 20f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "actionRot"
    )
    val bg by animateColorAsState(
        targetValue = if (active) activeContainerColor else containerColor,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "actionBg"
    )
    val fg by animateColorAsState(
        targetValue = if (active) activeContentColor else contentColor,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "actionFg"
    )
    val shape = rememberMorphPolygonShape(
        start = restPolygon,
        end = activePolygon,
        progress = morphProgress,
        rotationDegrees = shapeRot
    )
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interaction)
            .clip(shape)
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = fg,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun MetaPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 10.dp,
            bottomEnd = 20.dp,
            bottomStart = 20.dp
        ),
        color = container,
        contentColor = content,
        border = BorderStroke(0.5.dp, content.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
            } else {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(MaterialShapes.Cookie4Sided.toShape())
                        .background(content.copy(alpha = 0.72f))
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeading(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val spin = rememberInfiniteTransition(label = "sectionSpin")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sectionEmblem"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .graphicsLayer { rotationZ = angle }
                        .clip(MaterialShapes.SoftBurst.toShape())
                        .background(scheme.primary)
                )
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = scheme.onSurface
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun AccentDot(color: Color = Lime, size: Dp = 8.dp) {
    Box(
        Modifier
            .size(size)
            .clip(MaterialShapes.Sunny.toShape())
            .background(color)
    )
}

fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.955f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 560f),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun CoralGlow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialShapes.Cookie7Sided.toShape())
            .background(Coral.copy(alpha = 0.16f))
    )
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    )
}

@Composable
fun HSpace(width: Dp) {
    Spacer(Modifier.width(width))
}
