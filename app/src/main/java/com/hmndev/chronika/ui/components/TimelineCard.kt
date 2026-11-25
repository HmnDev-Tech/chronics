package com.hmndev.chronika.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PushPin
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.JournalEntry
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.ui.theme.asymmetricExpressiveShape
import com.hmndev.chronika.ui.theme.rememberMorphPolygonShape
import com.hmndev.chronika.util.EntryFormat
import com.hmndev.chronika.util.TextTools
import com.hmndev.chronika.util.TimeTools

@Composable
fun TimelineEntry(
    entry: JournalEntry,
    index: Int,
    onClick: (JournalEntry) -> Unit,
    onPin: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val interaction = remember(entry.id) { MutableInteractionSource() }
    val offset = if (compact) {
        0.dp
    } else {
        when (index % 5) {
            2 -> 8.dp
            4 -> 4.dp
            else -> 0.dp
        }
    }

    val nodeMorphProgress by animateFloatAsState(
        targetValue = if (entry.isPinned || !entry.isRead) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "railNodeMorph"
    )
    val nodeShape = rememberMorphPolygonShape(
        start = MaterialShapes.Circle,
        end = if (entry.isPinned) MaterialShapes.Sunny else MaterialShapes.Diamond,
        progress = nodeMorphProgress,
        rotationDegrees = if (entry.isPinned) 16f else 0f
    )
    val pinIconRotation by animateFloatAsState(
        targetValue = if (entry.isPinned) -22f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pinTilt"
    )
    val cardColor by animateColorAsState(
        targetValue = when {
            entry.isPinned -> scheme.primaryContainer.copy(alpha = 0.72f)
            !entry.isRead -> scheme.surfaceContainer
            else -> scheme.surfaceContainerLow
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "cardColor"
    )
    val cardShape = remember(index, entry.packageName, entry.isPinned, compact) {
        if (entry.isPinned) {
            RoundedCornerShape(
                topStart = if (compact) 26.dp else 34.dp,
                topEnd = if (compact) 12.dp else 16.dp,
                bottomEnd = if (compact) 26.dp else 34.dp,
                bottomStart = if (compact) 18.dp else 22.dp
            )
        } else {
            asymmetricExpressiveShape(
                seed = index + entry.packageName.hashCode(),
                compact = compact
            )
        }
    }

    Row(modifier = modifier.fillMaxWidth().padding(horizontal = device.screenPadding)) {
        Column(
            modifier = Modifier
                .width(if (compact) 48.dp else 54.dp)
                .padding(top = if (compact) 7.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (!entry.isRead) scheme.primaryContainer.copy(alpha = 0.7f) else scheme.surfaceContainerHigh
            ) {
                Text(
                    text = TimeTools.time(entry.postedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!entry.isRead) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(if (compact) 6.dp else 8.dp))
            Box(
                Modifier
                    .size(if (compact) 10.dp else 13.dp)
                    .clip(nodeShape)
                    .background(
                        when {
                            entry.isPinned -> Lime
                            !entry.isRead -> Coral
                            else -> scheme.primary.copy(alpha = 0.45f)
                        }
                    )
            )
            Spacer(Modifier.height(3.dp))
            Box(
                Modifier
                    .width(2.dp)
                    .height(if (compact) 42.dp else 68.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (entry.isPinned) Lime.copy(alpha = 0.8f) else scheme.outlineVariant.copy(alpha = 0.75f),
                                scheme.outlineVariant.copy(alpha = 0.15f)
                            )
                        )
                    )
            )
        }
        Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (compact) 7.dp else 11.dp, start = offset)
                .pressScale(interaction)
                .clickable(
                    interactionSource = interaction,
                    indication = null
                ) { onClick(entry) },
            shape = cardShape,
            color = cardColor,
            border = BorderStroke(
                width = if (entry.isPinned || !entry.isRead) 1.dp else 0.5.dp,
                color = when {
                    entry.isPinned -> scheme.primary.copy(alpha = 0.45f)
                    !entry.isRead -> Coral.copy(alpha = 0.32f)
                    else -> scheme.outlineVariant.copy(alpha = 0.35f)
                }
            ),
            tonalElevation = 0.dp
        ) {
            Box {
                if (entry.isPinned || !entry.isRead) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 26.dp, y = (-26).dp)
                            .clip(
                                if (entry.isPinned) {
                                    MaterialShapes.Cookie9Sided.toShape()
                                } else {
                                    MaterialShapes.Clover4Leaf.toShape()
                                }
                            )
                            .background(
                                if (entry.isPinned) {
                                    Lime.copy(alpha = 0.24f)
                                } else {
                                    Coral.copy(alpha = 0.11f)
                                }
                            )
                    )
                }
                Column(
                    Modifier.padding(
                        start = if (compact) 12.dp else 15.dp,
                        end = 10.dp,
                        top = if (compact) 9.dp else 13.dp,
                        bottom = if (compact) 10.dp else 14.dp
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            packageName = entry.packageName,
                            label = entry.appLabel,
                            size = if (compact) 30.dp else 38.dp,
                            highlighted = entry.isPinned
                        )
                        Spacer(Modifier.width(if (compact) 9.dp else 11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = entry.displayTitle.ifBlank { entry.appLabel },
                                style = if (compact) {
                                    MaterialTheme.typography.titleSmall
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (entry.isPinned) scheme.onPrimaryContainer else scheme.onSurface
                            )
                            Text(
                                text = entry.appLabel + if (entry.conversationTitle.isNotBlank()) {
                                    " · ${entry.conversationTitle}"
                                } else {
                                    ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.isPinned) {
                                    scheme.onPrimaryContainer.copy(alpha = 0.78f)
                                } else {
                                    scheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        val pinMorph by animateFloatAsState(
                            targetValue = if (entry.isPinned) 1f else 0f,
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                            label = "pinMorph"
                        )
                        val pinShape = rememberMorphPolygonShape(
                            start = MaterialShapes.Circle,
                            end = MaterialShapes.Sunny,
                            progress = pinMorph,
                            rotationDegrees = if (entry.isPinned) 18f else 0f
                        )
                        val pinInteraction = remember(entry.id) { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(if (compact) 30.dp else 34.dp)
                                .pressScale(pinInteraction)
                                .clip(pinShape)
                                .background(
                                    if (entry.isPinned) {
                                        scheme.primary
                                    } else {
                                        scheme.surfaceContainerHigh
                                    }
                                )
                                .clickable(
                                    interactionSource = pinInteraction,
                                    indication = null,
                                    onClick = { onPin(entry) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = if (entry.isPinned) S["common.unpin"] else S["common.pin"],
                                modifier = Modifier
                                    .size(if (compact) 14.dp else 16.dp)
                                    .graphicsLayer { rotationZ = pinIconRotation },
                                tint = if (entry.isPinned) scheme.onPrimary else scheme.onSurfaceVariant
                            )
                        }
                    }
                    if (entry.displayBody.isNotBlank()) {
                        Spacer(Modifier.height(if (compact) 5.dp else 8.dp))
                        Text(
                            text = TextTools.compactPreview(entry.displayBody, if (compact) 140 else 220),
                            style = if (compact) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (entry.isPinned) {
                                scheme.onPrimaryContainer.copy(alpha = 0.88f)
                            } else {
                                scheme.onSurfaceVariant
                            },
                            maxLines = when {
                                compact -> 2
                                entry.hasMessages -> 2
                                else -> 3
                            },
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val messages = if (!compact && entry.hasMessages) {
                        EntryFormat.messages(entry).takeLast(2)
                    } else {
                        emptyList()
                    }
                    if (messages.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (entry.isPinned) {
                                scheme.surface.copy(alpha = 0.55f)
                            } else {
                                scheme.surfaceContainerHigh.copy(alpha = 0.72f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                messages.forEach { message ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            Modifier
                                                .padding(top = 5.dp)
                                                .size(6.dp)
                                                .clip(MaterialShapes.Cookie4Sided.toShape())
                                                .background(Coral)
                                        )
                                        Spacer(Modifier.width(7.dp))
                                        Text(
                                            text = buildString {
                                                if (message.sender.isNotBlank()) append("${message.sender}: ")
                                                append(message.text)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(if (compact) 6.dp else 10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaPill(
                            text = EntryFormat.sourceLabel(entry.source),
                            icon = Icons.Rounded.GraphicEq,
                            container = if (entry.isPinned) scheme.surface.copy(alpha = 0.75f) else scheme.surfaceContainerHigh,
                            content = scheme.onSurfaceVariant
                        )
                        if (entry.isArchived) {
                            MetaPill(
                                text = S["detail.archived"],
                                container = scheme.secondaryContainer,
                                content = scheme.onSecondaryContainer
                            )
                        }
                        if (entry.isOngoing) {
                            MetaPill(
                                text = S["timeline.ongoing"],
                                container = scheme.tertiaryContainer,
                                content = scheme.onTertiaryContainer
                            )
                        }
                        if (entry.isSilent && !compact) {
                            MetaPill(
                                text = S["timeline.silent"],
                                container = scheme.surfaceContainerHighest,
                                content = scheme.onSurfaceVariant
                            )
                        }
                        if (entry.exportPath.isNotBlank()) {
                            MetaPill(
                                text = S["timeline.downloads"],
                                icon = Icons.Rounded.DownloadDone,
                                container = scheme.primaryContainer,
                                content = scheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        if (!entry.isRead) {
                            Box(
                                Modifier
                                    .size(9.dp)
                                    .clip(MaterialShapes.Sunny.toShape())
                                    .background(Coral)
                            )
                        }
                    }
                }
            }
        }
    }
}
