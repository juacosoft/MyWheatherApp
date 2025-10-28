package com.mtzdev.mywheatherapp.domain.entity

import com.mtzdev.mywheatherapp.data.model.response.LocalNamesModel
import com.mtzdev.mywheatherapp.data.model.response.WeatherGeoModel

data class WeatherGeoDataEntity(
    val name: String? = null,
    val localNames : LocalNamesEntity?,
    val lat: Double,
    val lon: Double,
    val country: String? = null
)

data class LocalNamesEntity(
    val es: String? = null,
    val en: String? = null
)

fun WeatherGeoModel.toEntity() = WeatherGeoDataEntity(
    name = name,
    localNames = localNames?.toEntity(),
    lat = lat ?: 4.6534649,
    lon = lon ?: -74.0836453,
    country = country
)

fun LocalNamesModel.toEntity() = LocalNamesEntity(
    es = es,
    en = en
)