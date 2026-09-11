package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Jerarquia de alerta clinica del design system (independiente de los roles de color de
 * Material3): cada nivel trae su color de acento, su tinte de fondo y su color de texto sobre
 * ese tinte, ya combinados para cumplir WCAG AA.
 */
enum class ClinicalTier(val accent: Color, val tint: Color, val onTint: Color) {
    HEALTHY(ClinicalHealthy, ClinicalHealthyTint, ClinicalHealthyText),
    WARNING(ClinicalWarning, ClinicalWarningTint, ClinicalWarningText),
    CRITICAL(ClinicalCritical, ClinicalCriticalTint, ClinicalCriticalText),
    NEUTRAL(ClinicalNeutral, ClinicalNeutralTint, ClinicalNeutralText)
}
