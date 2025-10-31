package com.mtzdev.mywheatherapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón para solicitar ubicación GPS del dispositivo.
 *
 * Muestra icono de GPS y texto "Usar mi ubicación", con loading state.
 *
 * @param onClick Callback cuando se presiona el botón
 * @param isLoading Si está obteniendo ubicación GPS (muestra spinner)
 * @param enabled Si el botón está habilitado
 * @param modifier Modificador de Compose
 */
@Composable
fun GpsLocationButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Obteniendo ubicación...")
        } else {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "GPS icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Usar mi ubicación")
        }
    }
}
