package com.mtzdev.mywheatherapp.domain.model

/**
 * Domain model representing a weather condition.
 * Contains weather category, description, and icon information.
 *
 * @property id OpenWeatherMap condition ID (see API documentation)
 * @property main Main weather category (Clear, Clouds, Rain, etc.)
 * @property description Detailed weather description in Spanish
 * @property icon Icon code for weather visualization (e.g., "01d" for clear day)
 */
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) {
    init {
        require(id > 0) {
            "Weather condition ID must be positive, got: $id"
        }
        require(main.isNotBlank()) {
            "Weather main category cannot be blank"
        }
        require(description.isNotBlank()) {
            "Weather description cannot be blank"
        }
        require(icon.length in 2..3) {
            "Weather icon code must be 2-3 characters, got: $icon"
        }
    }

    /**
     * Returns the full icon URL for OpenWeatherMap icons.
     * Format: https://openweathermap.org/img/wn/{icon}@2x.png
     *
     * @return Complete URL string for icon image
     */
    fun getIconUrl(): String {
        return "https://openweathermap.org/img/wn/${icon}@2x.png"
    }

    /**
     * Checks if this is a daytime icon.
     * Icons ending with 'd' represent day, 'n' represents night.
     *
     * @return true if daytime icon, false if nighttime
     */
    fun isDayTime(): Boolean = icon.endsWith('d')
}
