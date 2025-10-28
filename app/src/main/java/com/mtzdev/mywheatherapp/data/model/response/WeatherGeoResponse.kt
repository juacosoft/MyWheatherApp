package com.mtzdev.mywheatherapp.data.model.response

sealed class WeatherGeoResponse {
    data class Success(val data: WeatherGeoModel) : WeatherGeoResponse()
    data class Error(val error: Exception) : WeatherGeoResponse()
}

