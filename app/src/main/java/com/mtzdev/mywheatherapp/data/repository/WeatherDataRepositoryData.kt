package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.commons.FactoryWeatherException
import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.data.model.request.WeatherDataParams
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataResponse
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.entity.toEntity
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository

class WeatherDataRepositoryData(
    private val dataSource: WeatherDataDataSource
): WeatherDataRepository {
    override suspend fun getWeatherData(
        lat: Double,
        lon: Double
    ): WeatherDataResult {
        val params= WeatherDataParams(lat, lon)
        val result = dataSource.getWeatherData(params)
        return when(result){
            is WeatherDataResponse.Error -> {
                val error = FactoryWeatherException().create(result.error)
                WeatherDataResult.Error(error)
            }
            is WeatherDataResponse.Success -> WeatherDataResult.Data(result.data.toEntity())
        }
    }
}