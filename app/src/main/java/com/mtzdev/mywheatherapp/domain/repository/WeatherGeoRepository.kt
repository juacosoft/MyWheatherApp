package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.WeatherGeoResult

interface WeatherGeoRepository {

    suspend fun getGeoData(city: String): WeatherGeoResult
}