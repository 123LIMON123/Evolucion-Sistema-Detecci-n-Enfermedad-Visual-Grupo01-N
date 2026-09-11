package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.StatBadgeTextStyle

/**
 * Una fila del "Multi-Class Probability Breakdown Meter": nombre de la condicion, valor de
 * confianza (estilo stat-badge compacto) y una barra de progreso cuyo color cambia segun el
 * umbral de sospecha clinica (no confundir con el umbral de riesgo del veredicto principal).
 */
@Composable
fun ProbabilityMeterRow(name: String, confidence: Float, modifier: Modifier = Modifier) {
    val tier = probabilityMeterTier(confidence)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.labelMedium)
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = StatBadgeTextStyle.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = tier.accent
            )
        }
        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = tier.accent,
            trackColor = tier.tint
        )
    }
}
