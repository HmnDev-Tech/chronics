package com.hmndev.chronika.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.JournalStats
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime

@Composable
fun ChronikaHero(
    stats: JournalStats,
    modifier: Modifier = Modifier,
    onOpenInsights: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val blob = MaterialShapes.Cookie9Sided.toShape(startAngle = 14)
    val transition = rememberInfiniteTransition(label = "live")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(device.heroHeight)
            .clip(RoundedCornerShape(device.dp(42.dp)))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        scheme.surfaceContainerHigh,
                        scheme.surfaceContainerLow,
                        scheme.primaryContainer.copy(alpha = 0.65f)
                    ),
                    start = Offset.Zero,
                    end = Offset(1100f, 700f)
                )
            )
            .clickable(onClick = onOpenInsights)
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 58.dp, y = (-54).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Lime.copy(alpha = 0.62f), Color.Transparent)
                    ),
                    shape = blob
                )
        )
        Canvas(Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.78f)
                cubicTo(
                    size.width * 0.18f, size.height * 0.62f,
                    size.width * 0.30f, size.height * 0.94f,
                    size.width * 0.48f, size.height * 0.80f
                )
                cubicTo(
                    size.width * 0.66f, size.height * 0.66f,
                    size.width * 0.78f, size.height * 0.92f,
                    size.width, size.height * 0.72f
                )
            }
            drawPath(
                path = path,
                color = scheme.primary.copy(alpha = 0.22f),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = device.dp(24.dp), top = device.dp(22.dp), end = device.dp(140.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Coral.copy(alpha = pulse))
                )
                Spacer(Modifier.size(7.dp))
                Text(
                    text = S["hero.eyebrow"],
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stats.today.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                color = scheme.onSurface
            )
            Text(
                text = if (stats.today == 0) S["hero.quiet"] else S["hero.saved"],
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = device.dp(18.dp), bottom = device.dp(18.dp)),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeroStat(
                value = stats.total.toString(),
                label = S["hero.total"],
                container = scheme.surface.copy(alpha = 0.78f)
            )
            HeroStat(
                value = stats.apps.toString(),
                label = S["hero.apps"],
                container = scheme.secondaryContainer.copy(alpha = 0.92f),
                modifier = Modifier.offset(x = (-16).dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = device.dp(24.dp), bottom = device.dp(22.dp)),
            shape = RoundedCornerShape(50),
            color = scheme.primary,
            contentColor = scheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(S["hero.pulse"], style = MaterialTheme.typography.labelSmall)
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Lime)
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    container: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = container,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
