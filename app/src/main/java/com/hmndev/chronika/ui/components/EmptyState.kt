package com.hmndev.chronika.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime

@Composable
fun EmptyJournal(
    onTest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 20.dp,
            bottomEnd = 40.dp,
            bottomStart = 28.dp
        ),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Box {
            ContinuousMorphBlob(
                polygons = listOf(
                    MaterialShapes.Flower,
                    MaterialShapes.SoftBurst,
                    MaterialShapes.Cookie9Sided,
                    MaterialShapes.Sunny
                ),
                brush = Brush.radialGradient(
                    colors = listOf(
                        Lime.copy(alpha = 0.42f),
                        scheme.primaryContainer.copy(alpha = 0.16f),
                        Color.Transparent
                    )
                ),
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 42.dp, y = (-34).dp)
            )
            ContinuousMorphBlob(
                polygons = listOf(
                    MaterialShapes.Clover4Leaf,
                    MaterialShapes.Cookie6Sided
                ),
                brush = Brush.radialGradient(
                    colors = listOf(Coral.copy(alpha = 0.18f), Color.Transparent)
                ),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 32.dp)
            )
            Canvas(Modifier.matchParentSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.64f)
                    cubicTo(
                        size.width * 0.22f, size.height * 0.46f,
                        size.width * 0.38f, size.height * 0.84f,
                        size.width * 0.56f, size.height * 0.62f
                    )
                    cubicTo(
                        size.width * 0.74f, size.height * 0.45f,
                        size.width * 0.86f, size.height * 0.72f,
                        size.width, size.height * 0.52f
                    )
                }
                drawPath(
                    path,
                    color = scheme.primary.copy(alpha = 0.20f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.size(66.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(66.dp)
                            .clip(MaterialShapes.Cookie6Sided.toShape())
                            .background(scheme.primaryContainer)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = S["empty.title"],
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = S["empty.body"],
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onTest, shape = CircleShape) {
                        Icon(
                            Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.size(7.dp))
                        Text(S["empty.test"])
                    }
                    OutlinedButton(onClick = onOpenSettings, shape = CircleShape) {
                        Text(S["empty.setup"])
                    }
                }
            }
        }
    }
}
