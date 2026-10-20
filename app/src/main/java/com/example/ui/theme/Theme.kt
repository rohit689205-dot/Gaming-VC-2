package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VcColorScheme = darkColorScheme(
    primary = VcPrimaryPurple,
    onPrimary = VcTextPrimary,
    secondary = VcSecondaryViolet,
    onSecondary = VcTextPrimary,
    background = VcBackground,
    onBackground = VcTextPrimary,
    surface = VcSurfaceCard,
    onSurface = VcTextPrimary,
    surfaceVariant = VcElevatedPanel,
    onSurfaceVariant = VcTextSecondary,
    outline = VcBorderSubtle,
    error = VcErrorRed,
    onError = VcTextPrimary
)

@Composable
fun VcConnectTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VcBackground.toArgb()
            window.navigationBarColor = VcBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = VcColorScheme,
        typography = Typography,
        content = content
    )
}
