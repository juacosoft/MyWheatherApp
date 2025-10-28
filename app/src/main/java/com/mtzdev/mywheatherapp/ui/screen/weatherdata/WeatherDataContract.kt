package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

interface WeatherDataContract {

    data class State(
        val loadingWeather: Boolean = true,
        val currentGeoData: WeatherGeoDataEntity? = null,
        val weatherData: WeatherDataEntity? = null,
        val colors: WeatherColors = WeatherColors.NO_DATA
    ): MVIContract.UiState

    sealed interface Event: MVIContract.UiEvent{
        data class Init(val currentGeoData: WeatherGeoDataEntity): Event
        data object RefreshData: Event
    }

    sealed interface Effect: MVIContract.Effect {
        data class ShowError(val errorType: WeatherException): Effect
    }
}