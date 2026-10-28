package com.hmndev.chronika.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF1B1D17)
val Paper = Color(0xFFF7F4EA)
val Lime = Color(0xFFD7F26A)
val Forest = Color(0xFF2D4A32)
val Coral = Color(0xFFB84A2B)
val Clay = Color(0xFF6C5B3E)
val Moss = Color(0xFF44483C)
val Mist = Color(0xFFE4E6D8)
val Night = Color(0xFF0F120E)

val LightScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Lime,
    onPrimaryContainer = Color(0xFF1B2A12),
    inversePrimary = Color(0xFFB9D77A),
    secondary = Clay,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E0C8),
    onSecondaryContainer = Color(0xFF2A2014),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9CC),
    onTertiaryContainer = Color(0xFF3B1207),
    background = Paper,
    onBackground = Ink,
    surface = Color(0xFFFBF9F2),
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Moss,
    surfaceTint = Forest,
    inverseSurface = Color(0xFF303329),
    inverseOnSurface = Color(0xFFF2F1E7),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF74796B),
    outlineVariant = Color(0xFFC4C8B8),
    scrim = Color.Black,
    surfaceBright = Color(0xFFFBF9F2),
    surfaceDim = Color(0xFFDBD9CF),
    surfaceContainer = Color(0xFFF1EFE4),
    surfaceContainerHigh = Color(0xFFEBE9DE),
    surfaceContainerHighest = Color(0xFFE5E3D8),
    surfaceContainerLow = Color(0xFFF7F5EA),
    surfaceContainerLowest = Color.White
)

val DarkScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF1B2A12),
    primaryContainer = Color(0xFF3A4A22),
    onPrimaryContainer = Color(0xFFE6F5B0),
    inversePrimary = Forest,
    secondary = Color(0xFFE8C9A8),
    onSecondary = Color(0xFF3A2A18),
    secondaryContainer = Color(0xFF4A3B28),
    onSecondaryContainer = Color(0xFFF5E0C8),
    tertiary = Color(0xFFFFB59B),
    onTertiary = Color(0xFF5A1A08),
    tertiaryContainer = Color(0xFF7A2E1A),
    onTertiaryContainer = Color(0xFFFFD9CC),
    background = Night,
    onBackground = Color(0xFFE5E5DA),
    surface = Color(0xFF141713),
    onSurface = Color(0xFFE5E5DA),
    surfaceVariant = Color(0xFF2A3027),
    onSurfaceVariant = Color(0xFFC4C8B8),
    surfaceTint = Lime,
    inverseSurface = Color(0xFFE5E5DA),
    inverseOnSurface = Color(0xFF2A3027),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E9384),
    outlineVariant = Color(0xFF44483C),
    scrim = Color.Black,
    surfaceBright = Color(0xFF3A3E33),
    surfaceDim = Night,
    surfaceContainer = Color(0xFF1A1E18),
    surfaceContainerHigh = Color(0xFF242820),
    surfaceContainerHighest = Color(0xFF2E332A),
    surfaceContainerLow = Color(0xFF161A14),
    surfaceContainerLowest = Color(0xFF0B0E0A)
)
