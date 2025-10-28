package com.mtzdev.mywheatherapp.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDataModel (
    @SerialName("coord"      ) var coord      : WeatherCoordModel? = null,
    @SerialName("weather"    ) var weather    : List<WeatherModel> = arrayListOf(),
    @SerialName("base"       ) var base       : String?            = null,
    @SerialName("main"       ) var main       : WeatherMainModel?  = null,
    @SerialName("visibility" ) var visibility : Int?               = null,
    @SerialName("wind"       ) var wind       : WeatherWindModel?  = null,
    @SerialName("clouds"     ) var clouds     : WeatherCloudsModel?= null,
    @SerialName("dt"         ) var dt         : Int?               = null,
    @SerialName("sys"        ) var sys        : WeatherSysModel?   = null,
    @SerialName("timezone"   ) var timezone   : Int?               = null,
    @SerialName("id"         ) var id         : Int?               = null,
    @SerialName("name"       ) var name       : String?            = null,
    @SerialName("cod"        ) var cod        : Int?               = null
)

@Serializable
data class WeatherCoordModel (
    @SerialName("lon" ) var lon : Double? = null,
    @SerialName("lat" ) var lat : Double? = null
)

@Serializable
data class WeatherModel (
    @SerialName("id"          ) var id          : Int?    = null,
    @SerialName("main"        ) var main        : String? = null,
    @SerialName("description" ) var description : String? = null,
    @SerialName("icon"        ) var icon        : String? = null
)

@Serializable
data class WeatherMainModel (
    @SerialName("temp"       ) var temp      : Double? = null,
    @SerialName("feels_like" ) var feelsLike : Double? = null,
    @SerialName("temp_min"   ) var tempMin   : Double? = null,
    @SerialName("temp_max"   ) var tempMax   : Double? = null,
    @SerialName("pressure"   ) var pressure  : Int?    = null,
    @SerialName("humidity"   ) var humidity  : Int?    = null,
    @SerialName("sea_level"  ) var seaLevel  : Int?    = null,
    @SerialName("grnd_level" ) var grndLevel : Int?    = null
)

@Serializable
data class WeatherWindModel (
    @SerialName("speed" ) var speed : Double? = null,
    @SerialName("deg"   ) var deg   : Int?    = null,
    @SerialName("gust"  ) var gust  : Double? = null
)


@Serializable
data class WeatherCloudsModel (
    @SerialName("all" ) var all : Int? = null
)

@Serializable
data class WeatherSysModel (

    @SerialName("type"    ) var type    : Int?    = null,
    @SerialName("id"      ) var id      : Int?    = null,
    @SerialName("country" ) var country : String? = null,
    @SerialName("sunrise" ) var sunrise : Int?    = null,
    @SerialName("sunset"  ) var sunset  : Int?    = null

)