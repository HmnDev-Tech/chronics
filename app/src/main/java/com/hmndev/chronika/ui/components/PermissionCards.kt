package com.hmndev.chronika.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Lime

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = if (step.granted) {
            scheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            scheme.surfaceContainerLow
        },
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(step.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = step.accent,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "0${index + 1}".takeLast(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = step.accent
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
                        .size(26.dp)
                        .clip(CircleShape)
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
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(scheme.outline)
                        )
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = S["permission.readiness"],
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$done / $total",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (index < done) Lime else scheme.surfaceContainerHighest
                        )
                )
            }
        }
    }
}
