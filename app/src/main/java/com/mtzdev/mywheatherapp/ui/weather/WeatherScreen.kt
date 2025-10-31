package com.mtzdev.mywheatherapp.ui.weather

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.mtzdev.mywheatherapp.ui.weather.components.CitySearchBar
import com.mtzdev.mywheatherapp.ui.weather.components.ErrorMessage
import com.mtzdev.mywheatherapp.ui.weather.components.LoadingIndicator
import com.mtzdev.mywheatherapp.ui.weather.components.LocationModeToggle
import com.mtzdev.mywheatherapp.ui.weather.components.WeatherDisplay

/**
 * T077: Weather Screen as Voyager Tab.
 *
 * Main screen for weather display with:
 * - GPS auto-detection mode
 * - Manual city search mode
 * - Permission handling
 * - MVI state management
 */
class WeatherScreen(
    private val modifier: Modifier = Modifier
) : Tab {

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<WeatherScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        // Permission handling
        val locationPermissions = rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // Update permission status in state
        LaunchedEffect(locationPermissions.allPermissionsGranted) {
            val isGranted = locationPermissions.permissions.any { it.status.isGranted }
            screenModel.setEvent(WeatherContract.Event.OnPermissionResult(isGranted))
        }

        // Handle effects
        LaunchedEffect(Unit) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is WeatherContract.Effect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }

                    is WeatherContract.Effect.NavigateToSettings -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }

                    is WeatherContract.Effect.RequestLocationPermission -> {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                }
            }
        }

        // Auto-request location on first load (AUTO mode)
        LaunchedEffect(Unit) {
            if (state.locationMode == WeatherContract.LocationMode.AUTO) {
                screenModel.setEvent(WeatherContract.Event.RequestAutoDetection)
            }
        }

        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            WeatherContent(
                state = state,
                paddingValues = paddingValues,
                onEvent = screenModel::setEvent
            )
        }
    }

    @Composable
    private fun WeatherContent(
        state: WeatherContract.State,
        paddingValues: PaddingValues,
        onEvent: (WeatherContract.Event) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // T083: Location mode toggle
            LocationModeToggle(
                selectedMode = state.locationMode,
                onModeChange = { mode ->
                    onEvent(WeatherContract.Event.SwitchLocationMode(mode))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // T086: Search bar with animated visibility (visible in MANUAL mode)
            AnimatedVisibility(
                visible = state.locationMode == WeatherContract.LocationMode.MANUAL,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column {
                    CitySearchBar(
                        query = state.searchQuery,
                        onQueryChange = { query ->
                            onEvent(WeatherContract.Event.OnSearchQueryChanged(query))
                        },
                        onSearch = {
                            if (state.searchQuery.isNotBlank()) {
                                onEvent(WeatherContract.Event.SearchCity(state.searchQuery))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // T086: Crossfade for state transitions (loading, error, success)
            Crossfade(
                targetState = when {
                    state.isLoading -> "loading"
                    state.error != null -> "error"
                    state.weather != null -> "success"
                    else -> "empty"
                },
                label = "weather_state_transition"
            ) { targetState ->
                when (targetState) {
                    "loading" -> {
                        LoadingIndicator(
                            message = when (state.locationMode) {
                                WeatherContract.LocationMode.AUTO -> "Detectando ubicación..."
                                WeatherContract.LocationMode.MANUAL -> "Buscando ciudad..."
                            }
                        )
                    }

                    "error" -> {
                        state.error?.let { errorMessage ->
                            ErrorMessage(
                                message = errorMessage,
                                onRetry = {
                                    onEvent(WeatherContract.Event.RetryLastAction)
                                }
                            )
                        }
                    }

                    "success" -> {
                        state.weather?.let { weather ->
                            WeatherDisplay(weather = weather)
                        }
                    }

                    "empty" -> {
                        // Empty state - show nothing or placeholder
                    }
                }
            }
        }
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Clima"
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
}
