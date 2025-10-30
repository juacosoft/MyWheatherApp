package com.mtzdev.mywheatherapp.data.repository

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.dto.CoordinatesDto
import com.mtzdev.mywheatherapp.data.remote.dto.MainDto
import com.mtzdev.mywheatherapp.data.remote.dto.SysDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherConditionDto
import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import com.mtzdev.mywheatherapp.data.remote.dto.WindDto
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.net.UnknownHostException

class WeatherRepositoryImplTest {

    private lateinit var weatherRemoteDataSource: WeatherRemoteDataSource
    private lateinit var weatherMapper: WeatherMapper
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        weatherRemoteDataSource = mock()
        weatherMapper = WeatherMapper()
        repository = WeatherRepositoryImpl(
            weatherRemoteDataSource = weatherRemoteDataSource,
            weatherMapper = weatherMapper
        )
    }

    @After
    fun tearDown() {
        // Clean up
    }

    @Test
    fun `getCurrentWeatherByCoordinates when API returns success emits Loading then Success`() =
        runTest {
            // Given
            val lat = 40.4165
            val lon = -3.7026
            val weatherDto = createWeatherResponseDto()

            whenever(
                weatherRemoteDataSource.getCurrentWeather(lat, lon)
            ).thenReturn(weatherDto)

            // When & Then
            repository.getCurrentWeatherByCoordinates(lat, lon).test {
                // First emission: Loading
                val loadingResult = awaitItem()
                assertTrue(loadingResult is Result.Loading)

                // Second emission: Success with weather data
                val successResult = awaitItem()
                assertTrue(successResult is Result.Success)
                val weather = (successResult as Result.Success).data
                assertEquals(25.5, weather.temperature, 0.01)
                assertEquals("Madrid", weather.location.name)

                awaitComplete()
            }

            verify(weatherRemoteDataSource).getCurrentWeather(lat, lon)
        }

    @Test
    fun `getCurrentWeatherByCoordinates when network error emits Loading then Error`() =
        runTest {
            // Given
            val lat = 40.4165
            val lon = -3.7026

            whenever(
                weatherRemoteDataSource.getCurrentWeather(lat, lon)
            ).thenThrow(UnknownHostException())

            // When & Then
            repository.getCurrentWeatherByCoordinates(lat, lon).test {
                // First emission: Loading
                val loadingResult = awaitItem()
                assertTrue(loadingResult is Result.Loading)

                // Second emission: Error
                val errorResult = awaitItem()
                assertTrue(errorResult is Result.Error)
                val error = (errorResult as Result.Error).error
                assertTrue(error is DomainError.WeatherError.NoInternetConnection)

                awaitComplete()
            }
        }

    @Test
    fun `getCurrentWeatherByCoordinates when invalid latitude emits Loading then ValidationError`() =
        runTest {
            // Given
            val invalidLat = 100.0
            val lon = -3.7026

            // When & Then
            repository.getCurrentWeatherByCoordinates(invalidLat, lon).test {
                // First emission: Loading
                val loadingResult = awaitItem()
                assertTrue(loadingResult is Result.Loading)

                // Second emission: ValidationError
                val errorResult = awaitItem()
                assertTrue(errorResult is Result.Error)
                val error = (errorResult as Result.Error).error
                assertTrue(error is DomainError.ValidationError.InvalidCoordinates)

                awaitComplete()
            }

            // Should not call data source
            verify(weatherRemoteDataSource, never()).getCurrentWeather(any(), any())
        }

    @Test
    fun `getCurrentWeatherByCoordinates when 401 error emits Loading then InvalidApiKey`() =
        runTest {
            // Given
            val lat = 40.4165
            val lon = -3.7026

            val mockResponse: HttpResponse = mock {
                on { status } doReturn HttpStatusCode.Unauthorized
            }
            val clientException = ClientRequestException(mockResponse, "")

            whenever(
                weatherRemoteDataSource.getCurrentWeather(lat, lon)
            ).thenThrow(clientException)

            // When & Then
            repository.getCurrentWeatherByCoordinates(lat, lon).test {
                // First emission: Loading
                val loadingResult = awaitItem()
                assertTrue(loadingResult is Result.Loading)

                // Second emission: InvalidApiKey error
                val errorResult = awaitItem()
                assertTrue(errorResult is Result.Error)
                val error = (errorResult as Result.Error).error
                assertTrue(error is DomainError.WeatherError.InvalidApiKey)

                awaitComplete()
            }
        }

    // Helper function to create test DTO
    private fun createWeatherResponseDto() = WeatherResponseDto(
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
}
