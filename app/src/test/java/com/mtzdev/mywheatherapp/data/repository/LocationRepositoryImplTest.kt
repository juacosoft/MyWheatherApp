package com.mtzdev.mywheatherapp.data.repository

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.data.mapper.LocationMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

/**
 * T058-T061: Test suite for LocationRepositoryImpl.
 *
 * Tests:
 * - T058: City found successfully
 * - T059: City not found (empty results)
 * - T060: Blank city name (handled by use case, but validated here)
 * - T061: Network error handling
 */
class LocationRepositoryImplTest {

    private lateinit var repository: LocationRepositoryImpl
    private lateinit var remoteDataSource: GeocodingRemoteDataSource
    private lateinit var locationMapper: LocationMapper

    @Before
    fun setup() {
        remoteDataSource = mockk()
        locationMapper = LocationMapper() // Use real mapper for simplicity
        repository = LocationRepositoryImpl(remoteDataSource, locationMapper)
    }

    @Test
    fun `searchLocation_whenCityFound_emitsLoadingThenSuccess`() = runTest {
        // Given
        val cityName = "Madrid"
        val dtoList = listOf(
            GeocodingResponseDto(
                name = "Madrid",
                lat = 40.4165,
                lon = -3.7026,
                country = "ES",
                state = "Community of Madrid"
            )
        )
        coEvery { remoteDataSource.searchLocation(cityName) } returns dtoList

        // When
        val flow = repository.searchLocation(cityName)

        // Then
        flow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Result.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Result.Success)
            val location = (successItem as Result.Success).data
            assertEquals("Madrid, Community of Madrid", location.name)
            assertEquals("ES", location.country)

            awaitComplete()
        }
    }

    @Test
    fun `searchLocation_whenCityNotFound_emitsCityNotFoundError`() = runTest {
        // Given
        val cityName = "NonExistentCity"
        coEvery { remoteDataSource.searchLocation(cityName) } returns emptyList()

        // When
        val flow = repository.searchLocation(cityName)

        // Then
        flow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Result.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            val error = (errorItem as Result.Error).error
            assertTrue(error is DomainError.WeatherError.CityNotFound)

            awaitComplete()
        }
    }

    @Test
    fun `searchLocation_whenNetworkError_emitsNoInternetConnectionError`() = runTest {
        // Given
        val cityName = "Madrid"
        coEvery { remoteDataSource.searchLocation(cityName) } throws UnknownHostException()

        // When
        val flow = repository.searchLocation(cityName)

        // Then
        flow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Result.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            val error = (errorItem as Result.Error).error
            assertTrue(error is DomainError.WeatherError.NoInternetConnection)

            awaitComplete()
        }
    }

    @Test
    fun `searchLocation_whenUnauthorized_emitsInvalidApiKeyError`() = runTest {
        // Given
        val cityName = "Madrid"
        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.Unauthorized
        }
        val exception = ClientRequestException(mockResponse, "Unauthorized")
        coEvery { remoteDataSource.searchLocation(cityName) } throws exception

        // When
        val flow = repository.searchLocation(cityName)

        // Then
        flow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Result.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            val error = (errorItem as Result.Error).error
            assertTrue(error is DomainError.WeatherError.InvalidApiKey)

            awaitComplete()
        }
    }
}
