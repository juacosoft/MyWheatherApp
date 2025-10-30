package com.mtzdev.mywheatherapp.domain.model

import kotlin.math.roundToInt

/**
 * Domain model representing complete weather data for a location.
 * Contains temperature, atmospheric conditions, wind data, and location.
 *
 * @property temperature Actual temperature in Celsius
 * @property feelsLike Perceived temperature in Celsius (wind chill/heat index)
 * @property humidity Relative humidity percentage (0-100)
 * @property pressure Atmospheric pressure in hPa (hectopascals)
 * @property windSpeed Wind speed in km/h
 * @property windDirection Wind direction in degrees (0-360), null if unavailable
 * @property condition Weather condition details (description, icon, etc.)
 * @property location Geographic location for this weather data
 * @property timestamp Unix timestamp in seconds when data was retrieved
 * @throws IllegalArgumentException if any value is out of valid range
 */
data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: Int?,
    val condition: WeatherCondition,
    val location: Location,
    val timestamp: Long
) {
    init {
        require(temperature in -100.0..60.0) {
            "Temperature must be between -100°C and 60°C, got: $temperature"
        }
        require(feelsLike in -100.0..60.0) {
            "Feels like temperature must be between -100°C and 60°C, got: $feelsLike"
        }
        require(humidity in 0..100) {
            "Humidity must be between 0 and 100%, got: $humidity"
        }
        require(pressure > 0) {
            "Pressure must be positive, got: $pressure"
        }
        require(windSpeed >= 0.0) {
            "Wind speed cannot be negative, got: $windSpeed"
        }
        windDirection?.let {
            require(it in 0..360) {
                "Wind direction must be between 0 and 360 degrees, got: $it"
            }
        }
        require(timestamp > 0) {
            "Timestamp must be positive, got: $timestamp"
        }
    }

    /**
     * Returns formatted temperature string.
     * Example: "25°C"
     */
    fun getFormattedTemp(): String =
        "${temperature.roundToInt()}°C"

    /**
     * Returns formatted feels-like temperature string.
     * Example: "Sensación: 24°C"
     */
    fun getFormattedFeelsLike(): String =
        "Sensación: ${feelsLike.roundToInt()}°C"

    /**
     * Returns formatted humidity string.
     * Example: "Humedad: 60%"
     */
    fun getFormattedHumidity(): String =
        "Humedad: $humidity%"

    /**
     * Returns formatted wind speed string.
     * Example: "Viento: 15 km/h"
     */
    fun getFormattedWindSpeed(): String =
        "Viento: ${windSpeed.roundToInt()} km/h"

    /**
     * Returns formatted pressure string.
     * Example: "Presión: 1013 hPa"
     */
    fun getFormattedPressure(): String =
        "Presión: $pressure hPa"

    /**
     * Returns cardinal direction for wind (N, NE, E, SE, S, SW, W, NW).
     * Returns null if windDirection is null.
     */
    fun getWindCardinalDirection(): String? {
        return windDirection?.let { degrees ->
            val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((degrees + 22.5) / 45.0).toInt() % 8
            directions[index]
        }
    }

    /**
     * Checks if weather data is recent (less than 30 minutes old).
     * Useful for determining if data needs refresh.
     *
     * @return true if data is less than 30 minutes old
     */
    fun isRecent(): Boolean {
        val thirtyMinutesInSeconds = 30 * 60
        val currentTime = System.currentTimeMillis() / 1000
        return (currentTime - timestamp) < thirtyMinutesInSeconds
    }
}
