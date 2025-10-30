# API Contracts Documentation

**Project**: MyWeatherApp
**Feature**: Clima Actual con GPS
**Version**: 1.0.0
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Overview](#overview)
2. [Base Configuration](#base-configuration)
3. [Current Weather API](#current-weather-api)
4. [Geocoding API](#geocoding-api)
5. [Error Handling](#error-handling)
6. [Rate Limits](#rate-limits)
7. [Best Practices](#best-practices)

---

## Overview

This document defines the complete API contracts for OpenWeatherMap integration in the MyWeatherApp application. All APIs use RESTful HTTP requests with JSON responses.

### API Provider
- **Provider**: OpenWeatherMap
- **Documentation**: https://openweathermap.org/api
- **Version**: 2.5 (Weather) / 1.0 (Geocoding)
- **Protocol**: HTTPS only

### Authentication
- **Method**: API Key (Query Parameter)
- **Parameter Name**: `appid`
- **Key Storage**: BuildConfig / Environment Variable (NEVER in source code)

---

## Base Configuration

### Base URL
```
https://api.openweathermap.org
```

### Common Headers
```
Content-Type: application/json
Accept: application/json
```

### Client Configuration (Ktor)

```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/di/apiModule.kt
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        connectTimeoutMillis = 5_000
        socketTimeoutMillis = 10_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}

// Base URL Constant
const val WEATHER_BASE_URL = "https://api.openweathermap.org"
const val APP_ID_PARAM = "appid"
```

---

## Current Weather API

### Endpoint Details

**URL**: `/data/2.5/weather`
**Full URL**: `https://api.openweathermap.org/data/2.5/weather`
**Method**: `GET`
**Purpose**: Retrieve current weather data for specific coordinates

### Request Parameters

#### Required Parameters

| Parameter | Type | Description | Example | Validation |
|-----------|------|-------------|---------|------------|
| `lat` | Double | Latitude in decimal degrees | `40.4165` | -90 to 90 |
| `lon` | Double | Longitude in decimal degrees | `-3.7026` | -180 to 180 |
| `appid` | String | API Key for authentication | `your_api_key_here` | Non-empty |

#### Optional Parameters

| Parameter | Type | Description | Default | Options |
|-----------|------|-------------|---------|---------|
| `units` | String | Unit system for temperature | `standard` | `standard`, `metric`, `imperial` |
| `lang` | String | Language for weather descriptions | `en` | ISO 639-1 codes (e.g., `es`, `en`, `fr`) |

### Request Example

#### Using cURL
```bash
curl -X GET "https://api.openweathermap.org/data/2.5/weather?lat=40.4165&lon=-3.7026&appid=YOUR_API_KEY&units=metric&lang=es"
```

#### Using Ktor (Kotlin)
```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/data/services/WeatherDataService.kt
suspend fun getWeatherData(params: WeatherDataParams): WeatherDataModel {
    return client.get(WEATHER_ENDPOINT) {
        parameter(APP_ID_PARAM, apiKey)
        parameter(WEATHER_PARAM_LAT, params.lat)
        parameter(WEATHER_PARAM_LON, params.lon)
        parameter(WEATHER_PARAM_UNITS, WEATHER_PARAM_METRIC)
    }.body()
}
```

### Response Structure

#### Success Response (HTTP 200)

**Content-Type**: `application/json`

```json
{
  "coord": {
    "lon": -3.7026,
    "lat": 40.4165
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "cielo claro",
      "icon": "01d"
    }
  ],
  "base": "stations",
  "main": {
    "temp": 25.5,
    "feels_like": 24.8,
    "temp_min": 23.2,
    "temp_max": 27.1,
    "pressure": 1013,
    "humidity": 60,
    "sea_level": 1013,
    "grnd_level": 945
  },
  "visibility": 10000,
  "wind": {
    "speed": 3.5,
    "deg": 180,
    "gust": 5.2
  },
  "clouds": {
    "all": 0
  },
  "dt": 1698595200,
  "sys": {
    "type": 2,
    "id": 2007545,
    "country": "ES",
    "sunrise": 1698561120,
    "sunset": 1698599640
  },
  "timezone": 3600,
  "id": 3117735,
  "name": "Madrid",
  "cod": 200
}
```

#### Response Schema (Kotlin Data Classes)

```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/data/model/response/WeatherDataModel.kt

@Serializable
data class WeatherDataModel(
    @SerialName("coord") var coord: WeatherCoordModel? = null,
    @SerialName("weather") var weather: List<WeatherModel> = arrayListOf(),
    @SerialName("base") var base: String? = null,
    @SerialName("main") var main: WeatherMainModel? = null,
    @SerialName("visibility") var visibility: Int? = null,
    @SerialName("wind") var wind: WeatherWindModel? = null,
    @SerialName("clouds") var clouds: WeatherCloudsModel? = null,
    @SerialName("dt") var dt: Int? = null,
    @SerialName("sys") var sys: WeatherSysModel? = null,
    @SerialName("timezone") var timezone: Int? = null,
    @SerialName("id") var id: Int? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("cod") var cod: Int? = null
)

@Serializable
data class WeatherCoordModel(
    @SerialName("lon") var lon: Double? = null,
    @SerialName("lat") var lat: Double? = null
)

@Serializable
data class WeatherModel(
    @SerialName("id") var id: Int? = null,
    @SerialName("main") var main: String? = null,
    @SerialName("description") var description: String? = null,
    @SerialName("icon") var icon: String? = null
)

@Serializable
data class WeatherMainModel(
    @SerialName("temp") var temp: Double? = null,
    @SerialName("feels_like") var feelsLike: Double? = null,
    @SerialName("temp_min") var tempMin: Double? = null,
    @SerialName("temp_max") var tempMax: Double? = null,
    @SerialName("pressure") var pressure: Int? = null,
    @SerialName("humidity") var humidity: Int? = null,
    @SerialName("sea_level") var seaLevel: Int? = null,
    @SerialName("grnd_level") var grndLevel: Int? = null
)

@Serializable
data class WeatherWindModel(
    @SerialName("speed") var speed: Double? = null,
    @SerialName("deg") var deg: Int? = null,
    @SerialName("gust") var gust: Double? = null
)

@Serializable
data class WeatherCloudsModel(
    @SerialName("all") var all: Int? = null
)

@Serializable
data class WeatherSysModel(
    @SerialName("type") var type: Int? = null,
    @SerialName("id") var id: Int? = null,
    @SerialName("country") var country: String? = null,
    @SerialName("sunrise") var sunrise: Int? = null,
    @SerialName("sunset") var sunset: Int? = null
)
```

#### Field Descriptions

| Field Path | Type | Unit | Description | Nullable |
|------------|------|------|-------------|----------|
| `coord.lon` | Double | degrees | Longitude of the location | Yes |
| `coord.lat` | Double | degrees | Latitude of the location | Yes |
| `weather[].id` | Int | - | Weather condition ID | Yes |
| `weather[].main` | String | - | Group of weather parameters (Rain, Snow, Clear, etc.) | Yes |
| `weather[].description` | String | - | Weather condition description (localized) | Yes |
| `weather[].icon` | String | - | Weather icon ID (e.g., "01d") | Yes |
| `main.temp` | Double | °C (metric) | Current temperature | Yes |
| `main.feels_like` | Double | °C (metric) | Human perception of temperature | Yes |
| `main.temp_min` | Double | °C (metric) | Minimum temperature observed | Yes |
| `main.temp_max` | Double | °C (metric) | Maximum temperature observed | Yes |
| `main.pressure` | Int | hPa | Atmospheric pressure at sea level | Yes |
| `main.humidity` | Int | % | Humidity percentage | Yes |
| `main.sea_level` | Int | hPa | Atmospheric pressure at sea level | Yes |
| `main.grnd_level` | Int | hPa | Atmospheric pressure at ground level | Yes |
| `visibility` | Int | meters | Average visibility | Yes |
| `wind.speed` | Double | m/s (metric) | Wind speed | Yes |
| `wind.deg` | Int | degrees | Wind direction (meteorological) | Yes |
| `wind.gust` | Double | m/s (metric) | Wind gust speed | Yes |
| `clouds.all` | Int | % | Cloudiness percentage | Yes |
| `dt` | Int | unix timestamp | Time of data calculation | Yes |
| `sys.country` | String | ISO 3166 | Country code | Yes |
| `sys.sunrise` | Int | unix timestamp | Sunrise time | Yes |
| `sys.sunset` | Int | unix timestamp | Sunset time | Yes |
| `timezone` | Int | seconds | Shift in seconds from UTC | Yes |
| `name` | String | - | City name | Yes |
| `cod` | Int | - | HTTP status code | Yes |

### Status Codes

| Status Code | Meaning | Description | Recovery Action |
|-------------|---------|-------------|-----------------|
| `200` | OK | Successful request | Parse and use data |
| `400` | Bad Request | Invalid parameters (lat/lon out of range) | Validate input before request |
| `401` | Unauthorized | Invalid or missing API key | Check API key configuration |
| `404` | Not Found | Location not found for coordinates | Validate coordinates |
| `429` | Too Many Requests | Rate limit exceeded | Implement exponential backoff |
| `500` | Internal Server Error | Server error | Retry with backoff |
| `503` | Service Unavailable | Service temporarily down | Retry later |

### Error Response Example

```json
{
  "cod": 401,
  "message": "Invalid API key. Please see https://openweathermap.org/faq#error401 for more info."
}
```

---

## Geocoding API

### Endpoint Details

**URL**: `/geo/1.0/direct`
**Full URL**: `https://api.openweathermap.org/geo/1.0/direct`
**Method**: `GET`
**Purpose**: Convert city name to geographic coordinates

### Request Parameters

#### Required Parameters

| Parameter | Type | Description | Example | Validation |
|-----------|------|-------------|---------|------------|
| `q` | String | City name, state code, country code (comma-separated) | `Madrid,ES` | Non-empty, max 255 chars |
| `appid` | String | API Key for authentication | `your_api_key_here` | Non-empty |

#### Optional Parameters

| Parameter | Type | Description | Default | Range |
|-----------|------|-------------|---------|-------|
| `limit` | Int | Number of locations to return | `5` | 1-5 |

### Request Example

#### Using cURL
```bash
curl -X GET "https://api.openweathermap.org/geo/1.0/direct?q=Madrid,ES&appid=YOUR_API_KEY&limit=1"
```

#### Using Ktor (Kotlin)
```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/data/services/WeatherDataService.kt
suspend fun getGeoData(city: String): List<WeatherGeoModel> {
    return client.get(GEO_ENDPOINT) {
        parameter(APP_ID_PARAM, apiKey)
        parameter(WEATHER_PARAM_CITY, city)
    }.body()
}
```

### Response Structure

#### Success Response (HTTP 200)

**Content-Type**: `application/json`

```json
[
  {
    "name": "Madrid",
    "local_names": {
      "es": "Madrid",
      "en": "Madrid",
      "fr": "Madrid",
      "de": "Madrid"
    },
    "lat": 40.4165,
    "lon": -3.7026,
    "country": "ES"
  }
]
```

#### Response Schema (Kotlin Data Classes)

```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/data/model/response/WeatherGeoModel.kt

@Serializable
data class WeatherGeoModel(
    @SerialName("name") val name: String? = null,
    @SerialName("local_names") val localNames: LocalNamesModel? = null,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lon") val lon: Double? = null,
    @SerialName("country") val country: String? = null
)

@Serializable
data class LocalNamesModel(
    @SerialName("es") val es: String? = null,
    @SerialName("en") val en: String? = null
)
```

#### Field Descriptions

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `name` | String | City name in English | Yes |
| `local_names` | Object | City names in different languages | Yes |
| `local_names.es` | String | City name in Spanish | Yes |
| `local_names.en` | String | City name in English | Yes |
| `lat` | Double | Latitude in decimal degrees | Yes |
| `lon` | Double | Longitude in decimal degrees | Yes |
| `country` | String | Country code (ISO 3166-1 alpha-2) | Yes |

### Status Codes

| Status Code | Meaning | Description | Recovery Action |
|-------------|---------|-------------|-----------------|
| `200` | OK | Successful request | Parse results (may be empty array) |
| `400` | Bad Request | Invalid query format | Validate city name format |
| `401` | Unauthorized | Invalid or missing API key | Check API key configuration |
| `404` | Not Found | No locations found | Show "City not found" message |
| `429` | Too Many Requests | Rate limit exceeded | Implement rate limiting |
| `500` | Internal Server Error | Server error | Retry with backoff |

### Empty Results Example

When no location is found, the API returns an empty array:

```json
[]
```

**Handling**: Check if array is empty before accessing elements

```kotlin
val geoResults = weatherService.getGeoData("NonExistentCity")
if (geoResults.isEmpty()) {
    throw WeatherUnknowGeoDataException("City not found")
}
```

---

## Error Handling

### Network Error Mapping

The application uses a factory pattern to map network exceptions to domain exceptions:

```kotlin
// Location: app/src/main/java/com/mtzdev/mywheatherapp/commons/FactoryWeatherException.kt

class FactoryWeatherException {
    fun create(error: Exception): WeatherException {
        return when (error) {
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
```

### Error Categories

| Network Exception | Domain Exception | User Message | HTTP Status |
|-------------------|------------------|--------------|-------------|
| `UnknownHostException` | `WeatherConectionException` | "Error de conexión. Verifica tu internet" | N/A |
| `ConnectException` | `WeatherConectionException` | "Error de conexión. Verifica tu internet" | N/A |
| `HttpRequestTimeoutException` | `WeatherConectionException` | "La solicitud tardó demasiado. Intenta nuevamente" | 408 |
| `ClientRequestException` (4xx) | `WeatherServiceException` | "Error en la solicitud" | 400-499 |
| `ServerResponseException` (5xx) | `WeatherServiceException` | "El servicio no está disponible" | 500-599 |
| Empty geocoding results | `WeatherUnknowGeoDataException` | "Ciudad no encontrada" | 200 (empty) |
| Other exceptions | `WeatherUnknowException` | "Ocurrió un error inesperado" | N/A |

### Retry Strategy

```kotlin
// Recommended retry logic for transient errors
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxRetries - 1) {
        try {
            return block()
        } catch (e: Exception) {
            if (e is WeatherConectionException) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            } else {
                throw e
            }
        }
    }
    return block() // Last attempt
}
```

---

## Rate Limits

### Free Tier Limitations

| Metric | Limit | Period |
|--------|-------|--------|
| API Calls | 60 | Per minute |
| API Calls | 1,000,000 | Per month |

### Rate Limit Headers

OpenWeatherMap does not provide rate limit headers in responses. Implement client-side rate limiting:

```kotlin
class RateLimiter(
    private val maxRequests: Int = 60,
    private val timeWindowMs: Long = 60_000
) {
    private val timestamps = mutableListOf<Long>()

    suspend fun <T> execute(block: suspend () -> T): T {
        cleanOldTimestamps()

        if (timestamps.size >= maxRequests) {
            val oldestTimestamp = timestamps.first()
            val waitTime = timeWindowMs - (System.currentTimeMillis() - oldestTimestamp)
            if (waitTime > 0) {
                delay(waitTime)
            }
        }

        timestamps.add(System.currentTimeMillis())
        return block()
    }

    private fun cleanOldTimestamps() {
        val now = System.currentTimeMillis()
        timestamps.removeAll { now - it > timeWindowMs }
    }
}
```

### HTTP 429 Handling

When receiving a 429 status code:

```kotlin
try {
    val response = client.get(endpoint)
    // Process response
} catch (e: ClientRequestException) {
    if (e.response.status.value == 429) {
        // Wait 60 seconds before retry
        delay(60_000)
        // Retry request
    }
}
```

---

## Best Practices

### 1. API Key Security

**DO**:
- Store API key in `local.properties` or environment variables
- Access via BuildConfig
- Never commit API keys to version control

**DON'T**:
- Hardcode API keys in source code
- Expose API keys in logs
- Share API keys in public repositories

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "WEATHER_API_KEY", "\"${project.findProperty("WEATHER_API_KEY")}\"")
    }
}

// Usage
val apiKey = BuildConfig.WEATHER_API_KEY
```

### 2. Request Optimization

**Caching**:
- Cache weather data for 10 minutes (weather doesn't change frequently)
- Cache geocoding results indefinitely (coordinates don't change)

**Batching**:
- Avoid multiple requests for the same location within short time periods
- Debounce search input (wait 500ms after user stops typing)

### 3. Error Handling

**Graceful Degradation**:
- Show cached data if network fails
- Provide manual retry option
- Show meaningful error messages to users

**Logging**:
```kotlin
try {
    val weather = weatherService.getWeatherData(params)
} catch (e: Exception) {
    Log.e("WeatherAPI", "Failed to fetch weather", e)
    // Report to analytics/crash reporting
    throw factoryWeatherException.create(e)
}
```

### 4. Request Validation

**Before API Call**:
```kotlin
fun validateCoordinates(lat: Double, lon: Double): Boolean {
    return lat in -90.0..90.0 && lon in -180.0..180.0
}

fun validateCityName(city: String): Boolean {
    return city.isNotBlank() && city.length <= 255
}
```

### 5. Timeouts

Configure appropriate timeouts to prevent hanging requests:

```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 10_000  // Total request timeout
    connectTimeoutMillis = 5_000   // Connection establishment timeout
    socketTimeoutMillis = 10_000   // Socket read timeout
}
```

### 6. Unit Conversion

When using `units=metric`:
- Temperature: Celsius (°C)
- Wind Speed: meters/second (m/s)
- Pressure: hectoPascal (hPa)

Convert wind speed to km/h for display:
```kotlin
fun Double.metersPerSecondToKmPerHour(): Double = this * 3.6
```

### 7. Icon URLs

Weather icons are available at:
```
https://openweathermap.org/img/wn/{icon}@2x.png
```

Example:
```kotlin
fun getWeatherIconUrl(iconCode: String): String {
    return "https://openweathermap.org/img/wn/${iconCode}@2x.png"
}
```

### 8. Language Support

Supported language codes for `lang` parameter:
- `es` - Spanish
- `en` - English
- `fr` - French
- `de` - German
- `it` - Italian
- `pt` - Portuguese

---

## Testing API Contracts

### Unit Test Example

```kotlin
@Test
fun `getWeatherData returns valid response for valid coordinates`() = runTest {
    // Given
    val mockClient = mockHttpClient(
        responseContent = """
            {
              "main": {"temp": 25.5},
              "weather": [{"description": "Clear"}],
              "name": "Madrid"
            }
        """.trimIndent()
    )
    val service = WeatherDataService(mockClient, "test_api_key")
    val params = WeatherDataParams(lat = 40.4165, lon = -3.7026)

    // When
    val result = service.getWeatherData(params)

    // Then
    assertEquals(25.5, result.main?.temp)
    assertEquals("Madrid", result.name)
}

@Test
fun `getGeoData returns empty list for unknown city`() = runTest {
    // Given
    val mockClient = mockHttpClient(responseContent = "[]")
    val service = WeatherDataService(mockClient, "test_api_key")

    // When
    val result = service.getGeoData("NonExistentCity12345")

    // Then
    assertTrue(result.isEmpty())
}
```

### Integration Test Checklist

- [ ] Valid coordinates return weather data
- [ ] Invalid coordinates return 400 error
- [ ] Invalid API key returns 401 error
- [ ] Valid city name returns geocoding results
- [ ] Unknown city returns empty array
- [ ] Network timeout triggers timeout exception
- [ ] Rate limit (429) is handled gracefully
- [ ] Null/missing fields in response don't crash parser

---

## Appendix

### Complete Request/Response Examples

#### Example 1: Weather Request for Madrid

**Request**:
```bash
curl -X GET \
  "https://api.openweathermap.org/data/2.5/weather?lat=40.4165&lon=-3.7026&appid=YOUR_API_KEY&units=metric&lang=es" \
  -H "Accept: application/json"
```

**Response** (200 OK):
```json
{
  "coord": {"lon": -3.7026, "lat": 40.4165},
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "cielo claro",
      "icon": "01d"
    }
  ],
  "main": {
    "temp": 25.5,
    "feels_like": 24.8,
    "temp_min": 23.2,
    "temp_max": 27.1,
    "pressure": 1013,
    "humidity": 60
  },
  "wind": {"speed": 3.5, "deg": 180},
  "clouds": {"all": 0},
  "sys": {"country": "ES", "sunrise": 1698561120, "sunset": 1698599640},
  "name": "Madrid",
  "cod": 200
}
```

#### Example 2: Geocoding Request

**Request**:
```bash
curl -X GET \
  "https://api.openweathermap.org/geo/1.0/direct?q=Barcelona,ES&appid=YOUR_API_KEY&limit=1" \
  -H "Accept: application/json"
```

**Response** (200 OK):
```json
[
  {
    "name": "Barcelona",
    "local_names": {
      "es": "Barcelona",
      "en": "Barcelona",
      "ca": "Barcelona"
    },
    "lat": 41.3828939,
    "lon": 2.1774322,
    "country": "ES"
  }
]
```

### Weather Condition IDs Reference

| ID Range | Group | Description |
|----------|-------|-------------|
| 200-232 | Thunderstorm | Thunderstorm with varying intensities |
| 300-321 | Drizzle | Light precipitation |
| 500-531 | Rain | Rain with varying intensities |
| 600-622 | Snow | Snow and sleet |
| 701-781 | Atmosphere | Fog, mist, haze, dust, etc. |
| 800 | Clear | Clear sky |
| 801-804 | Clouds | Few to overcast clouds |

### Icon Code Reference

| Icon Code | Day/Night | Condition |
|-----------|-----------|-----------|
| `01d/01n` | Day/Night | Clear sky |
| `02d/02n` | Day/Night | Few clouds |
| `03d/03n` | Day/Night | Scattered clouds |
| `04d/04n` | Day/Night | Broken clouds |
| `09d/09n` | Day/Night | Shower rain |
| `10d/10n` | Day/Night | Rain |
| `11d/11n` | Day/Night | Thunderstorm |
| `13d/13n` | Day/Night | Snow |
| `50d/50n` | Day/Night | Mist |

---

**Document Status**: Complete
**Reviewed By**: Development Team
**Next Review**: 2025-11-29
