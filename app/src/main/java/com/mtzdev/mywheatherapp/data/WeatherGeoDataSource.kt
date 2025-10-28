package com.mtzdev.mywheatherapp.data

import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoResponse

interface WeatherGeoDataSource {

    suspend fun getCurrentWeather(city: String): WeatherGeoResponse
}