package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.commons.WeatherUnknowGeoDataException
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import kotlinx.coroutines.launch

class WeatherDataScreenModel(
    private val getWeatherDataUseCase: GetWeatherDataUseCase
): MVIBaseScreenMode<WeatherDataContract.State, WeatherDataContract.Event, WeatherDataContract.Effect>(WeatherDataContract.State()){


    private fun syncWeatherData() = screenModelScope.launch  {
        mutableState.value.currentGeoData?.let { secureGeo ->
            val lat = secureGeo.lat
            val lon = secureGeo.lon
            val result = getWeatherDataUseCase.invoke(lat, lon)
            when(result){
                is WeatherDataResult.Data -> {
                    val cloudsPercent = result.data.clouds?.all ?: 0
                    mutableState.value = mutableState.value.copy(
                        weatherData = result.data,
                        loadingWeather = false,
                        colors = if (cloudsPercent < 50) WeatherColors.SUNNY else WeatherColors.CLOUDY
                    )
                }
                is WeatherDataResult.Error -> {
                    sendEffect {
                        WeatherDataContract.Effect.ShowError(result.error)
                    }
                }
            }
        }?:run {
            sendEffect { WeatherDataContract.Effect.ShowError(WeatherUnknowGeoDataException("No geo data")) }
        }
    }

    override fun handleEvent(event: WeatherDataContract.Event) {
        when(event){
            is WeatherDataContract.Event.Init -> {
                mutableState.value = mutableState.value.copy(currentGeoData = event.currentGeoData, loadingWeather = true)
                syncWeatherData()
            }
            WeatherDataContract.Event.RefreshData -> {
                syncWeatherData()
            }
        }
    }
}