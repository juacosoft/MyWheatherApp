package com.mtzdev.mywheatherapp.ui.weather.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtzdev.mywheatherapp.ui.weather.WeatherContract

/**
 * T081: Composable for toggling between location modes.
 *
 * Shows FilterChips for AUTO (GPS) and MANUAL (search) modes.
 * Follows Material 3 design with clear selection state.
 */
@Composable
fun LocationModeToggle(
    selectedMode: WeatherContract.LocationMode,
    onModeChange: (WeatherContract.LocationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AUTO mode chip
            FilterChip(
                selected = selectedMode == WeatherContract.LocationMode.AUTO,
                onClick = {
                    onModeChange(WeatherContract.LocationMode.AUTO)
                },
                label = {
                    Text("GPS Automático")
                }
            )

            // MANUAL mode chip
            FilterChip(
                selected = selectedMode == WeatherContract.LocationMode.MANUAL,
                onClick = {
                    onModeChange(WeatherContract.LocationMode.MANUAL)
                },
                label = {
                    Text("Búsqueda Manual")
                }
            )
        }
    }
}
