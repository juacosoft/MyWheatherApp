package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.CoordinatesDto
import com.mtzdev.mywheatherapp.data.remote.dto.SysDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherConditionDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.model.WeatherCondition

/**
 * Mapper for converting WeatherResponseDto to Weather domain model.
 * Handles data transformation and validation from API response to domain.
 */
class WeatherMapper {

    /**
     * Maps WeatherResponseDto to Weather domain model.
     * Extracts and validates all weather data from API response.
     *
     * @param dto API response DTO
     * @return Weather domain model
     * @throws IllegalArgumentException if weather array is empty
     * @throws IllegalArgumentException if required fields are invalid
     */
    fun mapToDomain(dto: WeatherResponseDto): Weather {
        require(dto.weather.isNotEmpty()) {
            "Weather condition list cannot be empty"
        }

        val weatherConditionDto = dto.weather.first()
        val location = mapLocation(dto.coord, dto.name, dto.sys)
        val condition = mapWeatherCondition(weatherConditionDto)

        return Weather(
            temperature = dto.main.temp,
            feelsLike = dto.main.feelsLike,
            humidity = dto.main.humidity,
            pressure = dto.main.pressure,
            windSpeed = dto.wind.speed,
            windDirection = dto.wind.deg,
            condition = condition,
            location = location,
            timestamp = dto.timestamp
        )
    }

    /**
     * Maps location data from DTO to Location domain model.
     * Handles nullable coordinates with fallback values.
     *
     * @param coord Coordinates DTO (nullable)
     * @param name City name
     * @param sys System data containing country code
     * @return Location domain model
     */
    private fun mapLocation(
        coord: CoordinatesDto?,
        name: String,
        sys: SysDto
    ): Location {
        val latitude = coord?.lat ?: DEFAULT_LATITUDE
        val longitude = coord?.lon ?: DEFAULT_LONGITUDE

        return Location(
            latitude = latitude,
            longitude = longitude,
            name = name,
            country = sys.country
        )
    }

    /**
     * Maps weather condition DTO to domain model.
     *
     * @param dto Weather condition DTO
     * @return WeatherCondition domain model
     */
    private fun mapWeatherCondition(
        dto: WeatherConditionDto
    ): WeatherCondition {
        return WeatherCondition(
            id = dto.id,
            main = dto.main,
            description = dto.description,
            icon = dto.icon
        )
    }

    companion object {
        private const val DEFAULT_LATITUDE = 0.0
        private const val DEFAULT_LONGITUDE = 0.0
    }
}
