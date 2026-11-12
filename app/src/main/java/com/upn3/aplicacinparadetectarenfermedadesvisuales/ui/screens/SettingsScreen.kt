package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.ReminderRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.reminders.ReminderScheduler
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar

/**
 * [Principio S - SRP] Unica responsabilidad: reunir en un solo lugar las preferencias de la app
 * (idioma, borrar historial, informacion de version). No sabe nada de autenticacion ni del perfil
 * del usuario: eso sigue siendo responsabilidad de ProfileScreen.
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    localizationRepository: LocalizationRepository,
    historyRepository: AnalysisHistoryRepository,
    reminderRepository: ReminderRepository
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    val remindersEnabled by reminderRepository.isEnabled.collectAsState()
    var showPermissionDeniedNotice by remember { mutableStateOf(false) }

    fun enableReminders() {
        reminderRepository.setEnabled(true)
        ReminderScheduler.schedule(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableReminders()
        } else {
            showPermissionDeniedNotice = true
        }
    }

    fun onToggleReminders(enabled: Boolean) {
        if (!enabled) {
            reminderRepository.setEnabled(false)
            ReminderScheduler.cancel(context)
            return
        }
        val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        if (needsRuntimePermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            enableReminders()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.settingsTitle,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = strings.settingsLanguageSection) {
                SettingsRow(
                    label = strings.settingsLanguageCurrentValue,
                    action = {
                        TextButton(onClick = { localizationRepository.toggle() }) {
                            Text(if (language == AppLanguage.ES) "ES → EN" else "EN → ES")
                        }
                    }
                )
            }

            SettingsSection(title = strings.settingsRemindersSection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.settingsRemindersToggleLabel, style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = remindersEnabled, onCheckedChange = { onToggleReminders(it) })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.settingsRemindersDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showPermissionDeniedNotice) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.settingsRemindersPermissionDenied,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            SettingsSection(title = strings.settingsHistorySection) {
                OutlinedButton(
                    onClick = { showClearHistoryConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings.settingsClearHistoryAction)
                }
            }

            SettingsSection(title = strings.settingsAboutSection) {
                Text(text = strings.settingsAboutVersion, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.settingsAboutDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text(strings.settingsClearHistoryConfirmTitle) },
            text = { Text(strings.settingsClearHistoryConfirmMessage) },
            confirmButton = {
                TextButton(onClick = {
                    historyRepository.clear()
                    showClearHistoryConfirm = false
                }) {
                    Text(strings.settingsClearHistoryConfirmButton, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text(strings.settingsClearHistoryCancelButton)
                }
            }
        )
    }
}

/** [Principio S - SRP] Unica responsabilidad: dibujar una seccion con titulo dentro de Ajustes. */
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        action()
    }
}
