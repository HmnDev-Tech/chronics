package com.hmndev.chronika.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.ui.theme.rememberMorphPolygonShape

enum class JournalFilter(val labelKey: String, val icon: ImageVector) {
    ALL("filter.all", Icons.Rounded.Inbox),
    IMPORTANT("filter.important", Icons.Rounded.Bolt),
    PINNED("filter.pinned", Icons.Rounded.PushPin),
    ARCHIVED("filter.archived", Icons.Rounded.Archive),
}

@Composable
fun FilterLens(
    selected: JournalFilter,
    onSelected: (JournalFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JournalFilter.entries.forEach { filter ->
            val checked = selected == filter
            val interaction = remember(filter) { MutableInteractionSource() }
            val morphProgress by animateFloatAsState(
                targetValue = if (checked) 1f else 0f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "filterMorph"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (checked) 1.12f else 1f,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "filterIconScale"
            )
            val badgeShape = rememberMorphPolygonShape(
                start = MaterialShapes.Circle,
                end = MaterialShapes.Sunny,
                progress = morphProgress,
                rotationDegrees = if (checked) 15f else 0f
            )

            ToggleButton(
                checked = checked,
                onCheckedChange = { onSelected(filter) },
                interactionSource = interaction,
                modifier = Modifier.pressScale(interaction),
                shapes = ToggleButtonShapes(
                    shape = RoundedCornerShape(18.dp),
                    pressedShape = RoundedCornerShape(10.dp),
                    checkedShape = CircleShape
                ),
                colors = ToggleButtonDefaults.colors(
                    containerColor = scheme.surfaceContainerLow,
                    contentColor = scheme.onSurfaceVariant,
                    checkedContainerColor = scheme.primary,
                    checkedContentColor = scheme.onPrimary
                ),
                border = if (checked) null else BorderStroke(0.5.dp, scheme.outlineVariant.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(badgeShape)
                        .background(
                            if (checked) Lime.copy(alpha = 0.28f) else scheme.surfaceContainerHighest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
                Spacer(Modifier.width(7.dp))
                Text(
                    text = S[filter.labelKey],
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
