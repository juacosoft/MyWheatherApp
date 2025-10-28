package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import app.cash.turbine.test
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import com.mtzdev.mywheatherapp.getFakeWeatherDataEntity
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
class WeatherDataScreenModelTest {

    @get:Rule
    val testDispatcherRule = MainCoroutineRule()

    @MockK
    private lateinit var getWeatherDataUseCase: GetWeatherDataUseCase

    @InjectMockKs
    private lateinit var weatherDataScreenModel: WeatherDataScreenModel

    @Before
    fun setup(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `GIVEN event is Init WHEN handleEvent is called THEN getWeatherDataUseCase is called`() = runTest {
        val lat = 0.0
        val lon = 0.0
        val expectation = getFakeWeatherDataEntity()
        coEvery { getWeatherDataUseCase.invoke(lat, lon) } returns WeatherDataResult.Data(expectation)

        weatherDataScreenModel.setEvent(WeatherDataContract.Event.Init(getFakeWeatherGeoDataEntity().copy(lat = lat, lon = lon)))
        advanceUntilIdle()

        val result = weatherDataScreenModel.state.value
        assert(result.weatherData == expectation)
        coVerify { getWeatherDataUseCase.invoke(lat, lon) }
    }

    @Test
    fun `GIVEN event is RefreshData WHEN handleEvent is called THEN getWeatherDataUseCase is called`() = runTest {
        val lat = 0.0
        val lon = 0.0
        val expectation = getFakeWeatherDataEntity()
        coEvery { getWeatherDataUseCase.invoke(lat, lon) } returns WeatherDataResult.Data(expectation)
        weatherDataScreenModel.initialStateForTesting(WeatherDataContract.State(currentGeoData = getFakeWeatherGeoDataEntity()))

        weatherDataScreenModel.setEvent(WeatherDataContract.Event.RefreshData)
        advanceUntilIdle()

        val result = weatherDataScreenModel.state.value
        assert(result.weatherData == expectation)
        coVerify { getWeatherDataUseCase.invoke(lat, lon) }
    }

    @Test
    fun `GIVEN event is Init WHEN handleEvent is called THEN getWeatherDataUseCase is called with error`() = runTest {
        val lat = 0.0
        val lon = 0.0
        val error = WeatherException("some error")
        coEvery { getWeatherDataUseCase.invoke(lat, lon) } returns WeatherDataResult.Error(error)
 
        weatherDataScreenModel.effect.test {
            weatherDataScreenModel.setEvent(WeatherDataContract.Event.Init(getFakeWeatherGeoDataEntity().copy(lat = lat, lon = lon)))
            advanceUntilIdle()
 
            val emittedEffect = awaitItem()
            assert(emittedEffect is WeatherDataContract.Effect.ShowError)
            assert((emittedEffect as WeatherDataContract.Effect.ShowError).errorType.message == error.message)
            coVerify { getWeatherDataUseCase.invoke(lat, lon) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}