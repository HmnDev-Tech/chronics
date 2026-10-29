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
import com.hmndev.chronika.i18n.S

enum class JournalFilter(val labelKey: String, val icon: ImageVector) {
    ALL("filter.all", Icons.Rounded.Inbox),
    IMPORTANT("filter.important", Icons.Rounded.Bolt),
    PINNED("filter.pinned", Icons.Rounded.PushPin)
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
                contentDescription = S["filter.more"],
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        JournalFilter.entries.forEach { filter ->
            toggleableItem(
                checked = selected == filter,
                label = S[filter.labelKey],
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
