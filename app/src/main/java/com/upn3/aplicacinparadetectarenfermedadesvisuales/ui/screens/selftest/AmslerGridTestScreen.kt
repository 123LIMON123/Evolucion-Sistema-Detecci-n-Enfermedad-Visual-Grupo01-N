package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.selftest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.SelfTestDisclaimerBanner

private enum class GridPhase { INSTRUCTION, RESULT }

/**
 * [Principio S - SRP] Unica responsabilidad: correr el autotest de rejilla de Amsler para
 * Degeneracion Macular (A): dibuja la rejilla con un punto central y pregunta, ojo por ojo, si el
 * usuario nota distorsion. No calcula nada de agudeza visual ni de otras pruebas.
 */
@Composable
fun AmslerGridTestScreen(navController: NavController, localizationRepository: LocalizationRepository) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    var phase by remember { mutableStateOf(GridPhase.INSTRUCTION) }
    var eyeIndex by remember { mutableIntStateOf(0) }
    val results = remember { mutableStateListOf<Boolean>() }
    val eyeNames = listOf(strings.selfTestEyeRight, strings.selfTestEyeLeft)

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.amslerTitle,
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelfTestDisclaimerBanner(text = strings.selfTestDisclaimer)

            when (phase) {
                GridPhase.INSTRUCTION -> {
                    Text(text = strings.amslerIntro, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = strings.selfTestCoverEyeInstruction.format(eyeNames[eyeIndex]) + " " + strings.amslerInstruction,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    AmslerGridCanvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                    Text(text = strings.amslerQuestion, style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(true, false).forEach { normal ->
                            OutlinedButton(
                                onClick = {
                                    results.add(normal)
                                    if (eyeIndex == eyeNames.lastIndex) phase = GridPhase.RESULT else eyeIndex++
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (normal) strings.selfTestYes else strings.selfTestNo)
                            }
                        }
                    }
                }
                GridPhase.RESULT -> {
                    eyeNames.forEachIndexed { index, eyeName ->
                        val normal = results.getOrNull(index) ?: true
                        Text(
                            text = "${eyeName.replaceFirstChar { it.uppercase() }}: " +
                                if (normal) strings.amslerResultNormal else strings.amslerResultAbnormal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.selfTestBackToInfoAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmslerGridCanvas(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.onBackground
    val dotColor = MaterialTheme.colorScheme.error
    Canvas(modifier = modifier) {
        val step = size.width / 20f
        for (i in 0..20) {
            drawLine(lineColor, Offset(i * step, 0f), Offset(i * step, size.height), strokeWidth = Stroke.HairlineWidth)
            drawLine(lineColor, Offset(0f, i * step), Offset(size.width, i * step), strokeWidth = Stroke.HairlineWidth)
        }
        drawCircle(dotColor, radius = size.minDimension / 60f, center = Offset(size.width / 2f, size.height / 2f))
    }
}
