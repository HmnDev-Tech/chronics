package com.hmndev.chronika.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
        shape = RoundedCornerShape(34.dp),
        color = scheme.surfaceContainerLow
    ) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    color = Lime.copy(alpha = 0.20f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(size.width * 0.85f, size.height * 0.18f)
                )
                drawCircle(
                    color = Coral.copy(alpha = 0.14f),
                    radius = size.minDimension * 0.25f,
                    center = Offset(size.width * 0.10f, size.height * 0.85f)
                )
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.62f)
                    cubicTo(
                        size.width * 0.2f, size.height * 0.45f,
                        size.width * 0.35f, size.height * 0.8f,
                        size.width * 0.55f, size.height * 0.62f
                    )
                    cubicTo(
                        size.width * 0.72f, size.height * 0.46f,
                        size.width * 0.86f, size.height * 0.7f,
                        size.width, size.height * 0.52f
                    )
                }
                drawPath(
                    path,
                    color = scheme.primary.copy(alpha = 0.18f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Журнал ещё пуст",
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "Разрешите доступ к уведомлениям — Хроника начнёт собирать " +
                        "полные тексты в Downloads. Даже если сообщение исчезнет из чата, " +
                        "оно останется здесь.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTest, shape = CircleShape) {
                        Icon(
                            Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.size(7.dp))
                        Text("Проверить")
                    }
                    OutlinedButton(onClick = onOpenSettings, shape = CircleShape) {
                        Text("Настроить")
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
