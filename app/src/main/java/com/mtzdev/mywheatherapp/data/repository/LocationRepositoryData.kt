package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.LocationDataSource
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository

/**
 * Implementación del repositorio de ubicación.
 *
 * Esta clase actúa como mediador entre el dominio y la capa de datos,
 * delegando la obtención de ubicación al data source correspondiente.
 *
 * @property dataSource Data source de ubicación (local GPS)
 */
class LocationRepositoryData(
    private val dataSource: LocationDataSource
) : LocationRepository {

    /**
     * Obtiene la ubicación actual delegando al data source.
     *
     * @return [LocationResult] con ubicación o error
     */
    override suspend fun getCurrentLocation(): LocationResult {
        return dataSource.getCurrentLocation()
    }
}
