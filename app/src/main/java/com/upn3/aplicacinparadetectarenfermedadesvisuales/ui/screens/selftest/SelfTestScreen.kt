package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.selftest

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiseaseCatalog
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.SelfTestType

/**
 * [Principio S - SRP] Unica responsabilidad: leer que [SelfTestType] le corresponde a un codigo
 * de enfermedad y mostrar la pantalla de autotest adecuada. No conoce la logica interna de
 * ninguna prueba especifica: eso vive en cada pantalla de `ui/screens/selftest`.
 *
 * [Principio O - OCP] Agregar un autotest nuevo es agregar un valor a [SelfTestType] y un `when`
 * branch aqui, sin tocar [DiseaseInfoScreen] ni el resto de las pantallas existentes.
 */
@Composable
fun SelfTestScreen(navController: NavController, localizationRepository: LocalizationRepository, diseaseCode: String) {
    when (DiseaseCatalog.byCode(diseaseCode)?.selfTest) {
        SelfTestType.VISUAL_ACUITY -> VisualAcuityTestScreen(navController, localizationRepository)
        SelfTestType.AMSLER_GRID -> AmslerGridTestScreen(navController, localizationRepository)
        SelfTestType.ASTIGMATISM_FAN -> AstigmatismFanTestScreen(navController, localizationRepository)
        SelfTestType.DRY_EYE_QUESTIONNAIRE -> DryEyeQuestionnaireScreen(navController, localizationRepository)
        SelfTestType.NO_RELIABLE_SELF_TEST, null ->
            NoReliableSelfTestScreen(navController, localizationRepository, diseaseCode)
    }
}
