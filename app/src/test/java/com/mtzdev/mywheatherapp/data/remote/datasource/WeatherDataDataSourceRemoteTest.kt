package com.mtzdev.mywheatherapp.data.remote.datasource

import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataResponse
import com.mtzdev.mywheatherapp.data.remote.WeatherDataDataSourceRemote
import com.mtzdev.mywheatherapp.data.services.WeatherDataService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WeatherDataDataSourceRemoteTest {

    @MockK
    private lateinit var weatherDataService: WeatherDataService

    @InjectMockKs
    private lateinit var weatherDataDataSourceRemote: WeatherDataDataSourceRemote

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `WHEN getWeatherData is called THEN result Success`() = runTest {
        val params = WeatherDataParams(
            lat = 1.0,
            lon = 2.0
        )

        coEvery { weatherDataService.getWeatherData(params) } returns mockk()

        val result = weatherDataDataSourceRemote.getWeatherData(params)

        assert(result is WeatherDataResponse.Success)
    }

    @Test
    fun `WHEN getWeatherData is called THEN result Error`() = runTest {
        val params = WeatherDataParams(
            lat = 1.0,
            lon = 2.0
        )

        coEvery { weatherDataService.getWeatherData(params) } throws Exception("Error")

        val result = weatherDataDataSourceRemote.getWeatherData(params)

        assert(result is WeatherDataResponse.Error)
    }
}