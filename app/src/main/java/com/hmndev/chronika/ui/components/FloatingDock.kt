package com.hmndev.chronika.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.ui.theme.Lime

enum class DockDestination(val label: String, val icon: ImageVector) {
    JOURNAL("Журнал", Icons.Rounded.Timeline),
    INSIGHTS("Пульс", Icons.Rounded.QueryStats),
    SETTINGS("Настройки", Icons.Rounded.Tune)
}

@Composable
fun FloatingDock(
    selected: DockDestination,
    onSelect: (DockDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
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
    val background by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "dockColor"
    )
    val horizontal by animateDpAsState(
        targetValue = if (selected) 15.dp else 13.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
        label = "dockPadding"
    )
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontal, vertical = 10.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            modifier = Modifier.size(20.dp),
            tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onPrimary
            )
        }
        if (selected) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Lime)
            )
        }
    }
}
