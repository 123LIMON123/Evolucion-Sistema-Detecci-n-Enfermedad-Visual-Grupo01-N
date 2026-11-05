package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.PasswordChangeFailureReason
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.PasswordChangeResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.ThemeRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserSessionRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiagnosisResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.RiskLevel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar

/**
 * [Principio S - SRP] Unica responsabilidad: mostrar y editar los datos del usuario en sesion,
 * sus preferencias (tema) y un resumen de su actividad. No sabe nada de camara ni de como se
 * calcula un diagnostico; eso sigue viviendo en otras pantallas y repositorios.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    userSession: UserSessionRepository,
    localizationRepository: LocalizationRepository,
    authRepository: AuthRepository,
    themeRepository: ThemeRepository,
    historyRepository: AnalysisHistoryRepository
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val user by userSession.currentUser.collectAsState()
    val isDarkMode by themeRepository.isDarkMode.collectAsState()
    val history by historyRepository.history.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(user, isEditing) { mutableStateOf(user.name) }
    var editedEmail by remember(user, isEditing) { mutableStateOf(user.email) }
    var showChangePassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.profileTitle,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text(strings.nameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text(strings.emailLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) { Text(strings.profileCancelAction) }
                    Button(
                        onClick = {
                            userSession.updateProfile(editedName.trim(), editedEmail.trim())
                            isEditing = false
                        },
                        enabled = editedName.isNotBlank() && editedEmail.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text(strings.profileSaveAction) }
                }
            } else {
                Text(text = user.name, style = MaterialTheme.typography.headlineMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { isEditing = true }) {
                    Text(strings.profileEditAction)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            ProfileStatsCard(strings = strings, history = history)

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.profileDarkModeLabel, style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isDarkMode, onCheckedChange = { themeRepository.toggle() })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showChangePassword = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.profileChangePasswordAction)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    userSession.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.padding(start = 4.dp))
                Text(strings.logout)
            }
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            strings = strings,
            userEmail = user.email,
            authRepository = authRepository,
            onDismiss = { showChangePassword = false }
        )
    }
}

/** [Principio S - SRP] Unica responsabilidad: resumir cuantos analisis hizo el usuario y como se repartieron por nivel de riesgo. */
@Composable
private fun ProfileStatsCard(strings: AppStrings, history: List<DiagnosisResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.profileStatsSection.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text(
                    text = strings.profileStatsNone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                StatRow(strings.profileStatsTotalAnalyses, history.size.toString())
                StatRow(strings.profileStatsHighSuspicion, history.count { it.riskLevel == RiskLevel.SOSPECHA_ALTA }.toString())
                StatRow(strings.profileStatsModerate, history.count { it.riskLevel == RiskLevel.RIESGO_MODERADO }.toString())
                StatRow(strings.profileStatsLow, history.count { it.riskLevel == RiskLevel.BAJO_RIESGO }.toString())
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/** [Principio S - SRP] Unica responsabilidad: pedir contrasena actual + nueva y validar antes de llamar a AuthRepository. */
@Composable
private fun ChangePasswordDialog(
    strings: AppStrings,
    userEmail: String,
    authRepository: AuthRepository,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.profileChangePasswordTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; errorMessage = null },
                    label = { Text(strings.currentPasswordLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    label = { Text(strings.newPasswordLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it; errorMessage = null },
                    label = { Text(strings.confirmNewPasswordLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPassword != confirmNewPassword) {
                    errorMessage = strings.errorNewPasswordMismatch
                    return@TextButton
                }
                when (val result = authRepository.changePassword(userEmail, currentPassword, newPassword)) {
                    is PasswordChangeResult.Success -> onDismiss()
                    is PasswordChangeResult.Failure -> {
                        errorMessage = when (result.reason) {
                            PasswordChangeFailureReason.WRONG_CURRENT_PASSWORD -> strings.errorWrongCurrentPassword
                            PasswordChangeFailureReason.NEW_PASSWORD_TOO_SHORT -> strings.errorNewPasswordTooShort
                        }
                    }
                }
            }) {
                Text(strings.profileChangePasswordConfirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.profileCancelAction) }
        }
    )
}
