package com.hmndev.chronika.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmndev.chronika.data.AppSettings
import com.hmndev.chronika.data.SettingsStore

enum class ScreenClass { COMPACT, MEDIUM, EXPANDED }

/**
 * Dimensions derived from the real device screen. When [adaptive] is on, base
 * dimensions are multiplied by [scale] so paddings, cards, the hero and the dock
 * fit the actual phone instead of assuming one reference size.
 */
data class DeviceProfile(
    val widthDp: Int,
    val heightDp: Int,
    val density: Float,
    val scale: Float,
    val screenClass: ScreenClass,
    val adaptive: Boolean,
) {
    fun dp(value: Dp): Dp = (value.value * scale).dp
    fun dp(value: Float): Dp = (value * scale).dp

    val heroHeight: Dp
        get() {
            val base = when (screenClass) {
                ScreenClass.COMPACT -> 232f
                ScreenClass.MEDIUM -> 250f
                ScreenClass.EXPANDED -> 268f
            }
            val capped = minOf(base * scale, heightDp * 0.38f)
            return capped.dp
        }

    val screenPadding: Dp
        get() = when (screenClass) {
            ScreenClass.COMPACT -> dp(16f)
            ScreenClass.MEDIUM -> dp(20f)
            ScreenClass.EXPANDED -> dp(28f)
        }

    val cardPadding: Dp get() = dp(16f)
    val sectionSpacing: Dp get() = dp(18f)
    val dockBottomPadding: Dp get() = dp(14f)
    val contentMaxWidth: Dp
        get() = if (screenClass == ScreenClass.EXPANDED) 760.dp else Dp.Unspecified

    val summary: String
        get() = "$widthDp × $heightDp dp"
}

val LocalDeviceProfile = staticCompositionLocalOf {
    DeviceProfile(400, 800, 2.75f, 1f, ScreenClass.COMPACT, false)
}

@Composable
fun rememberDeviceProfile(settings: AppSettings? = null): DeviceProfile {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val observedSettings by SettingsStore.settings.collectAsState()
    val activeSettings = settings ?: observedSettings
    val width = configuration.screenWidthDp
    val height = configuration.screenHeightDp
    val screenClass = when {
        width < 400 -> ScreenClass.COMPACT
        width < 600 -> ScreenClass.MEDIUM
        else -> ScreenClass.EXPANDED
    }
    val auto = ((width / 400f) * 0.65f + (height / 860f) * 0.35f)
        .coerceIn(0.86f, 1.14f)
    val scale = if (activeSettings.adaptiveLayout) {
        (auto * activeSettings.uiScale).coerceIn(0.8f, 1.25f)
    } else {
        1f
    }
    return DeviceProfile(width, height, density, scale, screenClass, activeSettings.adaptiveLayout)
}
