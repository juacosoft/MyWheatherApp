package com.mtzdev.mywheatherapp.data.remote

import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoResponse
import com.mtzdev.mywheatherapp.data.services.WeatherGeoService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WeatherGeoDataSourceRemoteTest {

    @MockK
    private lateinit var weatherGeoService: WeatherGeoService

    @InjectMockKs
    private lateinit var weatherGeoDataSourceRemote: WeatherGeoDataSourceRemote

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }


    @Test
    fun `WHEN getGeoData is called THEN result Success`() = runTest {
        val city = "London"
        coEvery { weatherGeoService.getGeoData(city) } returns mockk<WeatherGeoModel>(relaxed = true)

        val result = weatherGeoDataSourceRemote.getCurrentWeather(city)

        assert(result is WeatherGeoResponse.Success)
    }

    @Test
    fun `WHEN getGeoData is called THEN result Error`() = runTest {
        val city = "London"
        coEvery { weatherGeoService.getGeoData(city) } throws Exception("Error")

        val result = weatherGeoDataSourceRemote.getCurrentWeather(city)

        assert(result is WeatherGeoResponse.Error)
    }
}