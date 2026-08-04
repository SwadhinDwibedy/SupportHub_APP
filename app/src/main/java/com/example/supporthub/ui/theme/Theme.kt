package com.example.supporthub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(

    primary = Emerald600,
    onPrimary = White,

    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,

    secondary = Blue600,
    onSecondary = White,

    secondaryContainer = Blue100,
    onSecondaryContainer = Blue900,

    tertiary = Purple500,
    onTertiary = White,

    tertiaryContainer = Purple100,
    onTertiaryContainer = Purple900,

    background = Background,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = White,

    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Error,

    outline = Gray300,
    outlineVariant = Gray200,

    inverseSurface = Gray900,
    inverseOnSurface = White,

    scrim = Color.Black.copy(alpha = 0.45f)
)

private val DarkColors = darkColorScheme(

    primary = Emerald400,
    onPrimary = Gray900,

    primaryContainer = Emerald800,
    onPrimaryContainer = White,

    secondary = Blue400,
    onSecondary = Gray900,

    secondaryContainer = Blue900,
    onSecondaryContainer = White,

    tertiary = Purple400,
    onTertiary = Gray900,

    tertiaryContainer = Purple900,
    onTertiaryContainer = White,

    background = Color(0xFF0B1220),
    onBackground = White,


    surface = Color(0xFF111827),
    onSurface = White,

    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Gray300,

    error = Color(0xFFFF6B6B),
    onError = White,

    outline = Gray700
)

@Composable
fun SupportHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}