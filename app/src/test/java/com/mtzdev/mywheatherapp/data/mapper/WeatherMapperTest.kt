package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.CoordinatesDto
import com.mtzdev.mywheatherapp.data.remote.dto.MainDto
import com.mtzdev.mywheatherapp.data.remote.dto.SysDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherConditionDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import com.mtzdev.mywheatherapp.data.remote.dto.WindDto
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeatherMapperTest {

    private lateinit var mapper: WeatherMapper

    @Before
    fun setup() {
        mapper = WeatherMapper()
    }

    @Test
    fun `mapToDomain when valid DTO returns complete domain model`() {
        // Given - Valid DTO with all fields
        val dto = WeatherResponseDto(
            coord = CoordinatesDto(lat = 40.4165, lon = -3.7026),
            weather = listOf(
                WeatherConditionDto(
                    id = 800,
                    main = "Clear",
                    description = "cielo claro",
                    icon = "01d"
                )
            ),
            main = MainDto(
                temp = 25.5,
                feelsLike = 24.8,
                humidity = 60,
                pressure = 1013
            ),
            wind = WindDto(speed = 3.5, deg = 180),
            timestamp = 1698765432L,
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When
        val weather = mapper.mapToDomain(dto)

        // Then - Verify all fields mapped correctly
        assertEquals(25.5, weather.temperature, 0.01)
        assertEquals(24.8, weather.feelsLike, 0.01)
        assertEquals(60, weather.humidity)
        assertEquals(1013, weather.pressure)
        assertEquals(3.5, weather.windSpeed, 0.01)
        assertEquals(180, weather.windDirection)

        // Verify condition mapping
        assertEquals(800, weather.condition.id)
        assertEquals("Clear", weather.condition.main)
        assertEquals("cielo claro", weather.condition.description)
        assertEquals("01d", weather.condition.icon)

        // Verify location mapping
        assertEquals(40.4165, weather.location.latitude, 0.0001)
        assertEquals(-3.7026, weather.location.longitude, 0.0001)
        assertEquals("Madrid", weather.location.name)
        assertEquals("ES", weather.location.country)

        // Verify timestamp
        assertEquals(1698765432L, weather.timestamp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mapToDomain when empty weather list throws exception`() {
        // Given - DTO with empty weather list
        val dto = WeatherResponseDto(
            coord = CoordinatesDto(lat = 40.4165, lon = -3.7026),
            weather = emptyList(), // Invalid - empty list
            main = MainDto(
                temp = 25.5,
                feelsLike = 24.8,
                humidity = 60,
                pressure = 1013
            ),
            wind = WindDto(speed = 3.5, deg = 180),
            timestamp = 1698765432L,
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When/Then - Expect exception
        mapper.mapToDomain(dto)
    }

    @Test
    fun `mapToDomain when nullable coord handles null properly`() {
        // Given - DTO with null coordinates
        val dto = WeatherResponseDto(
            coord = null, // Nullable field
            weather = listOf(
                WeatherConditionDto(
                    id = 800,
                    main = "Clear",
                    description = "cielo claro",
                    icon = "01d"
                )
            ),
            main = MainDto(
                temp = 25.5,
                feelsLike = 24.8,
                humidity = 60,
                pressure = 1013
            ),
            wind = WindDto(speed = 3.5, deg = null), // Null wind direction
            timestamp = 1698765432L,
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When
        val weather = mapper.mapToDomain(dto)

        // Then - Should use default coordinates
        assertEquals(0.0, weather.location.latitude, 0.0001)
        assertEquals(0.0, weather.location.longitude, 0.0001)
        assertEquals("Madrid", weather.location.name)
        assertEquals("ES", weather.location.country)

        // Wind direction should be null
        assertNull(weather.windDirection)
    }

    @Test
    fun `mapToDomain when wind direction is null handles it properly`() {
        // Given
        val dto = WeatherResponseDto(
            coord = CoordinatesDto(lat = 40.4165, lon = -3.7026),
            weather = listOf(
                WeatherConditionDto(
                    id = 800,
                    main = "Clear",
                    description = "cielo claro",
                    icon = "01d"
                )
            ),
            main = MainDto(
                temp = 25.5,
                feelsLike = 24.8,
                humidity = 60,
                pressure = 1013
            ),
            wind = WindDto(speed = 3.5, deg = null),
            timestamp = 1698765432L,
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When
        val weather = mapper.mapToDomain(dto)

        // Then
        assertNull(weather.windDirection)
        assertEquals(3.5, weather.windSpeed, 0.01)
    }

    @Test
    fun `mapToDomain with multiple weather conditions uses first one`() {
        // Given - DTO with multiple weather conditions
        val dto = WeatherResponseDto(
            coord = CoordinatesDto(lat = 40.4165, lon = -3.7026),
            weather = listOf(
                WeatherConditionDto(
                    id = 800,
                    main = "Clear",
                    description = "cielo claro",
                    icon = "01d"
                ),
                WeatherConditionDto(
                    id = 801,
                    main = "Clouds",
                    description = "nubes",
                    icon = "02d"
                )
            ),
            main = MainDto(
                temp = 25.5,
                feelsLike = 24.8,
                humidity = 60,
                pressure = 1013
            ),
            wind = WindDto(speed = 3.5, deg = 180),
            timestamp = 1698765432L,
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When
        val weather = mapper.mapToDomain(dto)

        // Then - Should use first condition
        assertEquals(800, weather.condition.id)
        assertEquals("Clear", weather.condition.main)
        assertEquals("cielo claro", weather.condition.description)
    }
}
