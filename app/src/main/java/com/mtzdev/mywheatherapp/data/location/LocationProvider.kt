package com.mtzdev.mywheatherapp.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mtzdev.mywheatherapp.domain.model.Location
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Provider for obtaining device location using FusedLocationProviderClient.
 * Handles GPS location requests with timeout and error handling.
 *
 * @property context Application context for location services
 */
class LocationProvider(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Gets current device location with 10-second timeout.
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission.
     *
     * @return Location domain model with coordinates
     * @throws LocationException if location cannot be obtained
     * @throws SecurityException if permissions not granted
     * @throws TimeoutCancellationException if operation exceeds timeout
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location {
        return try {
            withTimeout(LOCATION_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val cancellationTokenSource = CancellationTokenSource()

                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnSuccessListener { location ->
                        handleLocationSuccess(location, continuation)
                    }.addOnFailureListener { exception ->
                        handleLocationFailure(exception, continuation)
                    }

                    continuation.invokeOnCancellation {
                        cancellationTokenSource.cancel()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw LocationException.Timeout(
                "Location request timed out after ${LOCATION_TIMEOUT_MS}ms"
            )
        } catch (e: SecurityException) {
            throw LocationException.PermissionDenied(
                "Location permission not granted", e
            )
        }
    }

    private fun handleLocationSuccess(
        location: android.location.Location?,
        continuation: kotlin.coroutines.Continuation<Location>
    ) {
        if (location != null) {
            val domainLocation = Location(
                latitude = location.latitude,
                longitude = location.longitude,
                name = "Current Location",
                country = "XX"
            )
            continuation.resume(domainLocation)
        } else {
            continuation.resumeWithException(
                LocationException.Unavailable(
                    "Location is null - GPS may be disabled"
                )
            )
        }
    }

    private fun handleLocationFailure(
        exception: Exception,
        continuation: kotlin.coroutines.Continuation<Location>
    ) {
        exception.printStackTrace()
        continuation.resumeWithException(
            LocationException.Unavailable(
                "Failed to get location: ${exception.message}",
                exception
            )
        )
    }

    companion object {
        private const val LOCATION_TIMEOUT_MS = 10_000L
    }
}

/**
 * Sealed class hierarchy for location-related exceptions.
 * Represents different types of location retrieval failures.
 */
sealed class LocationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Location permission was denied by the user.
     */
    class PermissionDenied(
        message: String,
        cause: Throwable? = null
    ) : LocationException(message, cause)

    /**
     * Location is unavailable (GPS disabled, no signal, etc.).
     */
    class Unavailable(
        message: String,
        cause: Throwable? = null
    ) : LocationException(message, cause)

    /**
     * Location request exceeded the timeout limit.
     */
    class Timeout(
        message: String
    ) : LocationException(message)
}
