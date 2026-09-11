package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EyeBlue80,
    onPrimary = EyeBlue20,
    secondary = EyeTeal80,
    onSecondary = EyeTeal20,
    tertiary = EyeAmber80,
    onTertiary = EyeAmber40,
    background = EyeNeutral10,
    onBackground = EyeNeutral90,
    surface = EyeNeutral10,
    onSurface = EyeNeutral90,
    surfaceVariant = EyeNeutral20,
    onSurfaceVariant = EyeNeutral90,
    error = EyeError80,
    onError = EyeError20,
)

private val LightColorScheme = lightColorScheme(
    primary = EyeBlue40,
    onPrimary = Color.White,
    secondary = EyeTeal40,
    onSecondary = Color.White,
    tertiary = EyeAmber40,
    onTertiary = Color.White,
    background = EyeNeutral99,
    onBackground = EyeNeutral10,
    surface = EyeNeutral99,
    onSurface = EyeNeutral10,
    surfaceVariant = EyeNeutral95,
    onSurfaceVariant = EyeNeutral20,
    error = EyeError40,
    onError = Color.White,
)

/**
 * Tema de marca de la app: paleta fija "eye care" (no usamos Material You dinámico) para que la
 * identidad visual sea consistente en cualquier dispositivo.
 */
@Composable
fun AplicaciónParaDetectarEnfermedadesVisualesTheme(
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
        content = content
    )
}
