package com.upn3.aplicacinparadetectarenfermedadesvisuales.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AppContainer
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AuthViewModelFactory
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.CameraAnalysisViewModelFactory
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.AnalysisResultScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.CameraScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.DiseaseInfoScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.HistoryScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.HomeScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.LoginScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.OnboardingScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.ProfileScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.RegisterScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens.SettingsScreen
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AuthViewModel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

/**
 * [Principio S - SRP] Unica responsabilidad: definir el grafo de navegacion (que ruta muestra
 * que pantalla y con que argumentos). No dibuja UI propia ni contiene logica de negocio.
 */
@Composable
fun AppNavHost(navController: NavHostController, appContainer: AppContainer) {
    // La sesion y el onboarding pueden venir persistidos en disco: hasta que ambos terminen de
    // leerse no sabemos si arrancar en "onboarding", "login" o "home". Mostrar un loader breve
    // acá es mejor que arrancar siempre en una pantalla y pegar un salto a otra apenas carga.
    val isSessionReady by appContainer.userSessionRepository.isReady.collectAsState()
    val isOnboardingReady by appContainer.onboardingRepository.isReady.collectAsState()
    if (!isSessionReady || !isOnboardingReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val cameraAnalysisViewModel: CameraAnalysisViewModel = viewModel(
        factory = CameraAnalysisViewModelFactory(appContainer)
    )
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContainer)
    )
    val startDestination = when {
        !appContainer.onboardingRepository.hasCompletedOnboarding.value -> "onboarding"
        appContainer.userSessionRepository.isLoggedIn.value -> "home"
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                appContainer.onboardingRepository.markCompleted()
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }
        composable("home") {
            HomeScreen(navController, appContainer.localizationRepository, appContainer.historyRepository)
        }
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
            ProfileScreen(
                navController,
                appContainer.userSessionRepository,
                appContainer.localizationRepository,
                appContainer.authRepository,
                appContainer.themeRepository,
                appContainer.historyRepository
            )
        }
        composable("settings") {
            SettingsScreen(navController, appContainer.localizationRepository, appContainer.historyRepository)
        }
    }
}
