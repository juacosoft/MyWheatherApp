package com.mtzdev.mywheatherapp.data.services

import com.mtzdev.mywheatherapp.commons.APP_ID_PARAM
import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class WeatherDataService(
    private val client: HttpClient,
    private val apiKey: String
) {

    private companion object {
        const val WEATHER_ENDPOINT = "/data/2.5/weather"
        const val WEATHER_PARAM_LAT = "lat"
        const val WEATHER_PARAM_LON = "lon"
    }

    suspend fun getWeatherData(params: WeatherDataParams): WeatherDataModel {
        return client.get(WEATHER_ENDPOINT){
            parameter(APP_ID_PARAM, apiKey)
            parameter(WEATHER_PARAM_LAT, params.lat)
            parameter(WEATHER_PARAM_LON, params.lon)
        }.body()
    }
}