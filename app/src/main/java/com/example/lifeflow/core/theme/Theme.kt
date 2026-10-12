package com.example.lifeflow.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = CardLight,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurplePrimaryDark,
    secondary = BlueSecondary,
    onSecondary = CardLight,
    secondaryContainer = BlueLight,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = CardLight,
    onSurface = TextDark,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = CardLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = CardLight,
    primaryContainer = PurplePrimaryDark,
    onPrimaryContainer = PurpleLight,
    secondary = BlueSecondary,
    onSecondary = CardLight,
    secondaryContainer = BlueSecondary,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = CardDark,
    onSurface = TextLight,
    surfaceVariant = BorderDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed,
    onError = CardLight
)

@Composable
fun LifeFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
