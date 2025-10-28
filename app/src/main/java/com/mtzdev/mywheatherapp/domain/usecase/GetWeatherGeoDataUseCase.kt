package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository

class GetWeatherGeoDataUseCase(
    private val weatherGeoRepository: WeatherGeoRepository
) {

    suspend operator fun invoke(city: String) = weatherGeoRepository.getGeoData(city)
}