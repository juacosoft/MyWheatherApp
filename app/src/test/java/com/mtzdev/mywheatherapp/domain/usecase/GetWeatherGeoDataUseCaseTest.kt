package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class GetWeatherGeoDataUseCaseTest {

    @MockK
    private lateinit var weatherGeoRepository: WeatherGeoRepository

    @InjectMockKs
    private lateinit var useCase: GetWeatherGeoDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `WHEN invoke is called THEN repository getGeoData is called`() = runTest {
        coEvery { weatherGeoRepository.getGeoData("London") } returns mockk(relaxed = true)

        val result = useCase.invoke("London")

        coVerify { weatherGeoRepository.getGeoData("London") }
        assertNotNull(result)
    }
}