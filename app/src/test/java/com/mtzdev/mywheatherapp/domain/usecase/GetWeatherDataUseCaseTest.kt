package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository
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

class GetWeatherDataUseCaseTest {

    @MockK
    private lateinit var repository: WeatherDataRepository

    @InjectMockKs
    private lateinit var useCase: GetWeatherDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `WHEN invoke is called THEN repository getWeatherData is called`() = runTest {
        coEvery { repository.getWeatherData(0.0,0.0) } returns mockk(relaxed = true)

        val result = useCase.invoke(0.0,0.0)

        coVerify { repository.getWeatherData(0.0,0.0) }
        assertNotNull(result)
    }
}