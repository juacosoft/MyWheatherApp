package com.mtzdev.mywheatherapp.commons

import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

class FactoryWeatherException {

    fun create(error: Exception): WeatherException {
        return when (error)  {
            is UnknownHostException,
            is ConnectException,
            is HttpRequestTimeoutException -> {
                WeatherConectionException("Error de conexión")
            }
            else -> {
                WeatherServiceException("Error de servicio")
            }
        }
    }
}