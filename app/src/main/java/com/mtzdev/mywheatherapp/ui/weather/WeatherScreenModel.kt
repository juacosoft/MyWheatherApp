package com.mtzdev.mywheatherapp.ui.weather

import android.util.Log
import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentWeatherByCoordinatesUseCase
import com.mtzdev.mywheatherapp.domain.usecase.SearchLocationUseCase
import kotlinx.coroutines.launch

/**
 * T071-T076: ScreenModel for Weather Screen.
 *
 * Handles all business logic and state management for weather display,
 * including GPS auto-detection and manual city search.
 */
class WeatherScreenModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getCurrentWeatherByCoordinatesUseCase: GetCurrentWeatherByCoordinatesUseCase,
    private val searchLocationUseCase: SearchLocationUseCase
) : MVIBaseScreenMode<
        WeatherContract.State,
        WeatherContract.Event,
        WeatherContract.Effect
        >(WeatherContract.State()) {

    private var lastFailedAction: (() -> Unit)? = null

    /**
     * T072: Handle all UI events.
     */
    override fun handleEvent(event: WeatherContract.Event) {
        when (event) {
            is WeatherContract.Event.RequestAutoDetection -> handleAutoDetection()
            is WeatherContract.Event.SearchCity -> handleSearchCity(event.cityName)
            is WeatherContract.Event.SwitchLocationMode -> handleSwitchLocationMode(event.mode)
            is WeatherContract.Event.OnPermissionResult -> handlePermissionResult(event.isGranted)
            is WeatherContract.Event.ShowPermissionSettings -> handleShowPermissionSettings()
            is WeatherContract.Event.RetryLastAction -> handleRetry()
            is WeatherContract.Event.ClearError -> handleClearError()
            is WeatherContract.Event.OnSearchQueryChanged -> handleSearchQueryChanged(event.query)
            is WeatherContract.Event.SelectLocation -> handleSelectLocation(event.location)
        }
    }

    /**
     * T073: Handle GPS auto-detection.
     *
     * Flow:
     * 1. Check permissions
     * 2. Get current location from GPS
     * 3. Fetch weather for coordinates
     */
    private fun handleAutoDetection() {
        // Check permission status first
        if (state.value.permissionStatus != WeatherContract.PermissionStatus.GRANTED) {
            sendEffect { WeatherContract.Effect.RequestLocationPermission }
            lastFailedAction = { handleAutoDetection() }
            return
        }

        screenModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true, error = null)

            // Step 1: Get current location
            getCurrentLocationUseCase().collect { locationResult ->
                when (locationResult) {
                    is Result.Loading -> {
                        // Already in loading state
                    }

                    is Result.Success -> {
                        val location = locationResult.data
                        // Step 2: Get weather for coordinates
                        getCurrentWeatherByCoordinatesUseCase(
                            latitude = location.latitude,
                            longitude = location.longitude
                        ).collect { weatherResult ->
                            when (weatherResult) {
                                is Result.Loading -> {
                                    // Still loading
                                }

                                is Result.Success -> {
                                    mutableState.value = state.value.copy(
                                        weather = weatherResult.data,
                                        location = location,
                                        isLoading = false,
                                        error = null
                                    )
                                    lastFailedAction = null
                                }

                                is Result.Error -> {
                                    handleWeatherError(weatherResult.error)
                                    lastFailedAction = { handleAutoDetection() }
                                }
                            }
                        }
                    }

                    is Result.Error -> {
                        handleLocationError(locationResult.error)
                        Log.d("handleLocationError", "Error:: ${locationResult.error}")
                        lastFailedAction = { handleAutoDetection() }
                    }
                }
            }
        }
    }

    /**
     * T074: Handle manual city search.
     *
     * Flow:
     * 1. Validate city name
     * 2. Search for location
     * 3. Fetch weather for coordinates
     */
    private fun handleSearchCity(cityName: String) {
        screenModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true, error = null)

            searchLocationUseCase(cityName).collect { searchResult ->
                when (searchResult) {
                    is Result.Loading -> {
                        // Already in loading state
                    }

                    is Result.Success -> {
                        val location = searchResult.data
                        // Single result - fetch weather directly
                        handleSelectLocation(location)
                    }

                    is Result.Error -> {
                        handleSearchError(searchResult.error)
                        lastFailedAction = { handleSearchCity(cityName) }
                    }
                }
            }
        }
    }

    /**
     * Handle location selection from search results.
     */
    private fun handleSelectLocation(location: com.mtzdev.mywheatherapp.domain.model.Location) {
        screenModelScope.launch {
            mutableState.value = state.value.copy(
                isLoading = true,
                error = null
            )

            getCurrentWeatherByCoordinatesUseCase(
                latitude = location.latitude,
                longitude = location.longitude
            ).collect { weatherResult ->
                when (weatherResult) {
                    is Result.Loading -> {
                        // Still loading
                    }

                    is Result.Success -> {
                        mutableState.value = state.value.copy(
                            weather = weatherResult.data,
                            location = location,
                            isLoading = false,
                            error = null
                        )
                        lastFailedAction = null
                    }

                    is Result.Error -> {
                        handleWeatherError(weatherResult.error)
                        lastFailedAction = { handleSelectLocation(location) }
                    }
                }
            }
        }
    }

    /**
     * Handle location mode switch.
     */
    private fun handleSwitchLocationMode(mode: WeatherContract.LocationMode) {
        mutableState.value = state.value.copy(
            locationMode = mode,
            error = null,
            searchQuery = ""
        )
    }

    /**
     * Handle permission result from system.
     */
    private fun handlePermissionResult(isGranted: Boolean) {
        val newStatus = if (isGranted) {
            WeatherContract.PermissionStatus.GRANTED
        } else {
            WeatherContract.PermissionStatus.DENIED
        }

        mutableState.value = state.value.copy(permissionStatus = newStatus)

        if (isGranted) {
            // Retry the action that required permission
            lastFailedAction?.invoke()
        }
    }

    /**
     * Handle navigation to permission settings.
     */
    private fun handleShowPermissionSettings() {
        mutableState.value = state.value.copy(
            permissionStatus = WeatherContract.PermissionStatus.PERMANENTLY_DENIED
        )
        sendEffect { WeatherContract.Effect.NavigateToSettings }
    }

    /**
     * T076: Handle retry of last failed action.
     */
    private fun handleRetry() {
        lastFailedAction?.invoke() ?: run {
            // No failed action - try based on current mode
            when (state.value.locationMode) {
                WeatherContract.LocationMode.AUTO -> handleAutoDetection()
                WeatherContract.LocationMode.MANUAL -> {
                    if (state.value.searchQuery.isNotBlank()) {
                        handleSearchCity(state.value.searchQuery)
                    }
                }
            }
        }
    }

    /**
     * Clear error message.
     */
    private fun handleClearError() {
        mutableState.value = state.value.copy(error = null)
    }

    /**
     * Update search query.
     */
    private fun handleSearchQueryChanged(query: String) {
        mutableState.value = state.value.copy(searchQuery = query)
    }

    /**
     * T075: Map location errors to user-friendly messages.
     */
    private fun handleLocationError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.LocationError.PermissionDenied ->
                "Se necesitan permisos de ubicación para detectar tu posición"

            is DomainError.LocationError.GpsDisabled ->
                "Por favor activa el GPS en tu dispositivo"

            is DomainError.LocationError.Unavailable ->
                "No se pudo obtener tu ubicación. Intenta de nuevo"

            is DomainError.LocationError.Timeout ->
                "La detección de ubicación tardó demasiado. Intenta de nuevo"

            else -> "Error al obtener ubicación"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }

    /**
     * T075: Map weather errors to user-friendly messages.
     */
    private fun handleWeatherError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.WeatherError.NoInternetConnection ->
                "No hay conexión a Internet"

            is DomainError.WeatherError.InvalidApiKey ->
                "Error de configuración. Contacta soporte"

            is DomainError.WeatherError.RateLimitExceeded ->
                "Demasiadas solicitudes. Intenta más tarde"

            is DomainError.WeatherError.Unknown ->
                error.message ?: "Error al obtener el clima"

            else -> "Error al obtener el clima"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }

    /**
     * T075: Map search errors to user-friendly messages.
     */
    private fun handleSearchError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.WeatherError.CityNotFound ->
                "No se encontró la ciudad"

            is DomainError.WeatherError.NoInternetConnection ->
                "No hay conexión a Internet"

            is DomainError.ValidationError.EmptyCityName ->
                "Por favor ingresa el nombre de una ciudad"

            else -> "Error al buscar la ciudad"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }
}
