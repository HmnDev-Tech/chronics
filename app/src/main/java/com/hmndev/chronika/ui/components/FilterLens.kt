package com.hmndev.chronika.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class JournalFilter(val label: String, val icon: ImageVector) {
    ALL("Все", Icons.Rounded.Inbox),
    IMPORTANT("Важные", Icons.Rounded.Bolt),
    PINNED("Избранное", Icons.Rounded.PushPin)
}

@Composable
fun FilterLens(
    selected: JournalFilter,
    onSelected: (JournalFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        overflowIndicator = { _ ->
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Ещё фильтры",
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        JournalFilter.entries.forEach { filter ->
            val interaction = remember(filter) { MutableInteractionSource() }
            ToggleButton(
                checked = selected == filter,
                onCheckedChange = { onSelected(filter) },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interaction),
                interactionSource = interaction,
                icon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                contentPadding = ToggleButtonDefaults.contentPaddingFor(
                    ToggleButtonDefaults.size.height,
                    hasStartIcon = true
                )
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
