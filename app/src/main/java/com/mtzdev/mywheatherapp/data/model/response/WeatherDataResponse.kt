package com.mtzdev.mywheatherapp.data.model.response

sealed class WeatherDataResponse {
    data class Success(val data: WeatherDataModel): WeatherDataResponse()
    data class Error(val error: Exception): WeatherDataResponse()
}