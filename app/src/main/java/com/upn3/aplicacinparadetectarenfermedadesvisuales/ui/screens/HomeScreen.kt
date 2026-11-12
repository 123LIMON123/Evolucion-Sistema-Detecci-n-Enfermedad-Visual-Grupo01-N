package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar

private data class HomeMenuItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accent: Color,
    val route: String
)

/**
 * [Principio S - SRP] Unica responsabilidad: mostrar el menu principal de navegacion (nuevo
 * analisis, historial, enfermedades, perfil). No contiene logica de negocio ni de otras pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    localizationRepository: LocalizationRepository,
    historyRepository: AnalysisHistoryRepository
) {
    val strings = LocalAppStrings.current
    val history by historyRepository.history.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.appTitle,
                language = LocalAppLanguage.current,
                onToggleLanguage = { localizationRepository.toggle() },
                backContentDescription = strings.back
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            HomeHeroCard(
                welcome = strings.homeWelcome,
                subtitle = strings.homeSubtitle,
                analysesLabel = if (history.isEmpty()) {
                    strings.noHistoryYet
                } else {
                    strings.homeAnalysesCountLabel.format(history.size)
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            val items = listOf(
                HomeMenuItem(
                    icon = Icons.Filled.Search,
                    title = strings.newAnalysis,
                    description = strings.newAnalysisDescription,
                    accent = MaterialTheme.colorScheme.primary,
                    route = "camera"
                ),
                HomeMenuItem(
                    icon = Icons.Filled.DateRange,
                    title = strings.historyNav,
                    description = strings.historyDescription,
                    accent = MaterialTheme.colorScheme.secondary,
                    route = "history"
                ),
                HomeMenuItem(
                    icon = Icons.Filled.Info,
                    title = strings.diseaseInfoNav,
                    description = strings.diseaseInfoDescription,
                    accent = MaterialTheme.colorScheme.tertiary,
                    route = "disease_info"
                ),
                HomeMenuItem(
                    icon = Icons.Filled.AccountCircle,
                    title = strings.profileNav,
                    description = strings.profileDescription,
                    accent = MaterialTheme.colorScheme.primary,
                    route = "profile"
                ),
                HomeMenuItem(
                    icon = Icons.Filled.Settings,
                    title = strings.settingsNav,
                    description = strings.settingsDescription,
                    accent = MaterialTheme.colorScheme.secondary,
                    route = "settings"
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { item ->
                    HomeMenuCard(item = item, onClick = { navController.navigate(item.route) })
                }
            }
        }
    }
}

/**
 * [Principio S - SRP] Unica responsabilidad: dibujar el encabezado de bienvenida de Home, con un
 * dato real (cuantos analisis lleva guardados el usuario) en vez de solo texto decorativo, para
 * que la pantalla de entrada se sienta viva y no un simple menu vacio.
 */
@Composable
private fun HomeHeroCard(welcome: String, subtitle: String, analysesLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = welcome,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(
                    text = analysesLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/** [Principio S - SRP] Unica responsabilidad: dibujar una tarjeta de menu del Home. */
@Composable
private fun HomeMenuCard(item: HomeMenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, tint = item.accent)
            }
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
