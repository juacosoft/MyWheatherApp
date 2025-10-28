package com.mtzdev.mywheatherapp.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherGeoModel(
    @SerialName("name") val name: String? = null,
    @SerialName("local_names") val localNames : LocalNamesModel?,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lon") val lon: Double? = null,
    @SerialName("country") val country: String? = null
)

@Serializable
data class LocalNamesModel(
    @SerialName("es") val es: String? = null,
    @SerialName("en") val en: String? = null
)
