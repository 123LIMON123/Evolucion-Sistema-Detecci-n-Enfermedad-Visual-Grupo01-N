package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// [Principio S - SRP] Unica responsabilidad de este archivo: ensamblar los ColorScheme de
// Material3 (claro y oscuro) a partir de los tokens de Color.kt y exponer el composable de tema
// de la app. El esquema claro sigue siendo el pensado para consultorios/clinicas con buena
// iluminacion; el oscuro (v1.1) es para uso personal, activado por preferencia del usuario via
// [ThemeRepository], nunca automatico segun el tema del sistema.
private val ClinicalColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = AppError,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest
    // Los roles "fixed" (primaryFixed, secondaryFixed, tertiaryFixed, etc.) del spec no existen
    // en la version de Material3 de este proyecto; quedan disponibles como Color sueltos en
    // Color.kt para usarlos directamente donde se necesiten.
)

// Reutiliza los tonos "inverse"/"fixed" de Color.kt donde encajan (son los mismos valores que
// Material3 generaria para un ColorScheme oscuro de esta paleta) en vez de inventar una paleta
// nueva sin relacion con la marca clara.
private val ClinicalDarkColorScheme = darkColorScheme(
    primary = InversePrimary,
    onPrimary = OnPrimaryFixed,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = PrimaryFixed,
    inversePrimary = Primary,
    secondary = SecondaryFixedDim,
    onSecondary = OnSecondaryFixed,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = SecondaryFixed,
    tertiary = TertiaryFixedDim,
    onTertiary = OnTertiaryFixed,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = TertiaryFixed,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = InverseOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = InversePrimary,
    inverseSurface = Surface,
    inverseOnSurface = OnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceDim = DarkBackground,
    surfaceBright = DarkSurfaceContainerHighest,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

@Composable
fun OcuCheckTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) ClinicalDarkColorScheme else ClinicalColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ClinicalShapes,
        content = content
    )
}
