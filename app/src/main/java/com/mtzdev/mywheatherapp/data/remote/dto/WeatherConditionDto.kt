package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for weather condition data.
 * Represents the "weather" array element in API response.
 */
@Serializable
data class WeatherConditionDto(
    @SerialName("id")
    val id: Int,

    @SerialName("main")
    val main: String,

    @SerialName("description")
    val description: String,

    @SerialName("icon")
    val icon: String
)
