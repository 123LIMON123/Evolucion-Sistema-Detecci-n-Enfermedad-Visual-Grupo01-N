package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppStrings

/**
 * [Principio S - SRP] Unica responsabilidad: pedir nombre y apellidos la primera vez que alguien
 * entra con Google (nunca pide correo ni contraseña: esos ya los da la cuenta de Google). Se
 * muestra una sola vez, justo despues de un primer login con Google (ver
 * [com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AuthViewModel.signInWithGoogle]).
 */
@Composable
fun CompleteGoogleProfileDialog(strings: AppStrings, onConfirm: (fullName: String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val canConfirm = firstName.isNotBlank() && lastName.isNotBlank()

    AlertDialog(
        onDismissRequest = { /* Obligatorio: no se puede cerrar sin completar el nombre. */ },
        title = { Text(strings.completeProfileTitle) },
        text = {
            Column {
                Text(strings.completeProfileIntro, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(strings.nameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(strings.lastNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm("${firstName.trim()} ${lastName.trim()}".trim()) },
                enabled = canConfirm
            ) {
                Text(strings.completeProfileConfirm)
            }
        }
    )
}
