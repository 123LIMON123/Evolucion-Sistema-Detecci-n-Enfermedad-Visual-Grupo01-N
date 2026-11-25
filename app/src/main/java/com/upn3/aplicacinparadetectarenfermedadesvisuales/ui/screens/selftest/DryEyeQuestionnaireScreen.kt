package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.selftest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.SelfTestDisclaimerBanner

private enum class DryEyePhase { QUESTIONS, RESULT }

/**
 * [Principio S - SRP] Unica responsabilidad: correr el cuestionario de sintomas de Ojo Seco
 * (DRY): suma la frecuencia reportada de cada sintoma (Nunca=0, A veces=1, Frecuentemente=2) y
 * clasifica el resultado en bajo/moderado/alto. No es una escala clinica validada (tipo OSDI), es
 * un cuestionario de orientacion propio de esta app.
 */
@Composable
fun DryEyeQuestionnaireScreen(navController: NavController, localizationRepository: LocalizationRepository) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    val questions = listOf(strings.dryEyeQ1, strings.dryEyeQ2, strings.dryEyeQ3, strings.dryEyeQ4, strings.dryEyeQ5, strings.dryEyeQ6)
    var questionIndex by remember { mutableIntStateOf(0) }
    val scores = remember { mutableStateListOf<Int>() }
    var phase by remember { mutableStateOf(DryEyePhase.QUESTIONS) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.dryEyeTitle,
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
                DryEyePhase.QUESTIONS -> {
                    Text(text = strings.dryEyeIntro, style = MaterialTheme.typography.bodyMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "${questionIndex + 1}/${questions.size}", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = questions[questionIndex], style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            strings.dryEyeFreqNever to 0,
                            strings.dryEyeFreqSometimes to 1,
                            strings.dryEyeFreqOften to 2
                        ).forEach { (label, score) ->
                            OutlinedButton(
                                onClick = {
                                    scores.add(score)
                                    if (questionIndex == questions.lastIndex) {
                                        phase = DryEyePhase.RESULT
                                    } else {
                                        questionIndex++
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
                DryEyePhase.RESULT -> {
                    val total = scores.sum()
                    val resultText = when {
                        total <= 3 -> strings.dryEyeResultLow
                        total <= 7 -> strings.dryEyeResultModerate
                        else -> strings.dryEyeResultHigh
                    }
                    Text(text = strings.dryEyeResultTitle, style = MaterialTheme.typography.titleMedium)
                    Text(text = resultText, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.selfTestBackToInfoAction)
                    }
                }
            }
        }
    }
}
