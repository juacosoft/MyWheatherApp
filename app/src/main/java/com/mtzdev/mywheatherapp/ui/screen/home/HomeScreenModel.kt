package com.mtzdev.mywheatherapp.ui.screen.home

import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.commons.PermissionManager
import com.mtzdev.mywheatherapp.domain.LocationException
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.LocationEntity
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val permissionManager: PermissionManager
): MVIBaseScreenMode<HomeContract.State, HomeContract.Event, HomeContract.Effect>(
    HomeContract.State()
) {

    private fun updateState(newState: HomeContract.State) {
        mutableState.value = newState
    }

    override fun handleEvent(event: HomeContract.Event) {
        when(event) {
            // Existing events
            is HomeContract.Event.ChangeUbication -> loadGeoData(event.city)
            HomeContract.Event.NavigateToNotification -> sendEffect { HomeContract.Effect.NavigateToNotification }
            HomeContract.Event.NavigateToSettings -> sendEffect { HomeContract.Effect.NavigateToSettings }

            // GPS location events
            HomeContract.Event.OnRequestGpsLocation -> handleGpsLocationRequest()
            HomeContract.Event.OnPermissionGranted -> handlePermissionGranted()
            HomeContract.Event.OnPermissionDenied -> handlePermissionDenied()

            // City search events
            is HomeContract.Event.OnSearchCity -> handleCitySearch(event.cityName)
            is HomeContract.Event.OnSearchQueryChange -> handleSearchQueryChange(event.query)

            // Permission dialog events
            HomeContract.Event.OnPermissionDialogAllow -> handlePermissionDialogAllow()
            HomeContract.Event.OnPermissionDialogDeny -> handlePermissionDialogDeny()
            HomeContract.Event.OnPermissionDialogDismiss -> handlePermissionDialogDismiss()

            // Error handling
            HomeContract.Event.OnClearLocationError -> handleClearLocationError()
        }
    }

    /**
     * Maneja la solicitud de ubicación GPS.
     * Verifica permisos y decide si mostrar rationale o solicitar permiso directamente.
     */
    private fun handleGpsLocationRequest() {
        if (permissionManager.hasLocationPermission()) {
            // Ya tiene permiso, obtener ubicación directamente
            requestGpsLocation()
        } else {
            // No tiene permiso, mostrar diálogo de rationale
            updateState(mutableState.value.copy(showPermissionDialog = true))
        }
    }

    /**
     * Usuario aceptó otorgar permiso en el diálogo de rationale.
     * Cerrar diálogo y enviar effect para solicitar permiso del sistema.
     */
    private fun handlePermissionDialogAllow() {
        updateState(mutableState.value.copy(showPermissionDialog = false))
        sendEffect { HomeContract.Effect.RequestLocationPermission }
    }

    /**
     * Usuario denegó permiso en el diálogo de rationale.
     * Cerrar diálogo y mantener disponible la búsqueda manual.
     */
    private fun handlePermissionDialogDeny() {
        updateState(mutableState.value.copy(showPermissionDialog = false))
    }

    /**
     * Usuario cerró el diálogo de rationale sin acción.
     */
    private fun handlePermissionDialogDismiss() {
        updateState(mutableState.value.copy(showPermissionDialog = false))
    }

    /**
     * Sistema otorgó el permiso de ubicación.
     * Proceder a obtener ubicación GPS.
     */
    private fun handlePermissionGranted() {
        requestGpsLocation()
    }

    /**
     * Sistema denegó el permiso de ubicación.
     * Mostrar mensaje explicativo y ofrecer búsqueda manual.
     */
    private fun handlePermissionDenied() {
        updateState(
            mutableState.value.copy(
                isLoadingGpsLocation = false,
                locationError = "Permiso de ubicación denegado. Usa la búsqueda manual para encontrar tu ciudad.",
                showPermissionDeniedMessage = true
            )
        )
    }

    /**
     * Solicita ubicación GPS y maneja el resultado.
     */
    private fun requestGpsLocation() = screenModelScope.launch {
        updateState(
            mutableState.value.copy(
                isLoadingGpsLocation = true,
                locationError = null
            )
        )

        when (val result = getCurrentLocationUseCase()) {
            is LocationResult.Success -> {
                handleGpsLocationSuccess(result.location)
            }
            is LocationResult.Error -> {
                handleGpsLocationError(result.exception)
            }
        }
    }

    /**
     * Maneja el éxito de obtención de ubicación GPS.
     * Usa las coordenadas para obtener datos de geocoding.
     */
    private fun handleGpsLocationSuccess(location: LocationEntity) = screenModelScope.launch {
        // Por ahora, simplemente guardamos las coordenadas
        // En una implementación completa, buscaríamos el nombre de la ciudad por coordenadas
        // o directamente pasaríamos las coordenadas al weather data
        updateState(
            mutableState.value.copy(
                isLoadingGpsLocation = false,
                locationError = null
            )
        )

        // TODO: En sección 7.3 navegaremos a WeatherDataScreen con estas coordenadas
        // Por ahora solo mostramos un mensaje de éxito temporal
        sendEffect {
            HomeContract.Effect.ShowLocationError(
                "Ubicación obtenida: ${location.latitude}, ${location.longitude}"
            )
        }
    }

    /**
     * Maneja errores al obtener ubicación GPS.
     * Mapea excepciones de dominio a mensajes user-friendly.
     */
    private fun handleGpsLocationError(exception: LocationException) {
        val errorMessage = when (exception) {
            LocationException.PermissionDenied ->
                "Permiso de ubicación denegado. Usa la búsqueda manual."

            LocationException.GpsDisabled ->
                "GPS desactivado. Actívalo en configuración o usa la búsqueda manual."

            LocationException.Timeout ->
                "Tiempo de espera agotado. Intenta de nuevo o usa la búsqueda manual."

            LocationException.PlayServicesUnavailable ->
                "Google Play Services no disponible. Usa la búsqueda manual."

            is LocationException.Unknown ->
                "Error al obtener ubicación: ${exception.message}. Usa la búsqueda manual."
        }

        updateState(
            mutableState.value.copy(
                isLoadingGpsLocation = false,
                locationError = errorMessage
            )
        )

        sendEffect { HomeContract.Effect.ShowLocationError(errorMessage) }

        // Si es permiso denegado permanentemente, ofrecer ir a settings
        if (exception is LocationException.PermissionDenied) {
            // En la UI se mostrará un botón para ir a settings si es necesario
            updateState(mutableState.value.copy(showPermissionDeniedMessage = true))
        }
    }

    /**
     * Maneja la búsqueda de ciudad por nombre.
     */
    private fun handleCitySearch(cityName: String) {
        if (cityName.isBlank()) {
            sendEffect {
                HomeContract.Effect.ShowLocationError("Por favor ingresa un nombre de ciudad")
            }
            return
        }

        loadGeoData(cityName)
    }

    /**
     * Actualiza el query de búsqueda en el estado.
     */
    private fun handleSearchQueryChange(query: String) {
        updateState(mutableState.value.copy(searchQuery = query))
    }

    /**
     * Limpia el mensaje de error de ubicación.
     */
    private fun handleClearLocationError() {
        updateState(
            mutableState.value.copy(
                locationError = null,
                showPermissionDeniedMessage = false
            )
        )
    }

    /**
     * Carga datos de geocoding por nombre de ciudad.
     * Función original mantenida y extendida con estados de loading.
     */
    private fun loadGeoData(city: String) = screenModelScope.launch {
        updateState(
            mutableState.value.copy(
                loadingGeo = true,
                isLoadingCitySearch = true,
                locationError = null
            )
        )

        val result = getWeatherGeoDataUseCase.invoke(city)

        when(result) {
            is WeatherGeoResult.Error -> {
                updateState(
                    mutableState.value.copy(
                        loadingGeo = false,
                        isLoadingCitySearch = false,
                        locationError = "Ciudad no encontrada. Intenta con otro nombre."
                    )
                )
                sendEffect {
                    HomeContract.Effect.ShowLocationError("Ciudad no encontrada: $city")
                }
            }
            is WeatherGeoResult.Data -> {
                updateState(
                    mutableState.value.copy(
                        geoData = result.data,
                        loadingGeo = false,
                        isLoadingCitySearch = false,
                        locationError = null,
                        searchQuery = "" // Limpiar búsqueda después de éxito
                    )
                )
            }
        }
    }
}
