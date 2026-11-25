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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.SelfTestDisclaimerBanner

/**
 * [Principio S - SRP] Unica responsabilidad: mostrar, honestamente, que una condicion (Diabetes,
 * Glaucoma o Hipertension) no tiene un autotest casero confiable, y que hace falta equipo
 * clinico para evaluarla. No intenta simular un test falso solo para tener contenido.
 */
@Composable
fun NoReliableSelfTestScreen(navController: NavController, localizationRepository: LocalizationRepository, diseaseCode: String) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    val whatIsNeededText = when (diseaseCode) {
        "D" -> strings.noReliableTestDiabetes
        "G" -> strings.noReliableTestGlaucoma
        "H" -> strings.noReliableTestHypertension
        else -> strings.noReliableTestDiabetes
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.noReliableTestTitle,
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
            Text(text = strings.noReliableTestIntro, style = MaterialTheme.typography.bodyMedium)
            Text(text = strings.noReliableTestWhatIsNeeded, style = MaterialTheme.typography.titleSmall)
            Text(text = whatIsNeededText, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.selfTestBackToInfoAction)
            }
        }
    }
}
