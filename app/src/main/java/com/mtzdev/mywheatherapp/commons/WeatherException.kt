package com.mtzdev.mywheatherapp.commons

open class WeatherException(message: String): Exception(message)

class WeatherServiceException(message: String): WeatherException(message)
class WeatherConectionException(message: String): WeatherException(message)
class WeatherUnknowException(message: String): WeatherException(message)