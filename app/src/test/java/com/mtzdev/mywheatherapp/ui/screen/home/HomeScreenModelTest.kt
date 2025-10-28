package com.mtzdev.mywheatherapp.ui.screen.home

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.LocalNamesEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import com.mtzdev.mywheatherapp.getFakeWeatherGeoDataEntity
import com.mtzdev.mywheatherapp.ui.MainCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModelTest {

    @get:Rule
    val testDispatcherRule = MainCoroutineRule()

    @MockK
    private lateinit var getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase

    @InjectMockKs
    private lateinit var homeScreenModel: HomeScreenModel


    @Before
    fun setup(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `GIVEN event is ChangeUbication WHEN handleEvent is called THEN getWeatherGeoDataUseCase is called with success data`() = runTest {
        val currenCity = "London"
        coEvery { getWeatherGeoDataUseCase.invoke(currenCity) } returns  WeatherGeoResult.Data(getFakeWeatherGeoDataEntity())

        homeScreenModel.state.test {
            awaitItem()
            homeScreenModel.setEvent(HomeContract.Event.ChangeUbication(currenCity))
            val state = awaitItem()
            assert(state.geoData.name == "London")
            assert(state.geoData.lat == 0.0)
            assert(state.geoData.lon == 0.0)
            assert(state.geoData.localNames == getFakeWeatherGeoDataEntity().localNames)
        }
        coVerify { getWeatherGeoDataUseCase.invoke(currenCity) }
    }

    @Test
    fun `GIVEN event is ChangeUbication WHEN handleEvent is called THEN getWeatherGeoDataUseCase is called with error data`() = runTest {
        val currenCity = "London"
        coEvery { getWeatherGeoDataUseCase.invoke(currenCity) } returns  WeatherGeoResult.Error(
            WeatherException("some error"))
        homeScreenModel.setEvent(HomeContract.Event.ChangeUbication(currenCity))
        advanceUntilIdle()

        val initialState = homeScreenModel.state.value
        // defaults values
        assert(initialState.geoData.name == "Bogota")
        assert(initialState.geoData.lat == 4.6534649)
        assert(initialState.geoData.lon == -74.0836453)
        assert(initialState.geoData.localNames == null)
    }
}