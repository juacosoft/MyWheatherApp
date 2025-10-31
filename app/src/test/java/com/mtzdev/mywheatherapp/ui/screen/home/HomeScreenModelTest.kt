package com.mtzdev.mywheatherapp.ui.screen.home

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.commons.PermissionManager
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.LocationException
import com.mtzdev.mywheatherapp.domain.LocationResult
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.LocationEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import com.mtzdev.mywheatherapp.getFakeWeatherGeoDataEntity
import com.mtzdev.mywheatherapp.ui.MainCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModelTest {

    @get:Rule
    val testDispatcherRule = MainCoroutineRule()

    @MockK
    private lateinit var getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase

    @MockK
    private lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase

    @MockK
    private lateinit var permissionManager: PermissionManager

    private lateinit var homeScreenModel: HomeScreenModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        homeScreenModel = HomeScreenModel(
            getWeatherGeoDataUseCase = getWeatherGeoDataUseCase,
            getCurrentLocationUseCase = getCurrentLocationUseCase,
            permissionManager = permissionManager
        )
    }

    @Test
    fun `GIVEN event is ChangeUbication WHEN handleEvent is called THEN getWeatherGeoDataUseCase is called with success data`() = runTest {
        val currentCity = "London"
        coEvery { getWeatherGeoDataUseCase.invoke(currentCity) } returns WeatherGeoResult.Data(getFakeWeatherGeoDataEntity())

        homeScreenModel.state.test {
            awaitItem()
            homeScreenModel.setEvent(HomeContract.Event.ChangeUbication(currentCity))
            val state = awaitItem()
            assertNotNull(state.geoData)
            assertEquals("London", state.geoData?.name)
            assertEquals(0.0, state.geoData?.lat)
            assertEquals(0.0, state.geoData?.lon)
            assertEquals(getFakeWeatherGeoDataEntity().localNames, state.geoData?.localNames)
        }
        coVerify { getWeatherGeoDataUseCase.invoke(currentCity) }
    }

    @Test
    fun `GIVEN event is ChangeUbication WHEN handleEvent is called THEN getWeatherGeoDataUseCase is called with error data`() = runTest {
        val currentCity = "London"
        coEvery { getWeatherGeoDataUseCase.invoke(currentCity) } returns WeatherGeoResult.Error(
            WeatherException("some error")
        )
        homeScreenModel.setEvent(HomeContract.Event.ChangeUbication(currentCity))
        advanceUntilIdle()

        val initialState = homeScreenModel.state.value
        // geoData should remain null after error
        assertNull(initialState.geoData)
    }

    @Test
    fun `WHEN initial state THEN geoData is null`() = runTest {
        val initialState = homeScreenModel.state.value
        assertNull(initialState.geoData)
        assertEquals(false, initialState.loadingGeo)
        assertEquals(false, initialState.isLoadingGpsLocation)
        assertEquals(false, initialState.isLoadingCitySearch)
    }

    @Test
    fun `GIVEN permission granted WHEN OnRequestGpsLocation THEN getCurrentLocationUseCase is called`() = runTest {
        every { permissionManager.hasLocationPermission() } returns true
        val mockLocation = LocationEntity(4.0, -74.0, System.currentTimeMillis(), 10.0f)
        coEvery { getCurrentLocationUseCase.invoke() } returns LocationResult.Success(mockLocation)

        homeScreenModel.setEvent(HomeContract.Event.OnRequestGpsLocation)
        advanceUntilIdle()

        coVerify { getCurrentLocationUseCase.invoke() }
    }

    @Test
    fun `GIVEN permission not granted WHEN OnRequestGpsLocation THEN showPermissionDialog is true`() = runTest {
        every { permissionManager.hasLocationPermission() } returns false

        homeScreenModel.state.test {
            awaitItem() // initial state
            homeScreenModel.setEvent(HomeContract.Event.OnRequestGpsLocation)
            val state = awaitItem()
            assertEquals(true, state.showPermissionDialog)
        }
    }

    @Test
    fun `WHEN OnSearchCity with valid city THEN getWeatherGeoDataUseCase is called`() = runTest {
        val cityName = "Bogota"
        coEvery { getWeatherGeoDataUseCase.invoke(cityName) } returns WeatherGeoResult.Data(getFakeWeatherGeoDataEntity())

        homeScreenModel.setEvent(HomeContract.Event.OnSearchCity(cityName))
        advanceUntilIdle()

        coVerify { getWeatherGeoDataUseCase.invoke(cityName) }
    }

    @Test
    fun `WHEN OnSearchQueryChange THEN searchQuery is updated`() = runTest {
        val query = "New York"

        homeScreenModel.state.test {
            awaitItem() // initial state
            homeScreenModel.setEvent(HomeContract.Event.OnSearchQueryChange(query))
            val state = awaitItem()
            assertEquals(query, state.searchQuery)
        }
    }

    @Test
    fun `WHEN OnPermissionDialogDeny THEN showPermissionDialog is false`() = runTest {
        // First show dialog
        every { permissionManager.hasLocationPermission() } returns false
        homeScreenModel.setEvent(HomeContract.Event.OnRequestGpsLocation)
        advanceUntilIdle()

        homeScreenModel.state.test {
            awaitItem() // current state with dialog
            homeScreenModel.setEvent(HomeContract.Event.OnPermissionDialogDeny)
            val state = awaitItem()
            assertEquals(false, state.showPermissionDialog)
        }
    }

    @Test
    fun `WHEN OnClearLocationError THEN locationError is null`() = runTest {
        // First set an error by getting GPS denied
        every { permissionManager.hasLocationPermission() } returns true
        coEvery { getCurrentLocationUseCase.invoke() } returns LocationResult.Error(LocationException.PermissionDenied)
        homeScreenModel.setEvent(HomeContract.Event.OnRequestGpsLocation)
        advanceUntilIdle()

        homeScreenModel.state.test {
            awaitItem() // current state with error
            homeScreenModel.setEvent(HomeContract.Event.OnClearLocationError)
            val state = awaitItem()
            assertNull(state.locationError)
            assertEquals(false, state.showPermissionDeniedMessage)
        }
    }
}
