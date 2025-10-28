package com.mtzdev.mywheatherapp.data.remote.repository

import com.mtzdev.mywheatherapp.commons.WeatherConectionException
import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataResponse
import com.mtzdev.mywheatherapp.data.repository.WeatherDataRepositoryData
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

class WeatherDataRepositoryDataTest {

    @MockK
    private lateinit var dataSource: WeatherDataDataSource

    @InjectMockKs
    private lateinit var repository: WeatherDataRepositoryData

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `GIVEN result is success WHEN getWeatherData is called THEN result is Data`() = runTest {
        val params =  WeatherDataParams(
            lat = 1.0,
            lon = 2.0
        )
        coEvery { dataSource.getWeatherData(params) } returns WeatherDataResponse.Success(mockk(relaxed = true))

        val result = repository.getWeatherData(1.0, 2.0)

        val data = (result as? WeatherDataResult.Data)?.data
        assert(data != null)
    }

    @Test
    fun `GIVEN result is UnknownHostException WHEN getWeatherData is called THEN result Error with WeatherConectionException`() = runTest{
        val params =  WeatherDataParams(
            lat = 1.0,
            lon = 2.0
        )
        coEvery { dataSource.getWeatherData(params) } returns WeatherDataResponse.Error(
            UnknownHostException("some error")
        )
        val result = repository.getWeatherData(1.0, 2.0)

        val error = (result as? WeatherDataResult.Error)?.error
        assert(error != null)
        val errorType = error as? WeatherConectionException
        assert(errorType != null)
    }
}