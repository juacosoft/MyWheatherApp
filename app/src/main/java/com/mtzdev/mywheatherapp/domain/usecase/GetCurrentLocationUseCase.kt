package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.data.location.LocationException
import com.mtzdev.mywheatherapp.data.location.LocationProvider
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for obtaining the device's current GPS location.
 * Handles location provider interaction and error mapping to domain layer.
 *
 * @property locationProvider Provider for GPS location services
 */
class GetCurrentLocationUseCase(
    private val locationProvider: LocationProvider
) {

    /**
     * Retrieves the current device location.
     * Emits Loading state initially, then Success with location or Error.
     *
     * @return Flow emitting Result states with Location data
     */
    operator fun invoke(): Flow<Result<Location>> = flow {
        emit(Result.Loading)

        try {
            val location = locationProvider.getCurrentLocation()
            emit(Result.Success(location))
        } catch (e: LocationException.PermissionDenied) {
            emit(Result.Error(DomainError.LocationError.PermissionDenied))
        } catch (e: LocationException.Unavailable) {
            emit(Result.Error(DomainError.LocationError.Unavailable))
        } catch (e: LocationException.Timeout) {
            emit(Result.Error(DomainError.LocationError.Timeout))
        } catch (e: TimeoutCancellationException) {
            emit(Result.Error(DomainError.LocationError.Timeout))
        } catch (e: SecurityException) {
            emit(Result.Error(DomainError.LocationError.PermissionDenied))
        } catch (e: Exception) {
            val error = mapUnknownException(e)
            emit(Result.Error(error))
        }
    }

    private fun mapUnknownException(e: Exception): DomainError {
        return when {
            e.message?.contains("gps", ignoreCase = true) == true ->
                DomainError.LocationError.GpsDisabled
            else ->
                DomainError.LocationError.Unavailable
        }
    }
}
