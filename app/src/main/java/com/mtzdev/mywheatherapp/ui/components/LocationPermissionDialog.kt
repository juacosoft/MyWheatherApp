package com.mtzdev.mywheatherapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mtzdev.mywheatherapp.R

/**
 * Diálogo de solicitud de permisos de ubicación.
 *
 * Muestra un diálogo explicativo antes de solicitar permisos de ubicación,
 * siguiendo las mejores prácticas de UX de Android.
 *
 * @param onAllowClick Callback cuando el usuario acepta otorgar permiso
 * @param onDenyClick Callback cuando el usuario deniega el permiso
 * @param onDismiss Callback cuando el diálogo se cierra sin acción
 */
@Composable
fun LocationPermissionDialog(
    onAllowClick: () -> Unit,
    onDenyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon"
            )
        },
        title = {
            Text(text = "Acceso a ubicación")
        },
        text = {
            Text(
                text = "Necesitamos acceso a tu ubicación para mostrarte el clima " +
                        "de tu zona actual. Puedes usar la búsqueda manual si prefieres " +
                        "no otorgar este permiso."
            )
        },
        confirmButton = {
            TextButton(onClick = onAllowClick) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDenyClick) {
                Text("Usar búsqueda manual")
            }
        }
    )
}
