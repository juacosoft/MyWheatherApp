package com.mtzdev.mywheatherapp.domain.entity

import com.mtzdev.mywheatherapp.data.model.response.WeatherCloudsModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherCoordModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherDataModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherMainModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherSysModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherWindModel

data class WeatherDataEntity(
    val coord: WeatherCoordEntity?,
    val weather: List<WeatherEntity>,
    val base: String?,
    val main: WeatherMainEntity?,
    val visibility: Int?,
    val wind: WeatherWindEntity?,
    val clouds: WeatherCloudsEntity?,
    val dt: Int?,
    val sys: WeatherSysEntity?,
    val timezone: Int?,
    val id: Int?,
    val name: String?,
    val cod: Int?
)

data class WeatherCoordEntity(
    val lon: Double?,
    val lat: Double?
)

data class WeatherEntity(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?
)

data class WeatherMainEntity(
    val temp: Double?,
    val feelsLike: Double?,
    val tempMin: Double?,
    val tempMax: Double?,
    val pressure: Int?,
    val humidity: Int?,
    val seaLevel: Int?,
    val grndLevel: Int?
)

data class WeatherWindEntity(
    val speed: Double?,
    val deg: Int?,
    val gust: Double?
)

data class WeatherCloudsEntity(
    val all: Int?
)

data class WeatherSysEntity(
    val type: Int?,
    val id: Int?,
    val country: String?,
    val sunrise: Int?,
    val sunset: Int?
)

// Funciones de extensión para convertir Model a Entity

fun WeatherDataModel.toEntity(): WeatherDataEntity {
    return WeatherDataEntity(
        coord = this.coord?.toEntity(),
        weather = this.weather.map { it.toEntity() },
        base = this.base,
        main = this.main?.toEntity(),
        visibility = this.visibility,
        wind = this.wind?.toEntity(),
        clouds = this.clouds?.toEntity(),
        dt = this.dt,
        sys = this.sys?.toEntity(),
        timezone = this.timezone,
        id = this.id,
        name = this.name,
        cod = this.cod
    )
}

fun WeatherCoordModel.toEntity(): WeatherCoordEntity {
    return WeatherCoordEntity(
        lon = this.lon,
        lat = this.lat
    )
}

fun WeatherModel.toEntity(): WeatherEntity {
    return WeatherEntity(
        id = this.id,
        main = this.main,
        description = this.description,
        icon = this.icon
    )
}

fun WeatherMainModel.toEntity(): WeatherMainEntity {
    return WeatherMainEntity(
        temp = this.temp,
        feelsLike = this.feelsLike,
        tempMin = this.tempMin,
        tempMax = this.tempMax,
        pressure = this.pressure,
        humidity = this.humidity,
        seaLevel = this.seaLevel,
        grndLevel = this.grndLevel
    )
}

fun WeatherWindModel.toEntity(): WeatherWindEntity {
    return WeatherWindEntity(
        speed = this.speed,
        deg = this.deg,
        gust = this.gust
    )
}

fun WeatherCloudsModel.toEntity(): WeatherCloudsEntity {
    return WeatherCloudsEntity(
        all = this.all
    )
}

fun WeatherSysModel.toEntity(): WeatherSysEntity {
    return WeatherSysEntity(
        type = this.type,
        id = this.id,
        country = this.country,
        sunrise = this.sunrise,
        sunset = this.sunset
    )
}

