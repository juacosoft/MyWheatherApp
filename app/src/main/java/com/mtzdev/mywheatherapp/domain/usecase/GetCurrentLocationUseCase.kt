package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository

/**
 * Caso de uso para obtener la ubicación GPS actual del dispositivo.
 *
 * Este UseCase encapsula la lógica de negocio de obtener la ubicación del usuario,
 * delegando la implementación técnica al repositorio.
 *
 * Ejemplo de uso:
 * ```
 * val result = getCurrentLocationUseCase()
 * when (result) {
 *     is LocationResult.Success -> {
 *         val location = result.location
 *         // Usar coordenadas: location.latitude, location.longitude
 *     }
 *     is LocationResult.Error -> {
 *         when (result.exception) {
 *             is LocationException.PermissionDenied -> // Manejar permiso denegado
 *             is LocationException.GpsDisabled -> // Manejar GPS desactivado
 *             is LocationException.Timeout -> // Manejar timeout
 *             else -> // Manejar otros errores
 *         }
 *     }
 * }
 * ```
 *
 * @property repository Repositorio de ubicación inyectado
 */
class GetCurrentLocationUseCase(
    private val repository: LocationRepository
) {
    /**
     * Invoca el caso de uso para obtener la ubicación actual.
     *
     * Esta operación es suspendible y puede tomar hasta 10 segundos.
     *
     * @return [LocationResult] con el resultado de la operación
     */
    suspend operator fun invoke(): LocationResult = repository.getCurrentLocation()
}
