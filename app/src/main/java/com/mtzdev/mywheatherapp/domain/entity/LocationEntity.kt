package com.mtzdev.mywheatherapp.domain.entity

/**
 * Entidad de dominio que representa una ubicación geográfica obtenida del dispositivo.
 *
 * Esta entity es completamente independiente de frameworks de Android y representa
 * las coordenadas GPS puras del dispositivo del usuario.
 *
 * @property latitude Latitud en grados decimales (rango: -90.0 a 90.0)
 * @property longitude Longitud en grados decimales (rango: -180.0 a 180.0)
 * @property timestamp Timestamp Unix (en milisegundos) de cuando se obtuvo la ubicación
 * @property accuracy Precisión estimada en metros (opcional)
 */
data class LocationEntity(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float? = null
)
