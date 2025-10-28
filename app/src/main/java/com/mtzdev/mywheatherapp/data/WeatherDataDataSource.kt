package com.mtzdev.mywheatherapp.data

import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataResponse

interface WeatherDataDataSource {

    suspend fun getWeatherData(params: WeatherDataParams): WeatherDataResponse
}