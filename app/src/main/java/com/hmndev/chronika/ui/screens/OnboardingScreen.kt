package com.hmndev.chronika.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.i18n.S
import com.hmndev.chronika.shizuku.ShizukuState
import com.hmndev.chronika.ui.LocalDeviceProfile
import com.hmndev.chronika.ui.components.AccentDot
import com.hmndev.chronika.ui.components.PermissionStep
import com.hmndev.chronika.ui.components.PermissionStepCard
import com.hmndev.chronika.ui.components.SetupProgress
import com.hmndev.chronika.ui.theme.Coral
import com.hmndev.chronika.ui.theme.Lime
import com.hmndev.chronika.util.AccessState

@Composable
fun OnboardingScreen(
    access: AccessState,
    shizuku: ShizukuState,
    onOpenNotificationSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenShizuku: () -> Unit,
    onRequestShizuku: () -> Unit,
    onGrantListener: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onTest: () -> Unit,
    onFinish: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val device = LocalDeviceProfile.current
    val shizukuReady = shizuku.available && shizuku.permissionGranted
    val steps = listOf(
        PermissionStep(
            title = S["onboarding.listener"],
            description = S["onboarding.listener_desc"],
            granted = access.listener,
            actionLabel = if (access.listener) S["common.check"] else S["common.open"],
            icon = Icons.Rounded.NotificationsActive,
            accent = Lime,
            onAction = onOpenNotificationSettings
        ),
        PermissionStep(
            title = S["onboarding.accessibility"],
            description = S["onboarding.accessibility_desc"],
            granted = access.accessibility,
            actionLabel = if (access.accessibility) S["common.check"] else S["common.open"],
            icon = Icons.Rounded.Accessibility,
            accent = Coral,
            onAction = onOpenAccessibilitySettings
        ),
        PermissionStep(
            title = "Shizuku",
            description = S["onboarding.shizuku_desc"],
            granted = shizukuReady,
            actionLabel = when {
                !shizuku.available -> S["common.open"]
                !shizuku.permissionGranted -> S["common.allow"]
                else -> S["common.done"]
            },
            icon = Icons.Rounded.Terminal,
            accent = scheme.tertiary,
            onAction = {
                when {
                    !shizuku.available -> onOpenShizuku()
                    !shizuku.permissionGranted -> onRequestShizuku()
                    else -> onOpenShizuku()
                }
            }
        )
    )
    val done = steps.count { it.granted }

    LazyColumn(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = device.screenPadding,
            end = device.screenPadding,
            top = device.dp(28.dp),
            bottom = device.dp(32.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(device.dp(14.dp))
    ) {
        item(key = "hero") {
            Surface(
                shape = RoundedCornerShape(device.dp(40.dp)),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    Canvas(Modifier.matchParentSize()) {
                        drawCircle(
                            color = Lime.copy(alpha = 0.35f),
                            radius = size.minDimension * 0.44f,
                            center = Offset(size.width * 0.92f, size.height * 0.05f)
                        )
                        drawCircle(
                            color = Coral.copy(alpha = 0.14f),
                            radius = size.minDimension * 0.22f,
                            center = Offset(size.width * 0.06f, size.height * 0.96f)
                        )
                        val wave = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.72f)
                            cubicTo(
                                size.width * 0.22f, size.height * 0.56f,
                                size.width * 0.4f, size.height * 0.88f,
                                size.width * 0.62f, size.height * 0.7f
                            )
                            cubicTo(
                                size.width * 0.8f, size.height * 0.55f,
                                size.width * 0.9f, size.height * 0.8f,
                                size.width, size.height * 0.66f
                            )
                        }
                        drawPath(
                            wave,
                            color = scheme.primary.copy(alpha = 0.25f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(Modifier.padding(device.dp(24.dp))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AccentDot(color = Coral, size = 8.dp)
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = S["onboarding.eyebrow"],
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = S["onboarding.title"],
                            style = MaterialTheme.typography.displayLarge,
                            color = scheme.onSurface
                        )
                        Text(
                            text = S["app.tagline"],
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = S["onboarding.body"],
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "progress") {
            SetupProgress(done = done, total = steps.size)
        }

        items(steps.size, key = { "step-$it" }) { index ->
            PermissionStepCard(step = steps[index], index = index)
        }

        item(key = "postNotifications") {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = if (access.postNotifications) {
                    scheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    scheme.surfaceContainerLow
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = S["onboarding.post_title"],
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = S["onboarding.post_desc"],
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onRequestPostNotifications,
                        shape = CircleShape,
                        enabled = !access.postNotifications
                    ) {
                        Text(if (access.postNotifications) S["common.have"] else S["common.allow"])
                    }
                }
            }
        }

        if (shizukuReady) {
            item(key = "shizukuShortcuts") {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = scheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = S["onboarding.shizuku_active"],
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onGrantListener, shape = CircleShape) {
                                Text(S["settings.grant_listener"])
                            }
                            OutlinedButton(onClick = onGrantAccessibility, shape = CircleShape) {
                                Text(S["onboarding.accessibility_short"])
                            }
                        }
                    }
                }
            }
        }

        item(key = "actions") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onFinish,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(S["onboarding.enter"])
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                OutlinedButton(
                    onClick = onTest,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(S["onboarding.test"])
                }
                Text(
                    text = S["onboarding.footnote"],
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}
