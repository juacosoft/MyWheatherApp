package com.mtzdev.mywheatherapp.domain

import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

sealed class WeatherGeoResult {
    data class Data(val data: WeatherGeoDataEntity) : WeatherGeoResult()
    data class Error(val error: WeatherException) : WeatherGeoResult()
}

