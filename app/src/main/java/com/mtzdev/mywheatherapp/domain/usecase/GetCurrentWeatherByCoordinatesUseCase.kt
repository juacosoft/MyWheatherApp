package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for retrieving weather data by geographic coordinates.
 * Validates coordinates before delegating to repository.
 *
 * @property weatherRepository Repository for weather data operations
 */
class GetCurrentWeatherByCoordinatesUseCase(
    private val weatherRepository: WeatherRepository
) {

    /**
     * Retrieves current weather for given coordinates.
     * Validates coordinates before making repository call.
     *
     * @param latitude Latitude coordinate (-90.0 to 90.0)
     * @param longitude Longitude coordinate (-180.0 to 180.0)
     * @return Flow emitting Result states with Weather data
     */
    operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Flow<Result<Weather>> = flow {
        if (!isValidCoordinates(latitude, longitude)) {
            emit(Result.Error(DomainError.ValidationError.InvalidCoordinates))
            return@flow
        }

        weatherRepository.getCurrentWeatherByCoordinates(
            latitude = latitude,
            longitude = longitude
        ).collect { result ->
            emit(result)
        }
    }

    /**
     * Validates that coordinates are within valid ranges.
     *
     * @param latitude Latitude to validate
     * @param longitude Longitude to validate
     * @return true if coordinates are valid, false otherwise
     */
    private fun isValidCoordinates(
        latitude: Double,
        longitude: Double
    ): Boolean {
        val isLatitudeValid = latitude in -90.0..90.0
        val isLongitudeValid = longitude in -180.0..180.0
        return isLatitudeValid && isLongitudeValid
    }
}
