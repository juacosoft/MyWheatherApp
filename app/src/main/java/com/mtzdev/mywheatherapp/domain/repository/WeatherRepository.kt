package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for weather data operations.
 * Defines contract for retrieving weather information.
 * Implementations handle data sources (API, cache, etc.).
 */
interface WeatherRepository {

    /**
     * Retrieves current weather data for given coordinates.
     * Emits Loading state initially, then Success or Error.
     *
     * @param latitude Latitude coordinate (-90.0 to 90.0)
     * @param longitude Longitude coordinate (-180.0 to 180.0)
     * @return Flow emitting Result states (Loading, Success, Error)
     */
    suspend fun getCurrentWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Flow<Result<Weather>>
}
