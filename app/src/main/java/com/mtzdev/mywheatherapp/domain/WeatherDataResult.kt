package com.mtzdev.mywheatherapp.domain

import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity

sealed class WeatherDataResult {
    data class Data(val data: WeatherDataEntity) : WeatherDataResult()
    data class Error(val error: WeatherException) : WeatherDataResult()
}