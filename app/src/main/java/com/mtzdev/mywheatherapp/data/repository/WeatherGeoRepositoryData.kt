package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.commons.FactoryWeatherException
import com.mtzdev.mywheatherapp.data.WeatherGeoDataSource
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoResponse
import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.entity.toEntity
import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository

class WeatherGeoRepositoryData(
    private val weatherGeoDataSource: WeatherGeoDataSource
): WeatherGeoRepository {
    override suspend fun getGeoData(city: String): WeatherGeoResult {
        val result =  weatherGeoDataSource.getCurrentWeather(city)
        return when(result){
            is WeatherGeoResponse.Error -> {
                val error = FactoryWeatherException().create( result.error)
                WeatherGeoResult.Error(error)
            }
            is WeatherGeoResponse.Success -> WeatherGeoResult.Data(result.data.toEntity())
        }
    }
}