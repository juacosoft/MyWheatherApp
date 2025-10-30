package com.mtzdev.mywheatherapp.data.remote.datasource

import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * T050-T051: Remote data source for OpenWeatherMap Geocoding API.
 *
 * Endpoint: https://api.openweathermap.org/geo/1.0/direct
 * - Returns list of locations matching the city name
 * - Limit to 5 results for performance
 * - Uses existing HttpClient with 3s timeout
 */
class GeocodingRemoteDataSource(
    private val httpClient: HttpClient,
    private val apiKey: String
) {

    /**
     * T050: Search locations by city name.
     *
     * @param cityName The city name to search for
     * @param limit Maximum number of results (default 5)
     * @return List of locations matching the query
     * @throws Exception for network errors (handled by repository)
     */
    suspend fun searchLocation(
        cityName: String,
        limit: Int = 5
    ): List<GeocodingResponseDto> {
        return httpClient.get("https://api.openweathermap.org/geo/1.0/direct") {
            parameter("q", cityName)
            parameter("limit", limit)
            parameter("appid", apiKey)
        }.body()
    }
}
