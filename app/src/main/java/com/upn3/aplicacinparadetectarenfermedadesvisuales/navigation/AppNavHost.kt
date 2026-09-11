package com.upn3.aplicacinparadetectarenfermedadesvisuales.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AppContainer
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.CameraAnalysisViewModelFactory
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.AnalysisResultScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.CameraScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.DiseaseInfoScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.HistoryScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.HomeScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.LoginScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.ProfileScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.RegisterScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

@Composable
fun AppNavHost(navController: NavHostController, appContainer: AppContainer) {
    val cameraAnalysisViewModel: CameraAnalysisViewModel = viewModel(
        factory = CameraAnalysisViewModelFactory(appContainer)
    )

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, appContainer.userSessionRepository) }
        composable("register") { RegisterScreen(navController, appContainer.userSessionRepository) }
        composable("home") { HomeScreen(navController, appContainer.localizationRepository) }
        composable("camera") {
            CameraScreen(
                navController,
                cameraAnalysisViewModel,
                appContainer.historyRepository,
                appContainer.localizationRepository
            )
        }
        composable("analysis_result/{diseaseCode}/{confidence}/{riskLevel}") { backStackEntry ->
            val diseaseCode = backStackEntry.arguments?.getString("diseaseCode") ?: ""
            val confidence = backStackEntry.arguments?.getString("confidence")?.toFloatOrNull() ?: 0f
            val riskLevel = backStackEntry.arguments?.getString("riskLevel")?.let {
                runCatching { RiskLevel.valueOf(it) }.getOrNull()
            } ?: RiskLevel.BAJO_RIESGO
            AnalysisResultScreen(navController, diseaseCode, confidence, riskLevel, appContainer.localizationRepository)
        }
        composable("disease_info") { DiseaseInfoScreen(navController, appContainer.localizationRepository) }
        composable("history") {
            HistoryScreen(navController, appContainer.historyRepository, appContainer.localizationRepository)
        }
        composable("profile") {
            ProfileScreen(navController, appContainer.userSessionRepository, appContainer.localizationRepository)
        }
    }
}
