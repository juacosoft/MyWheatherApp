package com.mtzdev.mywheatherapp.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mtzdev.mywheatherapp.data.LocationDataSource
import com.mtzdev.mywheatherapp.domain.LocationException
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.entity.LocationEntity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

/**
 * Implementación de LocationDataSource que usa FusedLocationProviderClient de Google Play Services.
 *
 * Esta clase maneja toda la interacción con APIs de Android para obtener ubicación GPS,
 * convirtiendo errores de Android a excepciones de dominio.
 *
 * @property context Contexto de la aplicación
 * @property fusedLocationClient Cliente de ubicación de Google Play Services
 */
class LocationDataSourceLocal(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationDataSource {

    companion object {
        private const val LOCATION_TIMEOUT_MS = 10_000L // 10 segundos
    }

    /**
     * Obtiene la ubicación actual del dispositivo.
     *
     * Proceso:
     * 1. Verifica permisos de ubicación
     * 2. Verifica que GPS esté habilitado
     * 3. Verifica que Play Services esté disponible
     * 4. Intenta obtener lastLocation (más rápido)
     * 5. Si no hay lastLocation, obtiene ubicación actual con timeout de 10s
     * 6. Convierte android.location.Location a LocationEntity
     *
     * @return [LocationResult.Success] con ubicación o [LocationResult.Error] con excepción específica
     */
    override suspend fun getCurrentLocation(): LocationResult {
        // 1. Verificar permisos
        if (!hasLocationPermission()) {
            return LocationResult.Error(LocationException.PermissionDenied)
        }

        // 2. Verificar GPS habilitado
        if (!isGpsEnabled()) {
            return LocationResult.Error(LocationException.GpsDisabled)
        }

        // 3. Verificar Play Services disponible
        if (!isPlayServicesAvailable()) {
            return LocationResult.Error(LocationException.PlayServicesUnavailable)
        }

        return try {
            // 4. Intentar obtener última ubicación conocida (más rápido)
            val lastLocation = getLastKnownLocation()
            if (lastLocation != null) {
                return LocationResult.Success(lastLocation)
            }

            // 5. Si no hay última ubicación, obtener ubicación actual con timeout
            withTimeout(LOCATION_TIMEOUT_MS) {
                val currentLocation = getCurrentLocationWithTimeout()
                LocationResult.Success(currentLocation)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            LocationResult.Error(LocationException.Timeout)
        } catch (e: SecurityException) {
            LocationResult.Error(LocationException.PermissionDenied)
        } catch (e: Exception) {
            LocationResult.Error(LocationException.Unknown(e.message ?: "Error desconocido"))
        }
    }

    /**
     * Verifica si la app tiene permisos de ubicación.
     */
    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Verifica si el GPS está habilitado en el dispositivo.
     */
    private fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    /**
     * Verifica si Google Play Services está disponible en el dispositivo.
     */
    private fun isPlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    /**
     * Obtiene la última ubicación conocida del dispositivo.
     *
     * Esta es la forma más rápida de obtener ubicación, pero puede ser null
     * si el usuario nunca ha usado GPS o la ubicación es muy antigua.
     *
     * @return LocationEntity o null si no hay ubicación conocida
     */
    @Suppress("MissingPermission") // Ya verificado en hasLocationPermission()
    private suspend fun getLastKnownLocation(): LocationEntity? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location.toLocationEntity())
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    /**
     * Obtiene la ubicación actual del dispositivo con timeout.
     *
     * Usa getCurrentLocation() de FusedLocationProviderClient que es más
     * preciso pero puede tomar varios segundos.
     *
     * @return LocationEntity con la ubicación actual
     * @throws Exception si no se puede obtener la ubicación
     */
    @Suppress("MissingPermission") // Ya verificado en hasLocationPermission()
    private suspend fun getCurrentLocationWithTimeout(): LocationEntity {
        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location.toLocationEntity())
                    } else {
                        continuation.resumeWith(
                            Result.failure(Exception("No se pudo obtener ubicación"))
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWith(Result.failure(exception))
                }
        }
    }

    /**
     * Convierte android.location.Location a LocationEntity de dominio.
     */
    private fun android.location.Location.toLocationEntity(): LocationEntity {
        return LocationEntity(
            latitude = latitude,
            longitude = longitude,
            timestamp = time,
            accuracy = if (hasAccuracy()) accuracy else null
        )
    }
}
