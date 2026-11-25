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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.SelfTestDisclaimerBanner
import kotlin.math.cos
import kotlin.math.sin

private enum class FanPhase { INSTRUCTION, RESULT }
private const val FAN_LINE_COUNT = 18

/**
 * [Principio S - SRP] Unica responsabilidad: correr el autotest de disco de astigmatismo para
 * Astigmatismo (AST): dibuja lineas radiales y pregunta, ojo por ojo, si todas se ven igual de
 * nitidas. No calcula agudeza visual ni corre ninguna otra prueba.
 */
@Composable
fun AstigmatismFanTestScreen(navController: NavController, localizationRepository: LocalizationRepository) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    var phase by remember { mutableStateOf(FanPhase.INSTRUCTION) }
    var eyeIndex by remember { mutableIntStateOf(0) }
    val results = remember { mutableStateListOf<Boolean>() }
    val eyeNames = listOf(strings.selfTestEyeRight, strings.selfTestEyeLeft)

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.astigmatismTitle,
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
                FanPhase.INSTRUCTION -> {
                    Text(text = strings.astigmatismIntro, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = strings.selfTestCoverEyeInstruction.format(eyeNames[eyeIndex]) + " " + strings.astigmatismInstruction,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    AstigmatismFanCanvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                    Text(text = strings.astigmatismQuestion, style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(true, false).forEach { normal ->
                            OutlinedButton(
                                onClick = {
                                    results.add(normal)
                                    if (eyeIndex == eyeNames.lastIndex) phase = FanPhase.RESULT else eyeIndex++
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (normal) strings.selfTestYes else strings.selfTestNo)
                            }
                        }
                    }
                }
                FanPhase.RESULT -> {
                    eyeNames.forEachIndexed { index, eyeName ->
                        val normal = results.getOrNull(index) ?: true
                        Text(
                            text = "${eyeName.replaceFirstChar { it.uppercase() }}: " +
                                if (normal) strings.astigmatismResultNormal else strings.astigmatismResultAbnormal,
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
private fun AstigmatismFanCanvas(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.onBackground
    val dotColor = MaterialTheme.colorScheme.error
    val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.92f
        for (i in 0 until FAN_LINE_COUNT) {
            val angle = Math.PI * i / FAN_LINE_COUNT
            val dx = (radius * cos(angle)).toFloat()
            val dy = (radius * sin(angle)).toFloat()
            drawLine(
                lineColor,
                Offset(center.x - dx, center.y - dy),
                Offset(center.x + dx, center.y + dy),
                strokeWidth = strokeWidthPx
            )
        }
        drawCircle(dotColor, radius = size.minDimension / 60f, center = center)
    }
}
