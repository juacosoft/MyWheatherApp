package com.mtzdev.mywheatherapp.ui.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase
): StateScreenModel<HomeScreenModel.HomeScreenState>(HomeScreenState.Idle) {


    companion object {
        private val defaultData = WeatherGeoDataEntity(
            name = "Bogota",
            localNames = null,
            lat = 4.6534649,
            lon = -74.0836453
        )
    }

    init {
        //mutableState.value = HomeScreenState.Success(defaultData)
        loadGeoData("Bogota")
    }

    fun loadWeatherData(lat: Double, lon: Double) = screenModelScope.launch {
        val currentState = mutableState.value
        if (currentState is HomeScreenState.Success) {
            mutableState.value = HomeScreenState.Loading
            val result = getWeatherDataUseCase.invoke(lat, lon)
            when(result){
                is WeatherDataResult.Data -> mutableState.value = HomeScreenState.Success(currentState.geoData,result.data)
                is WeatherDataResult.Error -> mutableState.value = HomeScreenState.Error(result.error)
            }
        }
    }

    fun loadGeoData(city: String) = screenModelScope.launch {
        mutableState.value = HomeScreenState.Loading
        val result = getWeatherGeoDataUseCase.invoke(city)
        when(result){
            is WeatherGeoResult.Error -> mutableState.value = HomeScreenState.Error(result.error)
            is WeatherGeoResult.Data -> {
                mutableState.value = HomeScreenState.Success(result.data)
                if(result.data.lat != null && result.data.lon != null) {
                    loadWeatherData(result.data.lat, result.data.lon)
                }
            }
        }
    }

    sealed interface HomeScreenState {
        object Idle: HomeScreenState
        object Loading: HomeScreenState
        data class Success(
            val geoData: WeatherGeoDataEntity,
            val weatherData: WeatherDataEntity? = null
        ): HomeScreenState
        data class Error(val error: Exception): HomeScreenState
    }
}