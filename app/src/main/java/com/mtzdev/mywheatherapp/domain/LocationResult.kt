package com.mtzdev.mywheatherapp.domain

import com.mtzdev.mywheatherapp.domain.entity.LocationEntity

/**
 * Resultado de una operación de obtención de ubicación.
 *
 * Representa el resultado de manera type-safe, forzando al consumidor
 * a manejar ambos casos: éxito y error.
 */
sealed class LocationResult {
    /**
     * Operación exitosa con ubicación obtenida.
     *
     * @property location La ubicación del dispositivo
     */
    data class Success(val location: LocationEntity) : LocationResult()

    /**
     * Operación fallida con excepción de ubicación.
     *
     * @property exception La excepción específica que causó el error
     */
    data class Error(val exception: LocationException) : LocationResult()
}
