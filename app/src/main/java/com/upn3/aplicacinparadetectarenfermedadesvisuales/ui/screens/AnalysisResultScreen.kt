package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiseaseCatalog
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.RiskHighColor
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.RiskLowColor
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.RiskModerateColor

private fun colorFor(riskLevel: RiskLevel): Color = when (riskLevel) {
    RiskLevel.BAJO_RIESGO -> RiskLowColor
    RiskLevel.RIESGO_MODERADO -> RiskModerateColor
    RiskLevel.SOSPECHA_ALTA -> RiskHighColor
}

private fun labelFor(riskLevel: RiskLevel, strings: AppStrings): String = when (riskLevel) {
    RiskLevel.BAJO_RIESGO -> strings.riskLow
    RiskLevel.RIESGO_MODERADO -> strings.riskModerate
    RiskLevel.SOSPECHA_ALTA -> strings.riskHigh
}

@Composable
fun AnalysisResultScreen(
    navController: NavController,
    diseaseCode: String,
    confidence: Float,
    riskLevel: RiskLevel,
    localizationRepository: LocalizationRepository
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val diseaseName = DiseaseCatalog.byCode(diseaseCode)?.localized(language)?.name ?: diseaseCode
    val riskColor = colorFor(riskLevel)

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.resultTitle,
                language = language,
                onToggleLanguage = { localizationRepository.toggle() },
                backContentDescription = strings.back,
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = strings.probableDetection, style = MaterialTheme.typography.titleLarge)
            Text(
                text = diseaseName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = riskColor,
                trackColor = riskColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${strings.confidenceLabel}: ${(confidence * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(12.dp))
            RiskBadge(riskLevel = riskLevel, label = labelFor(riskLevel, strings), color = riskColor)

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(strings.newAnalysisAction)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.navigate("disease_info") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(strings.moreInfoAction)
            }
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel, label: String, color: Color) {
    val icon = when (riskLevel) {
        RiskLevel.BAJO_RIESGO -> Icons.Filled.CheckCircle
        RiskLevel.RIESGO_MODERADO -> Icons.Filled.Info
        RiskLevel.SOSPECHA_ALTA -> Icons.Filled.Warning
    }
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
