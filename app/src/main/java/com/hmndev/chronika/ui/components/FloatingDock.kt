package com.hmndev.chronika.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.ui.theme.rememberMorphPolygonShape

enum class DockDestination(val labelKey: String, val icon: ImageVector) {
    JOURNAL("dock.journal", Icons.Rounded.Timeline),
    INSIGHTS("dock.insights", Icons.Rounded.QueryStats),
    SETTINGS("dock.settings", Icons.Rounded.Tune)
}

@Composable
fun FloatingDock(
    selected: DockDestination,
    onSelect: (DockDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = scheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = 6.dp,
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockDestination.entries.forEach { destination ->
                DockItem(
                    destination = destination,
                    selected = selected == destination,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    destination: DockDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()

    val background by animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = effects,
        label = "dockColor"
    )
    val badgeBg by animateColorAsState(
        targetValue = if (selected) Lime.copy(alpha = 0.24f) else scheme.surfaceContainerHighest.copy(alpha = 0.65f),
        animationSpec = effects,
        label = "dockBadgeColor"
    )
    val horizontal by animateDpAsState(
        targetValue = if (selected) 14.dp else 10.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "dockPadding"
    )
    val morphProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spatial,
        label = "dockIconMorph"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 0.96f,
        animationSpec = spatial,
        label = "dockIconScale"
    )
    val badgeShape = rememberMorphPolygonShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Cookie9Sided,
        progress = morphProgress,
        rotationDegrees = if (selected) 18f else 0f
    )
    val sparkSpin = rememberInfiniteTransition(label = "dockSpark")
    val sparkAngle by sparkSpin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkAngle"
    )

    Row(
        modifier = Modifier
            .pressScale(interaction)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontal, vertical = 7.dp)
            .animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(badgeShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = S[destination.labelKey],
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(),
            exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = S[destination.labelKey],
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onPrimary
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .graphicsLayer { rotationZ = sparkAngle }
                        .clip(MaterialShapes.Sunny.toShape())
                        .background(Lime)
                )
            }
        }
    }
}
