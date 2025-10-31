package com.mtzdev.mywheatherapp.ui.screen.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mtzdev.mywheatherapp.ui.components.GpsLocationButton
import com.mtzdev.mywheatherapp.ui.components.LocationPermissionDialog
import com.mtzdev.mywheatherapp.ui.components.LocationSearchInput
import com.mtzdev.mywheatherapp.ui.components.TopBarComponent
import com.mtzdev.mywheatherapp.ui.screen.hourly.WeatherDataHourlyScreen
import com.mtzdev.mywheatherapp.ui.screen.notification.NotificationScreen
import com.mtzdev.mywheatherapp.ui.screen.settings.SettingsScreen
import com.mtzdev.mywheatherapp.ui.screen.weatherdata.WeatherDataScreen

class HomeScreen: Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        var paddingState by remember { mutableStateOf<PaddingValues>(PaddingValues(4.dp)) }

        // Permission launcher para solicitar permisos de ubicación
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                screenModel.setEvent(HomeContract.Event.OnPermissionGranted)
            } else {
                screenModel.setEvent(HomeContract.Event.OnPermissionDenied)
            }
        }

        // Effect handlers
        HandleEffects(
            screenModel = screenModel,
            snackbarHostState = snackbarHostState,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onOpenAppSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )

        // Permission dialog
        if (state.showPermissionDialog) {
            LocationPermissionDialog(
                onAllowClick = {
                    screenModel.setEvent(HomeContract.Event.OnPermissionDialogAllow)
                },
                onDenyClick = {
                    screenModel.setEvent(HomeContract.Event.OnPermissionDialogDeny)
                },
                onDismiss = {
                    screenModel.setEvent(HomeContract.Event.OnPermissionDialogDismiss)
                }
            )
        }

        // Main content
        if (state.geoData != null) {
            // Ya tenemos ubicación seleccionada, mostrar tabs de clima
            TabNavigator(WeatherDataScreen(state.geoData!!, Modifier.padding(paddingState))) {
                Scaffold(
                    topBar = {
                        val name = state.geoData?.localNames?.es ?: state.geoData?.name
                        TopBarComponent(
                            title = name.orEmpty(),
                            loading = state.loadingGeo,
                            onLeftIconClick = {
                                screenModel.setEvent(HomeContract.Event.NavigateToNotification)
                            },
                            onRightIconClick = {
                                screenModel.setEvent(HomeContract.Event.NavigateToSettings)
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            TabNavigationItem(WeatherDataScreen(state.geoData!!, Modifier.padding(paddingState)))
                            TabNavigationItem(WeatherDataHourlyScreen(paddingState))
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { paddingValues ->
                    paddingState = paddingValues
                    CurrentTab()
                }
            }
        } else {
            // No hay ubicación seleccionada, mostrar pantalla de selección
            LocationSelectionScreen(
                state = state,
                screenModel = screenModel,
                snackbarHostState = snackbarHostState
            )
        }
    }

    @Composable
    private fun LocationSelectionScreen(
        state: HomeContract.State,
        screenModel: HomeScreenModel,
        snackbarHostState: SnackbarHostState
    ) {
        Scaffold(
            topBar = {
                TopBarComponent(
                    title = "Selecciona ubicación",
                    loading = state.loadingGeo || state.isLoadingGpsLocation || state.isLoadingCitySearch,
                    onLeftIconClick = {
                        screenModel.setEvent(HomeContract.Event.NavigateToNotification)
                    },
                    onRightIconClick = {
                        screenModel.setEvent(HomeContract.Event.NavigateToSettings)
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Título
                    Text(
                        text = "Encuentra el clima de tu ubicación",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón GPS
                    GpsLocationButton(
                        onClick = {
                            screenModel.setEvent(HomeContract.Event.OnRequestGpsLocation)
                        },
                        isLoading = state.isLoadingGpsLocation,
                        enabled = !state.isLoadingCitySearch,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Texto divisor
                    Text(
                        text = "o",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Búsqueda manual
                    LocationSearchInput(
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = { query ->
                            screenModel.setEvent(HomeContract.Event.OnSearchQueryChange(query))
                        },
                        onSearchSubmit = {
                            screenModel.setEvent(HomeContract.Event.OnSearchCity(state.searchQuery))
                        },
                        isLoading = state.isLoadingCitySearch,
                        enabled = !state.isLoadingGpsLocation,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mensaje de error si existe
                    if (state.locationError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.locationError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current
        val defaultIcon = rememberVectorPainter(Icons.Default.Warning)
        NavigationBarItem(
            selected = tabNavigator.current == tab,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon ?: defaultIcon, contentDescription = tab.options.title) }
        )
    }

    @Composable
    private fun HandleEffects(
        screenModel: HomeScreenModel,
        snackbarHostState: SnackbarHostState,
        onRequestPermission: () -> Unit,
        onOpenAppSettings: () -> Unit
    ) {
        val navigator = LocalNavigator.current
        val effect = screenModel.effect.collectAsState(null)

        LaunchedEffect(effect.value) {
            when (val currentEffect = effect.value) {
                // Existing effects
                is HomeContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = currentEffect.errorType.toString()
                    )
                }
                HomeContract.Effect.NavigateToNotification -> {
                    navigator?.push(NotificationScreen())
                }
                HomeContract.Effect.NavigateToSettings -> {
                    navigator?.push(SettingsScreen())
                }

                // New location effects
                HomeContract.Effect.RequestLocationPermission -> {
                    onRequestPermission()
                }
                HomeContract.Effect.OpenAppSettings -> {
                    onOpenAppSettings()
                }
                is HomeContract.Effect.ShowLocationError -> {
                    snackbarHostState.showSnackbar(
                        message = currentEffect.message
                    )
                }

                null -> Unit
            }
        }
    }
}
