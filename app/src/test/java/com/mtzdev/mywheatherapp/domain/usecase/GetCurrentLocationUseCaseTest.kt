package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.LocationException
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.entity.LocationEntity
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
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

class GetCurrentLocationUseCaseTest {

    @MockK
    private lateinit var repository: LocationRepository

    @InjectMockKs
    private lateinit var useCase: GetCurrentLocationUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `WHEN invoke is called THEN repository getCurrentLocation is called`() = runTest {
        // Given
        val mockLocation = LocationEntity(
            latitude = 4.6534649,
            longitude = -74.0836453,
            timestamp = System.currentTimeMillis(),
            accuracy = 10.0f
        )
        coEvery { repository.getCurrentLocation() } returns LocationResult.Success(mockLocation)

        // When
        val result = useCase.invoke()

        // Then
        coVerify { repository.getCurrentLocation() }
        assertIs<LocationResult.Success>(result)
    }

    @Test
    fun `WHEN repository returns success THEN usecase returns success with location`() = runTest {
        // Given
        val expectedLocation = LocationEntity(
            latitude = 4.6534649,
            longitude = -74.0836453,
            timestamp = System.currentTimeMillis(),
            accuracy = 10.0f
        )
        coEvery { repository.getCurrentLocation() } returns LocationResult.Success(expectedLocation)

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Success>(result)
        assertEquals(expectedLocation, result.location)
    }

    @Test
    fun `WHEN repository returns permission denied error THEN usecase returns permission denied error`() = runTest {
        // Given
        coEvery { repository.getCurrentLocation() } returns LocationResult.Error(LocationException.PermissionDenied)

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.PermissionDenied>(result.exception)
    }

    @Test
    fun `WHEN repository returns gps disabled error THEN usecase returns gps disabled error`() = runTest {
        // Given
        coEvery { repository.getCurrentLocation() } returns LocationResult.Error(LocationException.GpsDisabled)

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.GpsDisabled>(result.exception)
    }

    @Test
    fun `WHEN repository returns timeout error THEN usecase returns timeout error`() = runTest {
        // Given
        coEvery { repository.getCurrentLocation() } returns LocationResult.Error(LocationException.Timeout)

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.Timeout>(result.exception)
    }

    @Test
    fun `WHEN repository returns play services unavailable error THEN usecase returns play services unavailable error`() = runTest {
        // Given
        coEvery { repository.getCurrentLocation() } returns LocationResult.Error(LocationException.PlayServicesUnavailable)

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.PlayServicesUnavailable>(result.exception)
    }

    @Test
    fun `WHEN repository returns unknown error THEN usecase returns unknown error with message`() = runTest {
        // Given
        val errorMessage = "Test error message"
        coEvery { repository.getCurrentLocation() } returns LocationResult.Error(LocationException.Unknown(errorMessage))

        // When
        val result = useCase.invoke()

        // Then
        assertIs<LocationResult.Error>(result)
        assertIs<LocationException.Unknown>(result.exception)
        assertEquals(errorMessage, (result.exception as LocationException.Unknown).message)
    }
}
