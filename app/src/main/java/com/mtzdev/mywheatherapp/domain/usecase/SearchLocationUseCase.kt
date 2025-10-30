package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * T052: Use case for searching locations by city name.
 *
 * Business rules:
 * - City name must not be blank
 * - Trims whitespace from input
 * - Delegates to LocationRepository for actual search
 */
class SearchLocationUseCase(
    private val locationRepository: LocationRepository
) {

    /**
     * Search for locations matching the given city name.
     *
     * @param cityName The city name to search for
     * @return Flow emitting Result states (Loading, Success, Error)
     */
    operator fun invoke(cityName: String): Flow<Result<Location>> = flow {
        // T052: Validate city name is not blank
        val trimmedCityName = cityName.trim()
        if (trimmedCityName.isBlank()) {
            emit(Result.Error(error = DomainError.ValidationError.EmptyCityName))
            return@flow
        }

        // Delegate to repository with trimmed input
        locationRepository.searchLocation(trimmedCityName).collect { result ->
            emit(result)
        }
    }
}
