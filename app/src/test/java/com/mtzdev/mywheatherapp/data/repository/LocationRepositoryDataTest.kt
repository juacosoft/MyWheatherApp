package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.LocationDataSource
import com.mtzdev.mywheatherapp.domain.LocationException
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.entity.LocationEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LocationRepositoryDataTest {

    @MockK
    private lateinit var dataSource: LocationDataSource

    @InjectMockKs
    private lateinit var repository: LocationRepositoryData

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `WHEN getCurrentLocation is called THEN dataSource getCurrentLocation is called`() = runTest {
        // Given
        val mockLocation = LocationEntity(
            latitude = 4.6534649,
            longitude = -74.0836453,
            timestamp = System.currentTimeMillis(),
            accuracy = 10.0f
        )
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Success(mockLocation)

        // When
        val result = repository.getCurrentLocation()

        // Then
        coVerify { dataSource.getCurrentLocation() }
        assertIs<LocationResult.Success>(result)
    }

    @Test
    fun `WHEN dataSource returns success THEN repository returns success`() = runTest {
        // Given
        val expectedLocation = LocationEntity(
            latitude = 4.6534649,
            longitude = -74.0836453,
            timestamp = System.currentTimeMillis(),
            accuracy = 15.5f
        )
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Success(expectedLocation)

        // When
        val result = repository.getCurrentLocation()

        // Then
        assertIs<LocationResult.Success>(result)
        assertEquals(expectedLocation, result.location)
    }

    @Test
    fun `WHEN dataSource returns error THEN repository returns error`() = runTest {
        // Given
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Error(LocationException.PermissionDenied)

        // When
        val result = repository.getCurrentLocation()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.PermissionDenied>(result.exception)
    }

    @Test
    fun `WHEN dataSource returns GPS disabled error THEN repository returns GPS disabled error`() = runTest {
        // Given
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Error(LocationException.GpsDisabled)

        // When
        val result = repository.getCurrentLocation()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.GpsDisabled>(result.exception)
    }

    @Test
    fun `WHEN dataSource returns timeout error THEN repository returns timeout error`() = runTest {
        // Given
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Error(LocationException.Timeout)

        // When
        val result = repository.getCurrentLocation()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.Timeout>(result.exception)
    }

    @Test
    fun `WHEN dataSource returns unknown error THEN repository returns unknown error with message`() = runTest {
        // Given
        val errorMessage = "Test unknown error"
        coEvery { dataSource.getCurrentLocation() } returns LocationResult.Error(LocationException.Unknown(errorMessage))

        // When
        val result = repository.getCurrentLocation()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.Unknown>(result.exception)
        assertEquals(errorMessage, (result.exception as LocationException.Unknown).message)
    }
}
