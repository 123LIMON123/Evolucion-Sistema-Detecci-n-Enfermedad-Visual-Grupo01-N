package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Aviso fijo de que la clasificacion es una herramienta de apoyo (triage), no un diagnostico
 * medico definitivo. Se muestra en los puntos donde el usuario ve un resultado de IA.
 *
 * El Row exterior usa height(IntrinsicSize.Min): sin eso, el Spacer del acento izquierdo con
 * fillMaxHeight() se estira hasta el alto disponible del padre (p. ej. toda la pantalla si el
 * banner esta dentro de un Box.fillMaxSize), en vez de ajustarse al alto real del contenido.
 */
@Composable
fun MedicalDisclaimerBanner(
    text: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(text = dismissLabel, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
