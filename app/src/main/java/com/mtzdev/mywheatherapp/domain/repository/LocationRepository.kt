package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.LocationResult

/**
 * Repositorio de ubicación para obtener coordenadas del dispositivo.
 *
 * Esta interface define el contrato para acceder a servicios de ubicación,
 * permitiendo que la implementación concreta esté en la capa de data.
 *
 * Responsabilidades:
 * - Obtener la ubicación GPS actual del dispositivo
 * - Manejar errores de permisos, GPS desactivado, timeout, etc.
 * - Retornar resultado type-safe (Success o Error)
 */
interface LocationRepository {

    /**
     * Obtiene la ubicación geográfica actual del dispositivo.
     *
     * Esta operación es suspendible y puede tomar hasta 10 segundos.
     * Si la ubicación no se puede obtener en ese tiempo, debe retornar
     * un error de timeout.
     *
     * @return [LocationResult.Success] con la ubicación si se obtuvo correctamente,
     *         [LocationResult.Error] si ocurrió algún error (permiso denegado, GPS desactivado, etc.)
     */
    suspend fun getCurrentLocation(): LocationResult
}
