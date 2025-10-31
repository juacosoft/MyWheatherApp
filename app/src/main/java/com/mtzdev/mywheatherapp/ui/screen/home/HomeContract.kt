package com.mtzdev.mywheatherapp.ui.screen.home

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

interface HomeContract: MVIContract {

    data class State(
        val loadingGeo: Boolean = false,
        val geoData: WeatherGeoDataEntity? = null,
        // Location states
        val isLoadingGpsLocation: Boolean = false,
        val isLoadingCitySearch: Boolean = false,
        val searchQuery: String = "",
        val locationError: String? = null,
        val showPermissionDialog: Boolean = false,
        val showPermissionDeniedMessage: Boolean = false,
    ): MVIContract.UiState

    sealed interface Event: MVIContract.UiEvent {
        // Existing events
        data class ChangeUbication(val city: String): Event
        data object NavigateToNotification: Event
        data object NavigateToSettings: Event

        // Location events
        data object OnRequestGpsLocation: Event
        data class OnSearchCity(val cityName: String): Event
        data class OnSearchQueryChange(val query: String): Event
        data object OnPermissionDialogAllow: Event
        data object OnPermissionDialogDeny: Event
        data object OnPermissionDialogDismiss: Event
        data object OnPermissionGranted: Event
        data object OnPermissionDenied: Event
        data object OnClearLocationError: Event
    }

    sealed interface Effect: MVIContract.Effect {
        // Existing effects
        data class ShowError(val errorType: WeatherException): Effect
        data object NavigateToNotification: Effect
        data object NavigateToSettings: Effect

        // Location effects
        data object RequestLocationPermission: Effect
        data object OpenAppSettings: Effect
        data class ShowLocationError(val message: String): Effect
    }
}