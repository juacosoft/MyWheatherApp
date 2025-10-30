package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for OpenWeatherMap Geocoding API response.
 * API returns a list of these objects, we use only the first result.
 *
 * API: https://api.openweathermap.org/geo/1.0/direct
 */
@Serializable
data class GeocodingResponseDto(
    @SerialName("name")
    val name: String,

    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lon: Double,

    @SerialName("country")
    val country: String,

    @SerialName("state")
    val state: String? = null
)
