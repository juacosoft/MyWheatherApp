package com.mtzdev.mywheatherapp.data.remote.datasource

import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for fetching weather data from OpenWeatherMap API.
 * Handles API communication and returns raw DTOs.
 *
 * @property client Ktor HTTP client for API requests
 * @property apiKey OpenWeatherMap API key
 */
class WeatherRemoteDataSource(
    private val client: HttpClient,
    private val apiKey: String
) {

    /**
     * Fetches current weather data for given coordinates.
     * Uses OpenWeatherMap Current Weather API endpoint.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return WeatherResponseDto from API
     * @throws io.ktor.client.plugins.ClientRequestException if request fails (4xx)
     * @throws io.ktor.client.plugins.ServerResponseException if server error (5xx)
     * @throws java.net.UnknownHostException if no internet connection
     */
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponseDto {
        return client.get(WEATHER_ENDPOINT) {
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("appid", apiKey)
            parameter("units", UNITS_METRIC)
            parameter("lang", LANGUAGE_SPANISH)
        }.body()
    }

    companion object {
        private const val WEATHER_ENDPOINT =
            "https://api.openweathermap.org/data/2.5/weather"
        private const val UNITS_METRIC = "metric"
        private const val LANGUAGE_SPANISH = "es"
    }
}
