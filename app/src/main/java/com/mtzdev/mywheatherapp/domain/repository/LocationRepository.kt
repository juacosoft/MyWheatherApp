package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location search operations.
 * Defines contract for geocoding and location searches.
 * Implementations handle data sources (geocoding API, etc.).
 */
interface LocationRepository {

    /**
     * Searches for a location by city name using geocoding API.
     * Emits Loading state initially, then Success with location or Error.
     *
     * @param query City name to search for (e.g., "Madrid", "Barcelona")
     * @return Flow emitting Result states (Loading, Success, Error)
     */
    suspend fun searchLocation(query: String): Flow<Result<Location>>
}
