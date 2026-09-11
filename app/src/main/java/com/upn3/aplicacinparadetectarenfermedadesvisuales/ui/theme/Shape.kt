package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Tokens "rounded" del design system: sm=4, DEFAULT=8, md=12, lg=16, xl=24, full=pill.
// Botones/inputs usan md (12dp), tarjetas/modales usan lg (16dp) segun el spec.
val ClinicalShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val PillShape = RoundedCornerShape(percent = 50)
