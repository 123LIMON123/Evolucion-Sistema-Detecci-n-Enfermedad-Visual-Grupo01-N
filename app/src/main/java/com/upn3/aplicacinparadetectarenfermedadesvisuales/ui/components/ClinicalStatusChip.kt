package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ClinicalTier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.PillShape

fun RiskLevel.toClinicalTier(): ClinicalTier = when (this) {
    RiskLevel.BAJO_RIESGO -> ClinicalTier.HEALTHY
    RiskLevel.RIESGO_MODERADO -> ClinicalTier.WARNING
    RiskLevel.SOSPECHA_ALTA -> ClinicalTier.CRITICAL
}

fun RiskLevel.label(strings: AppStrings): String = when (this) {
    RiskLevel.BAJO_RIESGO -> strings.riskLow
    RiskLevel.RIESGO_MODERADO -> strings.riskModerate
    RiskLevel.SOSPECHA_ALTA -> strings.riskHigh
}

/** Umbrales propios del medidor de probabilidades (distintos de los umbrales clinicos del
 * veredicto principal): aqui son puramente de presentacion, para que el desglose completo de
 * clases no se vea todo en rojo. */
fun probabilityMeterTier(confidence: Float): ClinicalTier = when {
    confidence > 0.45f -> ClinicalTier.CRITICAL
    confidence >= 0.15f -> ClinicalTier.WARNING
    else -> ClinicalTier.HEALTHY
}

@Composable
fun ClinicalStatusChip(tier: ClinicalTier, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(28.dp)
            .background(tier.tint, PillShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tier.accent, CircleShape)
        )
        Box(modifier = Modifier.width(6.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = tier.onTint,
            textAlign = TextAlign.Center
        )
    }
}
