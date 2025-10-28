package com.mtzdev.mywheatherapp.data.remote

import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataResponse
import com.mtzdev.mywheatherapp.data.services.WeatherDataService

class WeatherDataDataSourceRemote(
    private val service: WeatherDataService
): WeatherDataDataSource {
    override suspend fun getWeatherData(
        params: WeatherDataParams
    ): WeatherDataResponse {
        return try {
            WeatherDataResponse.Success(service.getWeatherData(params))
        }catch (e: Exception){
            WeatherDataResponse.Error(e)
        }
    }
}