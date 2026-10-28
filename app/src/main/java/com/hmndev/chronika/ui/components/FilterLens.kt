package com.hmndev.chronika.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
            toggleableItem(
                checked = selected == filter,
                label = filter.label,
                onCheckedChange = { onSelected(filter) },
                icon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                weight = 1f
            )
        }
    }
}
