package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.mapper.LocationMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.UnknownHostException

/**
 * T053-T055: Implementation of LocationRepository.
 *
 * Responsibilities:
 * - Call GeocodingRemoteDataSource
 * - Map DTOs to domain models using LocationMapper
 * - Handle empty results (T054)
 * - Map exceptions to domain errors (T055)
 * - Execute on IO dispatcher
 */
class LocationRepositoryImpl(
    private val remoteDataSource: GeocodingRemoteDataSource,
    private val locationMapper: LocationMapper
) : LocationRepository {

    override suspend fun searchLocation(cityName: String): Flow<Result<Location>> = flow {
        emit(Result.Loading)

        try {
            // T053: Call remote data source
            val dtoList = remoteDataSource.searchLocation(cityName)

            // T054: Handle empty results
            if (dtoList.isEmpty()) {
                emit(Result.Error(error = DomainError.WeatherError.CityNotFound))
                return@flow
            }

            // Map first result to domain model (geocoding returns best match first)
            val location = locationMapper.mapToDomain(dtoList.first())
            emit(Result.Success(location))

        } catch (e: UnknownHostException) {
            // T055: Network connectivity error
            emit(Result.Error(error = DomainError.WeatherError.NoInternetConnection))
        } catch (e: ClientRequestException) {
            // T055: HTTP errors from API
            val error = when (e.response.status.value) {
                401 -> DomainError.WeatherError.InvalidApiKey
                429 -> DomainError.WeatherError.RateLimitExceeded
                else -> DomainError.WeatherError.Unknown(e.message)
            }
            emit(Result.Error(error = error))
        } catch (e: Exception) {
            // T055: Generic error handling
            emit(Result.Error(error = DomainError.WeatherError.Unknown(e.message)))
        }
    }.flowOn(Dispatchers.IO)
}
