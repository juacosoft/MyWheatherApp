package com.mtzdev.mywheatherapp.data

import com.mtzdev.mywheatherapp.domain.LocationResult

/**
 * Data source para obtener ubicación del dispositivo.
 *
 * Esta interface abstrae el acceso a servicios de localización,
 * permitiendo diferentes implementaciones (GPS, Network, Mock para testing).
 */
interface LocationDataSource {

    /**
     * Obtiene la ubicación actual del dispositivo.
     *
     * @return [LocationResult] con la ubicación o error
     */
    suspend fun getCurrentLocation(): LocationResult
}
