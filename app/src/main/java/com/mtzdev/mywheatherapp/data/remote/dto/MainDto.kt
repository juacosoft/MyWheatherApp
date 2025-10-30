package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for main weather parameters.
 * Contains temperature, humidity, and pressure data.
 */
@Serializable
data class MainDto(
    @SerialName("temp")
    val temp: Double,

    @SerialName("feels_like")
    val feelsLike: Double,

    @SerialName("humidity")
    val humidity: Int,

    @SerialName("pressure")
    val pressure: Int
)
