package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for geographic coordinates.
 */
@Serializable
data class CoordinatesDto(
    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lon: Double
)
