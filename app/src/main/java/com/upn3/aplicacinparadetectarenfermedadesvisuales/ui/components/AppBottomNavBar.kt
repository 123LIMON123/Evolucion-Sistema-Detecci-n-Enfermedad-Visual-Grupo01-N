package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings

private data class BottomNavDestination(val route: String, val icon: ImageVector, val label: String)

/**
 * [Principio S - SRP] Unica responsabilidad: dibujar la navegacion inferior persistente de las 4
 * secciones principales (Inicio, Historial, Enfermedades, Perfil) y resaltar la activa segun la
 * ruta actual del [NavController]. No decide que hay dentro de cada pantalla.
 *
 * Ajustes se deja fuera a proposito (no entra bien como 5ta pestaña junto a estas 4 sin apretar
 * el diseño): se llega a ella desde un acceso dentro de Inicio/Perfil, igual que antes.
 */
@Composable
fun AppBottomNavBar(navController: NavController) {
    val strings = LocalAppStrings.current
    val destinations = listOf(
        BottomNavDestination("home", Icons.Filled.Home, strings.homeNavLabel),
        BottomNavDestination("history", Icons.Filled.DateRange, strings.historyNav),
        BottomNavDestination("disease_info", Icons.Filled.Info, strings.diseaseInfoNav),
        BottomNavDestination("profile", Icons.Filled.AccountCircle, strings.profileNav)
    )

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(destination.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
