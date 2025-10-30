package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.net.UnknownHostException

/**
 * Implementation of WeatherRepository interface.
 * Coordinates data fetching from remote source and mapping to domain models.
 *
 * @property weatherRemoteDataSource Remote data source for weather API
 * @property weatherMapper Mapper for DTO to domain model conversion
 */
class WeatherRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherMapper: WeatherMapper
) : WeatherRepository {

    /**
     * Retrieves current weather data for given coordinates.
     * Validates coordinates, fetches from API, and maps to domain.
     *
     * @param latitude Latitude coordinate (-90.0 to 90.0)
     * @param longitude Longitude coordinate (-180.0 to 180.0)
     * @return Flow emitting Result states (Loading, Success, Error)
     */
    override suspend fun getCurrentWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Flow<Result<Weather>> = flow {
        emit(Result.Loading)

        if (!areCoordinatesValid(latitude, longitude)) {
            emit(Result.Error(DomainError.ValidationError.InvalidCoordinates))
            return@flow
        }

        try {
            val dto = weatherRemoteDataSource.getCurrentWeather(
                latitude = latitude,
                longitude = longitude
            )
            val weather = weatherMapper.mapToDomain(dto)
            emit(Result.Success(weather))
        } catch (e: Exception) {
            val error = mapException(e)
            emit(Result.Error(error))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Validates that coordinates are within acceptable ranges.
     *
     * @param latitude Latitude to validate
     * @param longitude Longitude to validate
     * @return true if coordinates are valid, false otherwise
     */
    private fun areCoordinatesValid(
        latitude: Double,
        longitude: Double
    ): Boolean {
        val isLatitudeValid = latitude in -90.0..90.0
        val isLongitudeValid = longitude in -180.0..180.0
        return isLatitudeValid && isLongitudeValid
    }

    /**
     * Maps exceptions to domain errors.
     * Handles network, API, and unexpected errors.
     *
     * @param exception Exception to map
     * @return DomainError representing the error
     */
    private fun mapException(exception: Exception): DomainError {
        return when (exception) {
            is UnknownHostException ->
                DomainError.WeatherError.NoInternetConnection

            is ClientRequestException -> mapClientError(exception)

            is ServerResponseException ->
                DomainError.WeatherError.ServerError

            else ->
                DomainError.WeatherError.Unknown(exception.message)
        }
    }

    /**
     * Maps client request exceptions to specific domain errors.
     *
     * @param exception Client request exception
     * @return DomainError representing the specific client error
     */
    private fun mapClientError(
        exception: ClientRequestException
    ): DomainError {
        return when (exception.response.status) {
            HttpStatusCode.Unauthorized ->
                DomainError.WeatherError.InvalidApiKey

            HttpStatusCode.NotFound ->
                DomainError.WeatherError.CityNotFound

            HttpStatusCode.TooManyRequests ->
                DomainError.WeatherError.RateLimitExceeded

            else ->
                DomainError.WeatherError.Unknown(exception.message)
        }
    }
}
