package com.upn3.aplicacinparadetectarenfermedadesvisuales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.ProvideLocalization
import com.upn3.aplicacinparadetectarenfermedadesvisuales.navigation.AppNavHost
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.OcuCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as EyeDiseaseApp).container
        setContent {
            OcuCheckTheme {
                ProvideLocalization(appContainer.localizationRepository) {
                    val navController = rememberNavController()
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AppNavHost(navController, appContainer)
                        }
                    }
                }
            }
        }
    }
}
