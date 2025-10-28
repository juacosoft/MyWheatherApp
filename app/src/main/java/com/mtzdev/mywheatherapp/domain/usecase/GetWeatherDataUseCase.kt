package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository

class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) = repository.getWeatherData(lat, lon)
}