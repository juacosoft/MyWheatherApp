package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for wind data.
 * Contains wind speed and direction.
 */
@Serializable
data class WindDto(
    @SerialName("speed")
    val speed: Double,

    @SerialName("deg")
    val deg: Int? = null
)
