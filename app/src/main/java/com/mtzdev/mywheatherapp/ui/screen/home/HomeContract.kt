package com.mtzdev.mywheatherapp.ui.screen.home

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

interface HomeContract: MVIContract {

    data class State(
        val loadingGeo: Boolean = false,
        val geoData: WeatherGeoDataEntity,
    ): MVIContract.UiState

    sealed interface Event: MVIContract.UiEvent {
        data class ChangeUbication(val city: String): Event
        object NavigateToNotification: Event
        object NavigateToSettings: Event
    }

    sealed interface Effect: MVIContract.Effect {
        data class ShowError(val errorType: WeatherException): Effect
        object NavigateToNotification: Effect
        object NavigateToSettings: Effect

    }
}