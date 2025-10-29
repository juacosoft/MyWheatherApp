package com.mtzdev.mywheatherapp.data.services

import com.mtzdev.mywheatherapp.commons.APP_ID_PARAM
import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoModel
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
        const val GEO_ENDPOINT = "/geo/1.0/direct"
        const val WEATHER_PARAM_CITY = "q"
        const val WEATHER_PARAM_LAT = "lat"
        const val WEATHER_PARAM_LON = "lon"
        const val WEATHER_PARAM_UNITS = "units"
        const val WEATHER_PARAM_METRIC = "metric"
    }

    suspend fun getWeatherData(params: WeatherDataParams): WeatherDataModel {
        return client.get(WEATHER_ENDPOINT){
            parameter(APP_ID_PARAM, apiKey)
            parameter(WEATHER_PARAM_LAT, params.lat)
            parameter(WEATHER_PARAM_LON, params.lon)
            parameter(WEATHER_PARAM_UNITS, WEATHER_PARAM_METRIC)
        }.body()
    }

    suspend fun getGeoData(
        city: String
    ): List<WeatherGeoModel> {
        return client.get(GEO_ENDPOINT) {
            parameter(APP_ID_PARAM, apiKey)
            parameter(WEATHER_PARAM_CITY, city)
        }.body()
    }
}