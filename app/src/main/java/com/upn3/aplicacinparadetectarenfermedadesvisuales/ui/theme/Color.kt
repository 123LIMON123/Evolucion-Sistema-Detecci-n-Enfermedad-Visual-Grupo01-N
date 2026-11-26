package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme

import androidx.compose.ui.graphics.Color

// [Principio S - SRP] Unica responsabilidad de este archivo: declarar los tokens de color del
// design system "OcuCheck AI" (paleta clinica). La variante clara sigue siendo la pensada para
// consultorios/clinicas con buena iluminacion; en v1.1 se agrega una variante oscura (mas abajo)
// para uso personal fuera de ese contexto, donde la preferencia del usuario pesa mas que la
// calma visual de un consultorio. No arma ColorScheme ni sabe donde se usa cada color; eso vive
// en Theme.kt.
val Surface = Color(0xFFF7F9FB)
val SurfaceDim = Color(0xFFD8DADC)
val SurfaceBright = Color(0xFFF7F9FB)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF2F4F6)
val SurfaceContainer = Color(0xFFECEEF0)
val SurfaceContainerHigh = Color(0xFFE6E8EA)
val SurfaceContainerHighest = Color(0xFFE0E3E5)
val OnSurface = Color(0xFF191C1E)
val OnSurfaceVariant = Color(0xFF3E494B)
val InverseSurface = Color(0xFF2D3133)
val InverseOnSurface = Color(0xFFEFF1F3)
val Outline = Color(0xFF6E797B)
val OutlineVariant = Color(0xFFBDC8CB)
val SurfaceTint = Color(0xFF0E8CA6)

// v1.2: paleta "teal -> indigo" tomada de verdad del degradado del logo (muestreo de pixeles
// real: dominante #18A8C0/#18C0C0 en el aro/lupa, #001860/#180090 en la pupila). Primary es el
// teal (oscurecido un poco para contraste de texto blanco encima), Tertiary es el indigo: el
// degrade teal->indigo de HomeHeroCard/Login/Register queda igual que el del logo.
val Primary = Color(0xFF0E8CA6)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF14A6BD)
val OnPrimaryContainer = Color(0xFFECFDFF)
val InversePrimary = Color(0xFF7DE0EC)

val Secondary = Color(0xFF545F73)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFD5E0F8)
val OnSecondaryContainer = Color(0xFF586377)

val Tertiary = Color(0xFF23267A)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF2E3294)
val OnTertiaryContainer = Color(0xFFF2F2FF)

val AppError = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

val PrimaryFixed = Color(0xFFA7EEF5)
val PrimaryFixedDim = Color(0xFF6AD4E0)
val OnPrimaryFixed = Color(0xFF001F24)
val OnPrimaryFixedVariant = Color(0xFF00525F)

val SecondaryFixed = Color(0xFFD8E3FB)
val SecondaryFixedDim = Color(0xFFBCC7DE)
val OnSecondaryFixed = Color(0xFF111C2D)
val OnSecondaryFixedVariant = Color(0xFF3C475A)

val TertiaryFixed = Color(0xFFCCE5FF)
val TertiaryFixedDim = Color(0xFF93CCFF)
val OnTertiaryFixed = Color(0xFF001D31)
val OnTertiaryFixedVariant = Color(0xFF004B73)

val Background = Color(0xFFF7F9FB)
val OnBackground = Color(0xFF191C1E)
val SurfaceVariant = Color(0xFFE0E3E5)

// Jerarquia clinica de alertas (independiente de los roles M3, usada por chips/medidores).
val ClinicalHealthy = Color(0xFF059669)
val ClinicalHealthyTint = Color(0xFFECFDF5)
val ClinicalHealthyText = Color(0xFF065F46)

val ClinicalWarning = Color(0xFFD97706)
val ClinicalWarningTint = Color(0xFFFFFBEB)
val ClinicalWarningText = Color(0xFF92400E)

val ClinicalCritical = Color(0xFFDC2626)
val ClinicalCriticalTint = Color(0xFFFEF2F2)
val ClinicalCriticalText = Color(0xFF991B1B)

val ClinicalNeutral = Color(0xFF64748B)
val ClinicalNeutralTint = Color(0xFFF1F5F9)
val ClinicalNeutralText = Color(0xFF334155)

// Camera viewfinder
val ViewfinderScrim = Color(0xCC0F172A)
val ViewfinderPositioning = Color(0xFF0284C7)
val ViewfinderReady = Color(0xFF10B981)
val ViewfinderAlert = Color(0xFFF59E0B)

// Variante oscura (v1.1): reutiliza los roles "inverse"/"fixed" ya definidos arriba donde
// encajan (son los mismos tonos que M3 usaria para un ColorScheme oscuro de esta paleta), y
// agrega solo los que faltaban.
val DarkBackground = Color(0xFF101416)
val DarkOnBackground = Color(0xFFE2E5E6)
val DarkSurface = Color(0xFF101416)
val DarkSurfaceVariant = Color(0xFF3E494B)
val DarkOnSurfaceVariant = Color(0xFFBDC8CB)
val DarkSurfaceContainerLowest = Color(0xFF0B0E10)
val DarkSurfaceContainerLow = Color(0xFF191C1E)
val DarkSurfaceContainer = Color(0xFF1D2022)
val DarkSurfaceContainerHigh = Color(0xFF282B2D)
val DarkSurfaceContainerHighest = Color(0xFF333638)
val DarkOutline = Color(0xFF889295)
val DarkOutlineVariant = Color(0xFF3E494B)
val DarkPrimaryContainer = Color(0xFF00525F)
val DarkSecondaryContainer = Color(0xFF3C475A)
val DarkTertiaryContainer = Color(0xFF004B73)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
