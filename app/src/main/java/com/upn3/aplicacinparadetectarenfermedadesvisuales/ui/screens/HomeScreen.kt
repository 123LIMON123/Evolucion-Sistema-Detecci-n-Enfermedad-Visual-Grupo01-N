package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, localizationRepository: LocalizationRepository) {
    val strings = LocalAppStrings.current

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
                .padding(20.dp)
        ) {
            Text(text = strings.homeWelcome, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.homeSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

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

@Composable
private fun HomeMenuCard(item: HomeMenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
