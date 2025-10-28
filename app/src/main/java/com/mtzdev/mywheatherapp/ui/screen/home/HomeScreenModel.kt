package com.mtzdev.mywheatherapp.ui.screen.home

import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase
): MVIBaseScreenMode<HomeContract.State, HomeContract.Event, HomeContract.Effect>(HomeContract.State(geoData = defaultData)){

    companion object {
        private val defaultData = WeatherGeoDataEntity(
            name = "Bogota",
            localNames = null,
            lat = 4.6534649,
            lon = -74.0836453
        )
    }

    private fun updateState(newState: HomeContract.State) {
        mutableState.value = newState
    }

    private fun loadGeoData(city: String) = screenModelScope.launch {
        updateState(mutableState.value.copy(loadingGeo = true))
        val result = getWeatherGeoDataUseCase.invoke(city)
        when(result){
            is WeatherGeoResult.Error -> updateState(mutableState.value.copy(loadingGeo = false))
            is WeatherGeoResult.Data -> {
                updateState(mutableState.value.copy(geoData = result.data, loadingGeo = false))
            }
        }
    }

    override fun handleEvent(event: HomeContract.Event) {
        when(event){
            is HomeContract.Event.ChangeUbication -> loadGeoData(event.city)
            HomeContract.Event.NavigateToNotification -> sendEffect { HomeContract.Effect.NavigateToNotification }
            HomeContract.Event.NavigateToSettings -> sendEffect { HomeContract.Effect.NavigateToSettings }
        }
    }
}