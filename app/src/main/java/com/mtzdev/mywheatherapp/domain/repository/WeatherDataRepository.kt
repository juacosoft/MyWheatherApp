package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.WeatherDataResult

interface WeatherDataRepository {

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult
}