package com.mtzdev.mywheatherapp.domain.usecase

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.model.WeatherCondition
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GetCurrentWeatherByCoordinatesUseCaseTest {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var useCase: GetCurrentWeatherByCoordinatesUseCase

    @Before
    fun setup() {
        weatherRepository = mock()
        useCase = GetCurrentWeatherByCoordinatesUseCase(weatherRepository)
    }

    @After
    fun tearDown() {
        // Clean up
    }

    @Test
    fun `invoke when repository returns success emits weather data`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val weather = createWeather()

        whenever(
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        ).thenReturn(flowOf(Result.Success(weather)))

        // When & Then
        useCase(lat, lon).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(weather, (result as Result.Success).data)

            awaitComplete()
        }

        verify(weatherRepository).getCurrentWeatherByCoordinates(lat, lon)
    }

    @Test
    fun `invoke when repository returns error emits error`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val error = DomainError.WeatherError.ServerError

        whenever(
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        ).thenReturn(flowOf(Result.Error(error)))

        // When & Then
        useCase(lat, lon).test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(error, (result as Result.Error).error)

            awaitComplete()
        }
    }

    @Test
    fun `invoke when invalid latitude emits validation error without calling repository`() =
        runTest {
            // Given
            val invalidLat = 100.0
            val lon = -3.7026

            // When & Then
            useCase(invalidLat, lon).test {
                val result = awaitItem()
                assertTrue(result is Result.Error)
                val error = (result as Result.Error).error
                assertTrue(error is DomainError.ValidationError.InvalidCoordinates)

                awaitComplete()
            }

            // Should not call repository
            verify(weatherRepository, never()).getCurrentWeatherByCoordinates(any(), any())
        }

    @Test
    fun `invoke when invalid longitude emits validation error without calling repository`() =
        runTest {
            // Given
            val lat = 40.4165
            val invalidLon = -200.0

            // When & Then
            useCase(lat, invalidLon).test {
                val result = awaitItem()
                assertTrue(result is Result.Error)
                val error = (result as Result.Error).error
                assertTrue(error is DomainError.ValidationError.InvalidCoordinates)

                awaitComplete()
            }

            // Should not call repository
            verify(weatherRepository, never()).getCurrentWeatherByCoordinates(any(), any())
        }

    @Test
    fun `invoke with valid coordinates delegates to repository`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val weather = createWeather()

        whenever(
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        ).thenReturn(flowOf(Result.Loading, Result.Success(weather)))

        // When
        useCase(lat, lon).test {
            // Loading
            val loadingResult = awaitItem()
            assertTrue(loadingResult is Result.Loading)

            // Success
            val successResult = awaitItem()
            assertTrue(successResult is Result.Success)

            awaitComplete()
        }

        // Then
        verify(weatherRepository, times(1)).getCurrentWeatherByCoordinates(lat, lon)
    }

    // Helper function to create test weather
    private fun createWeather() = Weather(
        temperature = 25.5,
        feelsLike = 24.8,
        humidity = 60,
        pressure = 1013,
        windSpeed = 3.5,
        windDirection = 180,
        condition = WeatherCondition(
            id = 800,
            main = "Clear",
            description = "cielo claro",
            icon = "01d"
        ),
        location = Location(
            latitude = 40.4165,
            longitude = -3.7026,
            name = "Madrid",
            country = "ES"
        ),
        timestamp = 1698765432L
    )
}
