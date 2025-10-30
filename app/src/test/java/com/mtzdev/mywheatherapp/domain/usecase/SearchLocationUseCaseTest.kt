package com.mtzdev.mywheatherapp.domain.usecase

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * T062-T064: Test suite for SearchLocationUseCase.
 *
 * Tests:
 * - T062: Validates blank city name
 * - T063: Trims whitespace from input
 * - T064: Delegates to repository with trimmed input
 */
class SearchLocationUseCaseTest {

    private lateinit var useCase: SearchLocationUseCase
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchLocationUseCase(repository)
    }

    @Test
    fun `invoke_whenCityNameIsBlank_emitsValidationError`() = runTest {
        // Given
        val blankCityName = "   "

        // When
        val flow = useCase(blankCityName)

        // Then
        flow.test {
            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            val error = (errorItem as Result.Error).error
            assertTrue(error is DomainError.ValidationError.EmptyCityName)

            awaitComplete()
        }
    }

    @Test
    fun `invoke_whenCityNameHasWhitespace_trimsAndDelegatesToRepository`() = runTest {
        // Given
        val cityNameWithWhitespace = "  Madrid  "
        val trimmedCityName = "Madrid"
        val mockLocation = Location(
            latitude = 40.4165,
            longitude = -3.7026,
            name = "Madrid",
            country = "ES"
        )
        coEvery { repository.searchLocation(trimmedCityName) } returns flowOf(
            Result.Success(mockLocation)
        )

        // When
        val flow = useCase(cityNameWithWhitespace)

        // Then
        flow.test {
            val successItem = awaitItem()
            assertTrue(successItem is Result.Success)

            awaitComplete()
        }

        // Verify repository was called with trimmed input
        verify { repository.searchLocation(trimmedCityName) }
    }

    @Test
    fun `invoke_whenValidCityName_delegatesToRepository`() = runTest {
        // Given
        val cityName = "Madrid"
        val mockLocation = Location(
            latitude = 40.4165,
            longitude = -3.7026,
            name = "Madrid",
            country = "ES"
        )
        coEvery { repository.searchLocation(cityName) } returns flowOf(
            Result.Loading,
            Result.Success(mockLocation)
        )

        // When
        val flow = useCase(cityName)

        // Then
        flow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Result.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Result.Success)
            assertEquals(mockLocation, (successItem as Result.Success).data)

            awaitComplete()
        }

        // Verify repository was called
        verify { repository.searchLocation(cityName) }
    }
}
