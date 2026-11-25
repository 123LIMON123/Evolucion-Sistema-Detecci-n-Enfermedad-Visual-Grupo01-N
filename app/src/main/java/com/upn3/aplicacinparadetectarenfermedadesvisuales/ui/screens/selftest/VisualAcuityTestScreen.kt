package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.selftest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.SelfTestDisclaimerBanner

/** Altura en mm de una letra 20/20 a la distancia estandar de 6.096 m (20 pies), ~5 min de arco. */
private const val BASE_20_20_HEIGHT_MM = 8.73f
private const val BASE_DISTANCE_M = 6.096f

/** Niveles del mas facil (letra grande) al mas dificil (letra chica), con su multiplicador sobre 20/20. */
private val ACUITY_LEVELS = listOf(
    "20/200" to 10.0f,
    "20/100" to 5.0f,
    "20/70" to 3.5f,
    "20/40" to 2.0f,
    "20/30" to 1.5f,
    "20/20" to 1.0f
)

private val LETTER_POOL = listOf('E', 'F', 'P', 'T', 'O', 'Z', 'L', 'D', 'C', 'N')

private enum class AcuityPhase { INTRO, TESTING, RESULT }

/**
 * [Principio S - SRP] Unica responsabilidad: correr el autotest de agudeza visual (tipo Snellen)
 * para Miopia. Calcula el tamano fisico real de la letra a partir del DPI reportado por el
 * dispositivo (no de `sp`/escala de fuente del sistema), para que el tamano en pantalla sea
 * consistente sin importar el tamano de pantalla o si el usuario agrando la letra del sistema.
 */
@Composable
fun VisualAcuityTestScreen(navController: NavController, localizationRepository: LocalizationRepository) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val baseDensity = LocalDensity.current

    var phase by remember { mutableStateOf(AcuityPhase.INTRO) }
    var distanceMeters by remember { mutableFloatStateOf(2f) }
    var eyeIndex by remember { mutableIntStateOf(0) }
    var levelIndex by remember { mutableIntStateOf(0) }
    var lastCorrectLabel by remember { mutableStateOf<String?>(null) }
    val results = remember { mutableStateListOf<String>() }

    val eyeNames = listOf(strings.selfTestEyeRight, strings.selfTestEyeLeft)

    fun letterHeightSp(multiplier: Float): Float {
        val heightMm = BASE_20_20_HEIGHT_MM * (distanceMeters / BASE_DISTANCE_M) * multiplier
        val ydpi = context.resources.displayMetrics.ydpi
        val heightPx = heightMm / 25.4f * ydpi
        return heightPx / baseDensity.density
    }

    fun advance(correct: Boolean) {
        if (correct) {
            lastCorrectLabel = ACUITY_LEVELS[levelIndex].first
            if (levelIndex == ACUITY_LEVELS.lastIndex) {
                results.add(lastCorrectLabel ?: "< 20/200")
                lastCorrectLabel = null
                levelIndex = 0
                if (eyeIndex == eyeNames.lastIndex) phase = AcuityPhase.RESULT else eyeIndex++
            } else {
                levelIndex++
            }
        } else {
            results.add(lastCorrectLabel ?: "< 20/200")
            lastCorrectLabel = null
            levelIndex = 0
            if (eyeIndex == eyeNames.lastIndex) phase = AcuityPhase.RESULT else eyeIndex++
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.visualAcuityTitle,
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
                AcuityPhase.INTRO -> AcuityIntroStep(
                    distanceMeters = distanceMeters,
                    onDistanceChange = { distanceMeters = it },
                    onStart = { phase = AcuityPhase.TESTING }
                )
                AcuityPhase.TESTING -> AcuityTestingStep(
                    eyeName = eyeNames[eyeIndex],
                    letterHeightSp = letterHeightSp(ACUITY_LEVELS[levelIndex].second),
                    coverEyeInstruction = strings.selfTestCoverEyeInstruction.format(eyeNames[eyeIndex]),
                    questionText = strings.visualAcuityQuestion,
                    onAnswer = { correct -> advance(correct) }
                )
                AcuityPhase.RESULT -> AcuityResultStep(
                    eyeNames = eyeNames,
                    results = results,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun AcuityIntroStep(distanceMeters: Float, onDistanceChange: (Float) -> Unit, onStart: () -> Unit) {
    val strings = LocalAppStrings.current
    Text(text = strings.visualAcuityIntro, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = strings.visualAcuityChooseDistance, style = MaterialTheme.typography.titleSmall)
    val options = listOf(1f to strings.visualAcuityDistance1m, 2f to strings.visualAcuityDistance2m, 3f to strings.visualAcuityDistance3m)
    options.forEach { (meters, label) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = distanceMeters == meters, onClick = { onDistanceChange(meters) })
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
        Text(strings.visualAcuityStartAction)
    }
}

@Composable
private fun AcuityTestingStep(
    eyeName: String,
    letterHeightSp: Float,
    coverEyeInstruction: String,
    questionText: String,
    onAnswer: (Boolean) -> Unit
) {
    val choices = remember(eyeName, letterHeightSp) { LETTER_POOL.shuffled().take(4) }
    val target = choices[0]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = coverEyeInstruction, style = MaterialTheme.typography.bodyMedium)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalDensity provides Density(density = LocalDensity.current.density, fontScale = 1f)) {
            Text(text = target.toString(), fontSize = letterHeightSp.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }

    Text(text = questionText, style = MaterialTheme.typography.titleSmall)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.shuffled().forEach { letter ->
            OutlinedButton(
                onClick = { onAnswer(letter == target) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(letter.toString())
            }
        }
    }
}

@Composable
private fun AcuityResultStep(eyeNames: List<String>, results: List<String>, onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    Text(text = strings.visualAcuityResultTitle, style = MaterialTheme.typography.titleMedium)
    eyeNames.forEachIndexed { index, eyeName ->
        val result = results.getOrNull(index) ?: "—"
        Text(text = strings.visualAcuityResultLine.format(eyeName.replaceFirstChar { it.uppercase() }, result), style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = strings.visualAcuityResultDisclaimer,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(strings.selfTestBackToInfoAction)
    }
}
