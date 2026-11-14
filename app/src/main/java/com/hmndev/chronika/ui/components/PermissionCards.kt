package com.hmndev.chronika.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.ui.theme.asymmetricExpressiveShape
import com.hmndev.chronika.ui.theme.rememberMorphPolygonShape

data class PermissionStep(
    val title: String,
    val description: String,
    val granted: Boolean,
    val actionLabel: String,
    val icon: ImageVector,
    val accent: Color,
    val onAction: () -> Unit,
    val actionEnabled: Boolean = true
)

@Composable
fun PermissionStepCard(
    step: PermissionStep,
    index: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val morphProgress by animateFloatAsState(
        targetValue = if (step.granted) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "stepCheckMorph"
    )
    val checkShape = rememberMorphPolygonShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Sunny,
        progress = morphProgress,
        rotationDegrees = if (step.granted) 18f else 0f
    )
    val cardBg by animateColorAsState(
        targetValue = if (step.granted) {
            scheme.primaryContainer.copy(alpha = 0.62f)
        } else {
            scheme.surfaceContainerLow
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "stepCardBg"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = asymmetricExpressiveShape(seed = index),
        color = cardBg,
        border = BorderStroke(
            width = if (step.granted) 1.dp else 0.5.dp,
            color = if (step.granted) scheme.primary.copy(alpha = 0.45f) else scheme.outlineVariant.copy(alpha = 0.4f)
        ),
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                ExpressivePolygonBadge(
                    icon = step.icon,
                    active = step.granted,
                    restPolygon = MaterialShapes.Cookie6Sided,
                    activePolygon = MaterialShapes.Sunny,
                    containerColor = step.accent.copy(alpha = 0.16f),
                    activeContainerColor = scheme.primary,
                    contentColor = step.accent,
                    activeContentColor = scheme.onPrimary,
                    size = 48.dp,
                    iconSize = 23.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "0${index + 1}".takeLast(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (step.granted) scheme.primary else step.accent
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(checkShape)
                        .background(if (step.granted) Lime else scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (step.granted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = S["common.done"],
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(MaterialShapes.Cookie4Sided.toShape())
                                .background(scheme.outline)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (step.granted) S["common.allowed"] else S["common.needs_permission"],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (step.granted) scheme.primary else scheme.tertiary
                )
                Button(
                    onClick = step.onAction,
                    enabled = step.actionEnabled,
                    shape = CircleShape
                ) {
                    Text(step.actionLabel)
                }
            }
        }
    }
}

@Composable
fun SetupProgress(
    done: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val ratio by animateFloatAsState(
        targetValue = (done.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "setupRatio"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = S["permission.readiness"],
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(total) { index ->
                    val complete = index < done
                    Box(
                        Modifier
                            .size(if (complete) 11.dp else 9.dp)
                            .clip(
                                if (complete) {
                                    MaterialShapes.Sunny.toShape()
                                } else {
                                    MaterialShapes.Cookie4Sided.toShape()
                                }
                            )
                            .background(if (complete) Lime else scheme.surfaceContainerHighest)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$done / $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearWavyProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth(),
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHighest,
            amplitude = { p -> (1f - p).coerceIn(0.15f, 1f) }
        )
    }
}
