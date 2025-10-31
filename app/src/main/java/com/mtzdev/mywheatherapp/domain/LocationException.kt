package com.mtzdev.mywheatherapp.domain

/**
 * Excepciones específicas del dominio de ubicación.
 *
 * Estas excepciones son completamente independientes de Android y representan
 * casos de error de negocio en la obtención de ubicación del dispositivo.
 */
sealed class LocationException(message: String) : Exception(message) {

    /**
     * El usuario denegó el permiso de ubicación.
     */
    data object PermissionDenied : LocationException("Permiso de ubicación denegado")

    /**
     * El GPS está desactivado en el dispositivo.
     */
    data object GpsDisabled : LocationException("GPS desactivado en dispositivo")

    /**
     * Timeout al intentar obtener la ubicación (excede 10 segundos).
     */
    data object Timeout : LocationException("Timeout al obtener ubicación")

    /**
     * Google Play Services no está disponible en el dispositivo.
     */
    data object PlayServicesUnavailable : LocationException("Google Play Services no disponible")

    /**
     * Error desconocido durante la obtención de ubicación.
     *
     * @property message Mensaje descriptivo del error
     */
    data class Unknown(override val message: String) : LocationException(message)
}
