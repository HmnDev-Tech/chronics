package com.hmndev.chronika.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Core brand palette (preserved names + expressive M3 tonal architecture)
val Ink = Color(0xFF12150F)
val Paper = Color(0xFFF6F4EC)
val Lime = Color(0xFFC8FF2E)
val Forest = Color(0xFF1E4628)
val Coral = Color(0xFFFF5436)
val Clay = Color(0xFF5C4278)
val Moss = Color(0xFF3E4537)
val Mist = Color(0xFFE3E8D8)
val Night = Color(0xFF0B0E09)

// Expressive Material 3 accent tokens
val ElectricLime = Lime
val HyperCoral = Coral
val BerryViolet = Clay
val SolarAmber = Color(0xFFFFB800)
val MintFoam = Color(0xFF8EF3B8)
val DeepForest = Color(0xFF132D19)
val ObsidianNight = Color(0xFF141811)

val LightScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color(0xFFF9FCF4),
    primaryContainer = Lime,
    onPrimaryContainer = Color(0xFF142409),
    inversePrimary = Color(0xFFB5EC3D),
    secondary = Clay,
    onSecondary = Color(0xFFFCF8FF),
    secondaryContainer = Color(0xFFEBDCFF),
    onSecondaryContainer = Color(0xFF281242),
    tertiary = Coral,
    onTertiary = Color(0xFFFFF8F6),
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF410A00),
    background = Paper,
    onBackground = Ink,
    surface = Color(0xFFFAF8F1),
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Moss,
    surfaceTint = Forest,
    inverseSurface = Color(0xFF262A20),
    inverseOnSurface = Color(0xFFF2F1E7),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF717768),
    outlineVariant = Color(0xFFC1C6B4),
    scrim = Color.Black,
    surfaceBright = Color(0xFFFDFBF5),
    surfaceDim = Color(0xFFDAD8CD),
    surfaceContainer = Color(0xFFEEECE1),
    surfaceContainerHigh = Color(0xFFE6E4D7),
    surfaceContainerHighest = Color(0xFFDDE0D0),
    surfaceContainerLow = Color(0xFFF5F3E9),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

val DarkScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF152607),
    primaryContainer = Color(0xFF2E4916),
    onPrimaryContainer = Color(0xFFDCFF7D),
    inversePrimary = Forest,
    secondary = Color(0xFFD8BAFF),
    onSecondary = Color(0xFF3A1E5A),
    secondaryContainer = Color(0xFF4D316F),
    onSecondaryContainer = Color(0xFFF0E2FF),
    tertiary = Color(0xFFFF8F77),
    onTertiary = Color(0xFF5A1102),
    tertiaryContainer = Color(0xFF7E240F),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Night,
    onBackground = Color(0xFFE7EAE0),
    surface = ObsidianNight,
    onSurface = Color(0xFFE7EAE0),
    surfaceVariant = Color(0xFF282E23),
    onSurfaceVariant = Color(0xFFC2C8B7),
    surfaceTint = Lime,
    inverseSurface = Color(0xFFE7EAE0),
    inverseOnSurface = Color(0xFF23271F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8C9282),
    outlineVariant = Color(0xFF41473A),
    scrim = Color.Black,
    surfaceBright = Color(0xFF33392E),
    surfaceDim = Night,
    surfaceContainer = Color(0xFF191E16),
    surfaceContainerHigh = Color(0xFF23291F),
    surfaceContainerHighest = Color(0xFF2E3529),
    surfaceContainerLow = Color(0xFF141812),
    surfaceContainerLowest = Color(0xFF080A06)
)
