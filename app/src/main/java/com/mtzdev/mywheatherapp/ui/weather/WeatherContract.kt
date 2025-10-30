package com.mtzdev.mywheatherapp.ui.weather

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Weather

/**
 * T065-T070: MVI Contract for Weather Screen.
 *
 * Defines State, Event, and Effect for weather display functionality
 * following Clean Architecture and MVI pattern.
 */
object WeatherContract {

    /**
     * T066: UI State for weather screen.
     *
     * Represents all possible states of the weather display.
     */
    data class State(
        val weather: Weather? = null,
        val location: Location? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val locationMode: LocationMode = LocationMode.AUTO,
        val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN,
        val searchQuery: String = ""
    ) : MVIContract.UiState

    /**
     * T067: UI Events that user can trigger.
     */
    sealed interface Event : MVIContract.UiEvent {
        /** Request GPS auto-detection of location */
        data object RequestAutoDetection : Event

        /** Search for city by name */
        data class SearchCity(val cityName: String) : Event

        /** Switch between AUTO and MANUAL location modes */
        data class SwitchLocationMode(val mode: LocationMode) : Event

        /** Handle permission result from system */
        data class OnPermissionResult(val isGranted: Boolean) : Event

        /** Navigate to app settings for permission */
        data object ShowPermissionSettings : Event

        /** Retry the last failed action */
        data object RetryLastAction : Event

        /** Clear current error message */
        data object ClearError : Event

        /** Update search query text */
        data class OnSearchQueryChanged(val query: String) : Event

        /** Select location from search results */
        data class SelectLocation(val location: Location) : Event
    }

    /**
     * T068: Side Effects (one-time events).
     */
    sealed interface Effect : MVIContract.Effect {
        /** Navigate to system settings */
        data object NavigateToSettings : Effect

        /** Show snackbar with message */
        data class ShowSnackbar(val message: String) : Effect

        /** Request location permission */
        data object RequestLocationPermission : Effect
    }

    /**
     * T069: Location mode enum.
     */
    enum class LocationMode {
        /** Automatic GPS detection */
        AUTO,

        /** Manual city search */
        MANUAL
    }

    /**
     * T070: Permission status enum.
     */
    enum class PermissionStatus {
        /** Permission status not yet determined */
        UNKNOWN,

        /** Permission granted */
        GRANTED,

        /** Permission denied (can still ask) */
        DENIED,

        /** Permission permanently denied (must go to settings) */
        PERMANENTLY_DENIED
    }
}
