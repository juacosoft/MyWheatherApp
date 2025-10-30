package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for OpenWeatherMap Current Weather API response.
 * Maps directly to JSON structure from API endpoint.
 *
 * API: https://api.openweathermap.org/data/2.5/weather
 */
@Serializable
data class WeatherResponseDto(
    @SerialName("coord")
    val coord: CoordinatesDto? = null,

    @SerialName("weather")
    val weather: List<WeatherConditionDto>,

    @SerialName("main")
    val main: MainDto,

    @SerialName("wind")
    val wind: WindDto,

    @SerialName("dt")
    val timestamp: Long,

    @SerialName("name")
    val name: String,

    @SerialName("sys")
    val sys: SysDto
)
