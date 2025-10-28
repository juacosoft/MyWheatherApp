package com.mtzdev.mywheatherapp.data.services

import com.mtzdev.mywheatherapp.commons.APP_ID_PARAM
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class WeatherGeoService(
    private val client: HttpClient,
    private val apiKey: String
) {

    private companion object {
        const val GEO_ENDPOINT = "/geo/1.0/direct"
        const val WEATHER_PARAM_CITY = "q"
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