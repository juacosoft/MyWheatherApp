package com.mtzdev.mywheatherapp.data.remote

import com.mtzdev.mywheatherapp.data.WeatherGeoDataSource
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoResponse
import com.mtzdev.mywheatherapp.data.services.WeatherDataService

class WeatherGeoDataSourceRemote(
    private val weatherGeoService: WeatherDataService
): WeatherGeoDataSource {

    override suspend fun getCurrentWeather(
        city: String
    ): WeatherGeoResponse {
        try {
            val response = weatherGeoService.getGeoData(city)
            return WeatherGeoResponse.Success(response.first())
        } catch (e: Exception) {
            return WeatherGeoResponse.Error(e)
        }
    }
}