package com.mtzdev.mywheatherapp

import com.mtzdev.mywheatherapp.domain.entity.LocalNamesEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherCloudsEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherCoordEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherMainEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherSysEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherWindEntity

fun getFakeWeatherGeoDataEntity() = WeatherGeoDataEntity(
    name = "London",
    localNames = LocalNamesEntity(
        es = "Londres",
        en = "London"
    ),
    lat = 0.0,
    lon = 0.0,
    country = "LON"
)

fun getFakeWeatherDataEntity() = WeatherDataEntity(
    coord = WeatherCoordEntity(lon = -0.1257, lat = 51.5085),
    weather = listOf(
        WeatherEntity(
            id = 801,
            main = "Clouds",
            description = "nubes dispersas",
            icon = "02d"
        )
    ),
    base = "stations",
    main = WeatherMainEntity(
        temp = 289.92,
        feelsLike = 289.39,
        tempMin = 288.71,
        tempMax = 290.93,
        pressure = 1012,
        humidity = 72,
        seaLevel = 1012,
        grndLevel = 1008
    ),
    visibility = 10000,
    wind = WeatherWindEntity(
        speed = 5.14,
        deg = 240,
        gust = 7.2
    ),
    clouds = WeatherCloudsEntity(all = 20),
    dt = 1661870592,
    sys = WeatherSysEntity(
        type = 2,
        id = 2075535,
        country = "GB",
        sunrise = 1661834992,
        sunset = 1661882570
    ),
    timezone = 3600,
    id = 2643743,
    name = "London",
    cod = 200
)