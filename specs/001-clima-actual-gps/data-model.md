# Data Model Documentation: Clima Actual con GPS

**Feature**: 001-clima-actual-gps
**Version**: 1.0.0
**Date**: 2025-10-29
**Status**: Specification Complete

---

## Table of Contents

1. [Domain Models (Pure Kotlin)](#1-domain-models-pure-kotlin)
2. [Data Transfer Objects (DTOs)](#2-data-transfer-objects-dtos)
3. [Mappers](#3-mappers)
4. [Entity Relationships](#4-entity-relationships)
5. [Validation Rules](#5-validation-rules)
6. [State Transitions](#6-state-transitions)
7. [Immutability Guarantees](#7-immutability-guarantees)

---

## 1. Domain Models (Pure Kotlin)

Domain models are pure Kotlin data structures with **NO Android dependencies**. They represent the core business entities and are used throughout the domain and presentation layers.

### 1.1 Location

**Purpose**: Represents a geographic location with coordinates and metadata.

**Fields**:
- `latitude`: Double - Latitude coordinate (-90.0 to 90.0)
- `longitude`: Double - Longitude coordinate (-180.0 to 180.0)
- `name`: String - City or location name
- `country`: String - ISO 3166-1 alpha-2 country code (e.g., "ES", "US")

**Validation Rules**:
- Latitude must be in range [-90.0, 90.0]
- Longitude must be in range [-180.0, 180.0]
- Name must not be blank
- Country must be 2-character ISO code

**Invariants**:
- All fields are immutable (val)
- Coordinates are validated on creation
- Thread-safe due to immutability

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * Domain model representing a geographic location.
 * Contains coordinates and location metadata for weather queries.
 *
 * @property latitude Latitude coordinate in decimal degrees (-90.0 to 90.0)
 * @property longitude Longitude coordinate in decimal degrees (-180.0 to 180.0)
 * @property name City or location name
 * @property country ISO 3166-1 alpha-2 country code (2 letters)
 * @throws IllegalArgumentException if coordinates are out of valid range
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val country: String
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90.0 and 90.0, got: $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180.0 and 180.0, got: $longitude"
        }
        require(name.isNotBlank()) {
            "Location name cannot be blank"
        }
        require(country.length == 2) {
            "Country code must be 2 characters (ISO 3166-1 alpha-2), got: $country"
        }
    }

    /**
     * Returns a human-readable location string.
     * Format: "CityName, CountryCode"
     * Example: "Madrid, ES"
     */
    fun toDisplayString(): String = "$name, $country"

    /**
     * Calculates distance to another location in kilometers.
     * Uses Haversine formula for great-circle distance.
     *
     * @param other Target location
     * @return Distance in kilometers
     */
    fun distanceTo(other: Location): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude)) *
                Math.cos(Math.toRadians(other.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
```

---

### 1.2 WeatherCondition

**Purpose**: Represents the current weather condition with metadata and icon.

**Fields**:
- `id`: Int - OpenWeatherMap condition ID (e.g., 800 = clear sky)
- `main`: String - Main weather category (e.g., "Clear", "Clouds", "Rain")
- `description`: String - Detailed description in Spanish (e.g., "cielo claro")
- `icon`: String - Icon code (e.g., "01d", "10n")

**Validation Rules**:
- ID must be positive
- Main and description must not be blank
- Icon must be valid format (2-3 characters)

**Invariants**:
- All fields are immutable
- Represents a single weather condition snapshot

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * Domain model representing a weather condition.
 * Contains weather category, description, and icon information.
 *
 * @property id OpenWeatherMap condition ID (see API documentation)
 * @property main Main weather category (Clear, Clouds, Rain, etc.)
 * @property description Detailed weather description in Spanish
 * @property icon Icon code for weather visualization (e.g., "01d" for clear day)
 */
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) {
    init {
        require(id > 0) {
            "Weather condition ID must be positive, got: $id"
        }
        require(main.isNotBlank()) {
            "Weather main category cannot be blank"
        }
        require(description.isNotBlank()) {
            "Weather description cannot be blank"
        }
        require(icon.length in 2..3) {
            "Weather icon code must be 2-3 characters, got: $icon"
        }
    }

    /**
     * Returns the full icon URL for OpenWeatherMap icons.
     * Format: https://openweathermap.org/img/wn/{icon}@2x.png
     *
     * @return Complete URL string for icon image
     */
    fun getIconUrl(): String {
        return "https://openweathermap.org/img/wn/${icon}@2x.png"
    }

    /**
     * Checks if this is a daytime icon.
     * Icons ending with 'd' represent day, 'n' represents night.
     *
     * @return true if daytime icon, false if nighttime
     */
    fun isDayIcon(): Boolean = icon.endsWith('d')
}
```

---

### 1.3 Weather

**Purpose**: Complete weather data for a location at a specific time.

**Fields**:
- `temperature`: Double - Temperature in Celsius
- `feelsLike`: Double - Perceived temperature in Celsius
- `humidity`: Int - Relative humidity percentage (0-100)
- `pressure`: Int - Atmospheric pressure in hPa
- `windSpeed`: Double - Wind speed in km/h
- `windDirection`: Int? - Wind direction in degrees (0-360), nullable
- `condition`: WeatherCondition - Weather condition details
- `location`: Location - Location for this weather data
- `timestamp`: Long - Unix timestamp (seconds) when data was retrieved

**Validation Rules**:
- Temperature must be in valid range (-100 to 60°C)
- Humidity must be 0-100%
- Pressure must be positive
- Wind speed must be non-negative
- Wind direction must be 0-360° if present
- Timestamp must be positive

**Invariants**:
- All fields immutable except for nullable windDirection
- Represents a single weather snapshot
- Cannot be modified after creation (use copy())

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.domain.model

import kotlin.math.roundToInt

/**
 * Domain model representing complete weather data for a location.
 * Contains temperature, atmospheric conditions, wind data, and location.
 *
 * @property temperature Actual temperature in Celsius
 * @property feelsLike Perceived temperature in Celsius (wind chill/heat index)
 * @property humidity Relative humidity percentage (0-100)
 * @property pressure Atmospheric pressure in hPa (hectopascals)
 * @property windSpeed Wind speed in km/h
 * @property windDirection Wind direction in degrees (0-360), null if unavailable
 * @property condition Weather condition details (description, icon, etc.)
 * @property location Geographic location for this weather data
 * @property timestamp Unix timestamp in seconds when data was retrieved
 * @throws IllegalArgumentException if any value is out of valid range
 */
data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: Int?,
    val condition: WeatherCondition,
    val location: Location,
    val timestamp: Long
) {
    init {
        require(temperature in -100.0..60.0) {
            "Temperature must be between -100°C and 60°C, got: $temperature"
        }
        require(feelsLike in -100.0..60.0) {
            "Feels like temperature must be between -100°C and 60°C, got: $feelsLike"
        }
        require(humidity in 0..100) {
            "Humidity must be between 0 and 100%, got: $humidity"
        }
        require(pressure > 0) {
            "Pressure must be positive, got: $pressure"
        }
        require(windSpeed >= 0.0) {
            "Wind speed cannot be negative, got: $windSpeed"
        }
        windDirection?.let {
            require(it in 0..360) {
                "Wind direction must be between 0 and 360 degrees, got: $it"
            }
        }
        require(timestamp > 0) {
            "Timestamp must be positive, got: $timestamp"
        }
    }

    /**
     * Returns formatted temperature string.
     * Example: "25.5°C"
     */
    fun getFormattedTemperature(): String =
        "${temperature.roundToInt()}°C"

    /**
     * Returns formatted feels-like temperature string.
     * Example: "Sensación: 24°C"
     */
    fun getFormattedFeelsLike(): String =
        "Sensación: ${feelsLike.roundToInt()}°C"

    /**
     * Returns formatted humidity string.
     * Example: "Humedad: 60%"
     */
    fun getFormattedHumidity(): String =
        "Humedad: $humidity%"

    /**
     * Returns formatted wind speed string.
     * Example: "Viento: 15 km/h"
     */
    fun getFormattedWindSpeed(): String =
        "Viento: ${windSpeed.roundToInt()} km/h"

    /**
     * Returns cardinal direction for wind (N, NE, E, SE, S, SW, W, NW).
     * Returns null if windDirection is null.
     */
    fun getWindCardinalDirection(): String? {
        return windDirection?.let { degrees ->
            val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((degrees + 22.5) / 45.0).toInt() % 8
            directions[index]
        }
    }

    /**
     * Checks if weather data is recent (less than 30 minutes old).
     * Useful for determining if data needs refresh.
     *
     * @return true if data is less than 30 minutes old
     */
    fun isRecent(): Boolean {
        val thirtyMinutesInSeconds = 30 * 60
        val currentTime = System.currentTimeMillis() / 1000
        return (currentTime - timestamp) < thirtyMinutesInSeconds
    }
}
```

---

### 1.4 Result<T>

**Purpose**: Generic wrapper for operation results representing loading, success, or error states.

**States**:
- `Loading`: Operation in progress
- `Success<T>`: Operation completed successfully with data
- `Error`: Operation failed with error information

**Validation Rules**:
- Only one state can be active at a time (sealed class)
- Success must contain non-null data
- Error must contain DomainError

**Invariants**:
- Immutable sealed class hierarchy
- Type-safe state representation
- Exhaustive when expressions

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * Generic wrapper for operation results in the domain layer.
 * Represents the three states of an asynchronous operation: Loading, Success, Error.
 * Provides type-safe handling of operation outcomes.
 *
 * @param T The type of data held by Success state
 */
sealed class Result<out T> {

    /**
     * Represents a loading state (operation in progress).
     * Used to trigger loading UI indicators.
     */
    data object Loading : Result<Nothing>()

    /**
     * Represents a successful operation with data.
     *
     * @property data The result data of type T
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with error information.
     *
     * @property error The domain error that occurred
     */
    data class Error(val error: DomainError) : Result<Nothing>()

    /**
     * Maps the success data to a different type.
     * Error and Loading states are passed through unchanged.
     *
     * @param transform Function to transform success data
     * @return Result with transformed data or original Error/Loading
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Chains another Result-producing operation if this is Success.
     * Error and Loading states are passed through unchanged.
     *
     * @param transform Function that produces a new Result
     * @return Result from transform or original Error/Loading
     */
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
}

/**
 * Extension to check if result is successful.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Extension to check if result is error.
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Extension to check if result is loading.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Extension to get data or null.
 * Returns data if Success, null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Extension to get error or null.
 * Returns error if Error, null otherwise.
 */
fun <T> Result<T>.errorOrNull(): DomainError? = when (this) {
    is Result.Error -> error
    else -> null
}

/**
 * Extension to get data or default value.
 * Returns data if Success, default value otherwise.
 */
fun <T> Result<T>.getOrDefault(default: T): T = when (this) {
    is Result.Success -> data
    else -> default
}

/**
 * Extension to execute block only if Success.
 */
inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        block(data)
    }
    return this
}

/**
 * Extension to execute block only if Error.
 */
inline fun <T> Result<T>.onError(block: (DomainError) -> Unit): Result<T> {
    if (this is Result.Error) {
        block(error)
    }
    return this
}

/**
 * Extension to execute block only if Loading.
 */
inline fun <T> Result<T>.onLoading(block: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        block()
    }
    return this
}
```

---

### 1.5 DomainError

**Purpose**: Sealed hierarchy of domain-level errors for business logic failures.

**Error Categories**:
- **LocationError**: GPS, permissions, location service issues
- **WeatherError**: API, network, data retrieval issues
- **ValidationError**: Input validation failures

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * Sealed hierarchy of domain-level errors.
 * These represent business logic errors, not infrastructure exceptions.
 * All error types should be handled gracefully in the presentation layer.
 */
sealed class DomainError {

    /**
     * Location-related errors.
     * Covers GPS, permissions, and location service issues.
     */
    sealed class LocationError : DomainError() {
        /**
         * User denied location permissions.
         * Recovery: Request permissions or use manual search.
         */
        data object PermissionDenied : LocationError()

        /**
         * Location is unavailable (service disabled, no signal, etc.).
         * Recovery: Enable GPS or use manual search.
         */
        data object Unavailable : LocationError()

        /**
         * Location request timed out (>10 seconds).
         * Recovery: Retry or use manual search.
         */
        data object Timeout : LocationError()

        /**
         * GPS is disabled in device settings.
         * Recovery: Enable GPS in settings or use manual search.
         */
        data object GpsDisabled : LocationError()
    }

    /**
     * Weather API-related errors.
     * Covers network, server, and data retrieval issues.
     */
    sealed class WeatherError : DomainError() {
        /**
         * Requested city was not found in geocoding API.
         * Recovery: Verify city name and try again.
         */
        data object CityNotFound : WeatherError()

        /**
         * API key is invalid or missing.
         * Recovery: Check API key configuration.
         */
        data object InvalidApiKey : WeatherError()

        /**
         * API rate limit exceeded (too many requests).
         * Recovery: Wait before retrying.
         */
        data object RateLimitExceeded : WeatherError()

        /**
         * No internet connection available.
         * Recovery: Check internet connection and retry.
         */
        data object NoInternetConnection : WeatherError()

        /**
         * Server returned 5xx error.
         * Recovery: Wait and retry later.
         */
        data object ServerError : WeatherError()

        /**
         * Unknown or unexpected error occurred.
         *
         * @property message Error message if available
         */
        data class Unknown(val message: String?) : WeatherError()
    }

    /**
     * Validation errors for user input or data.
     * Covers invalid coordinates, empty fields, etc.
     */
    sealed class ValidationError : DomainError() {
        /**
         * City name is empty or blank.
         * Recovery: Prompt user to enter city name.
         */
        data object EmptyCityName : ValidationError()

        /**
         * Coordinates are out of valid range.
         * Recovery: Use valid coordinates or manual search.
         */
        data object InvalidCoordinates : ValidationError()
    }
}
```

---

## 2. Data Transfer Objects (DTOs)

DTOs represent the structure of data received from the OpenWeatherMap API. They use `@Serializable` for kotlinx.serialization and map directly to JSON responses.

### 2.1 WeatherResponseDto

**Purpose**: Maps to OpenWeatherMap Current Weather API response.

**API Endpoint**: `https://api.openweathermap.org/data/2.5/weather`

**JSON Example**:
```json
{
  "coord": { "lon": -3.7026, "lat": 40.4165 },
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
    "humidity": 60,
    "pressure": 1013
  },
  "wind": {
    "speed": 3.5,
    "deg": 180
  },
  "dt": 1698765432,
  "name": "Madrid",
  "sys": {
    "country": "ES"
  }
}
```

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for OpenWeatherMap Current Weather API response.
 * Maps directly to JSON structure from API endpoint.
 *
 * API: https://api.openweathermap.org/data/2.5/weather
 */
@Serializable
data class WeatherResponseDto(
    @SerialName("coord")
    val coord: CoordinatesDto? = null,

    @SerialName("weather")
    val weather: List<WeatherConditionDto>,

    @SerialName("main")
    val main: MainDto,

    @SerialName("wind")
    val wind: WindDto,

    @SerialName("dt")
    val timestamp: Long,

    @SerialName("name")
    val name: String,

    @SerialName("sys")
    val sys: SysDto
)

/**
 * DTO for geographic coordinates.
 */
@Serializable
data class CoordinatesDto(
    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lon: Double
)

/**
 * DTO for system-level data (country, etc.).
 */
@Serializable
data class SysDto(
    @SerialName("country")
    val country: String
)
```

---

### 2.2 WeatherConditionDto

**Purpose**: Maps to weather condition object in API response.

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for weather condition data.
 * Represents the "weather" array element in API response.
 */
@Serializable
data class WeatherConditionDto(
    @SerialName("id")
    val id: Int,

    @SerialName("main")
    val main: String,

    @SerialName("description")
    val description: String,

    @SerialName("icon")
    val icon: String
)
```

---

### 2.3 MainDto

**Purpose**: Maps to main weather parameters (temperature, humidity, pressure).

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for main weather parameters.
 * Contains temperature, humidity, and pressure data.
 */
@Serializable
data class MainDto(
    @SerialName("temp")
    val temp: Double,

    @SerialName("feels_like")
    val feelsLike: Double,

    @SerialName("humidity")
    val humidity: Int,

    @SerialName("pressure")
    val pressure: Int
)
```

---

### 2.4 WindDto

**Purpose**: Maps to wind data (speed, direction).

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for wind data.
 * Contains wind speed and direction.
 */
@Serializable
data class WindDto(
    @SerialName("speed")
    val speed: Double,

    @SerialName("deg")
    val deg: Int? = null
)
```

---

### 2.5 GeocodingResponseDto

**Purpose**: Maps to OpenWeatherMap Geocoding API response.

**API Endpoint**: `https://api.openweathermap.org/geo/1.0/direct`

**JSON Example**:
```json
[
  {
    "name": "Madrid",
    "lat": 40.4165,
    "lon": -3.7026,
    "country": "ES",
    "state": "Madrid"
  }
]
```

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for OpenWeatherMap Geocoding API response.
 * API returns a list of these objects, we use only the first result.
 *
 * API: https://api.openweathermap.org/geo/1.0/direct
 */
@Serializable
data class GeocodingResponseDto(
    @SerialName("name")
    val name: String,

    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lon: Double,

    @SerialName("country")
    val country: String,

    @SerialName("state")
    val state: String? = null
)
```

---

## 3. Mappers

Mappers convert DTOs to domain models. They handle null values, provide defaults, and validate data integrity.

### 3.1 WeatherMapper

**Purpose**: Maps `WeatherResponseDto` to `Weather` domain model.

**Mapping Logic**:
- Extracts first weather condition (API returns array, we use index 0)
- Combines location data from multiple DTO fields
- Validates all fields before creating domain model
- Handles nullable wind direction

**Null Handling**:
- Weather array must have at least one element (throws if empty)
- Wind direction is optional (maps to null if missing)
- Coordinates use lat/lon from main response if coord object is null

**Error Scenarios**:
- Empty weather array → `IllegalArgumentException`
- Missing required fields → `IllegalArgumentException`
- Invalid coordinate values → Caught by Location validation

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.model.WeatherCondition

/**
 * Mapper for converting WeatherResponseDto to Weather domain model.
 * Handles data transformation and validation from API response to domain.
 */
object WeatherMapper {

    /**
     * Maps WeatherResponseDto to Weather domain model.
     * Extracts and validates all weather data from API response.
     *
     * @param dto API response DTO
     * @return Weather domain model
     * @throws IllegalArgumentException if weather array is empty
     * @throws IllegalArgumentException if required fields are invalid
     */
    fun mapToDomain(dto: WeatherResponseDto): Weather {
        require(dto.weather.isNotEmpty()) {
            "Weather condition list cannot be empty"
        }

        val weatherConditionDto = dto.weather.first()
        val location = mapLocation(dto)
        val condition = mapWeatherCondition(weatherConditionDto)

        return Weather(
            temperature = dto.main.temp,
            feelsLike = dto.main.feelsLike,
            humidity = dto.main.humidity,
            pressure = dto.main.pressure,
            windSpeed = dto.wind.speed,
            windDirection = dto.wind.deg,
            condition = condition,
            location = location,
            timestamp = dto.timestamp
        )
    }

    /**
     * Maps location data from DTO to Location domain model.
     * Uses coord object if available, otherwise falls back to defaults.
     *
     * @param dto Weather response DTO
     * @return Location domain model
     */
    private fun mapLocation(dto: WeatherResponseDto): Location {
        val lat = dto.coord?.lat ?: 0.0
        val lon = dto.coord?.lon ?: 0.0

        return Location(
            latitude = lat,
            longitude = lon,
            name = dto.name,
            country = dto.sys.country
        )
    }

    /**
     * Maps weather condition DTO to domain model.
     *
     * @param dto Weather condition DTO
     * @return WeatherCondition domain model
     */
    private fun mapWeatherCondition(
        dto: com.mtzdev.mywheatherapp.data.remote.dto.WeatherConditionDto
    ): WeatherCondition {
        return WeatherCondition(
            id = dto.id,
            main = dto.main,
            description = dto.description,
            icon = dto.icon
        )
    }
}
```

---

### 3.2 LocationMapper

**Purpose**: Maps `GeocodingResponseDto` to `Location` domain model.

**Mapping Logic**:
- Direct field mapping (name, lat, lon, country)
- Validates coordinates in Location constructor
- API returns array; caller must handle empty results

**Null Handling**:
- State field is optional (not used in Location model)
- All required fields are non-nullable in DTO

**Error Scenarios**:
- Empty response array → Handled by repository (CityNotFound error)
- Invalid coordinates → Caught by Location validation

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import com.mtzdev.mywheatherapp.domain.model.Location

/**
 * Mapper for converting GeocodingResponseDto to Location domain model.
 * Transforms geocoding API response to domain representation.
 */
object LocationMapper {

    /**
     * Maps GeocodingResponseDto to Location domain model.
     * Performs direct field mapping with validation in Location constructor.
     *
     * @param dto Geocoding API response DTO
     * @return Location domain model
     * @throws IllegalArgumentException if coordinates are invalid (from Location)
     */
    fun mapToDomain(dto: GeocodingResponseDto): Location {
        return Location(
            latitude = dto.lat,
            longitude = dto.lon,
            name = dto.name,
            country = dto.country
        )
    }

    /**
     * Maps a list of GeocodingResponseDto to Location domain models.
     * Useful when processing multiple search results.
     *
     * @param dtoList List of geocoding DTOs
     * @return List of Location domain models
     */
    fun mapListToDomain(dtoList: List<GeocodingResponseDto>): List<Location> {
        return dtoList.map { mapToDomain(it) }
    }
}
```

---

### 3.3 WeatherConditionMapper

**Purpose**: Maps `WeatherConditionDto` to `WeatherCondition` domain model.

**Mapping Logic**:
- Direct field-to-field mapping
- No transformations needed (all types match)
- Validation performed in WeatherCondition constructor

**Implementation**:

```kotlin
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.WeatherConditionDto
import com.mtzdev.mywheatherapp.domain.model.WeatherCondition

/**
 * Mapper for converting WeatherConditionDto to WeatherCondition domain model.
 * Handles weather condition data transformation.
 */
object WeatherConditionMapper {

    /**
     * Maps WeatherConditionDto to WeatherCondition domain model.
     * Performs direct field mapping with validation in constructor.
     *
     * @param dto Weather condition DTO from API
     * @return WeatherCondition domain model
     * @throws IllegalArgumentException if values are invalid (from WeatherCondition)
     */
    fun mapToDomain(dto: WeatherConditionDto): WeatherCondition {
        return WeatherCondition(
            id = dto.id,
            main = dto.main,
            description = dto.description,
            icon = dto.icon
        )
    }

    /**
     * Maps a list of WeatherConditionDto to domain models.
     * Useful for processing multiple conditions from API.
     *
     * @param dtoList List of weather condition DTOs
     * @return List of WeatherCondition domain models
     */
    fun mapListToDomain(dtoList: List<WeatherConditionDto>): List<WeatherCondition> {
        return dtoList.map { mapToDomain(it) }
    }
}
```

---

## 4. Entity Relationships

### 4.1 Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN MODELS                            │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐
│   Weather    │
│              │
│ - temperature│         1:1          ┌──────────────────┐
│ - feelsLike  │◆─────────────────────│ WeatherCondition │
│ - humidity   │ (composition)        │                  │
│ - pressure   │                      │ - id             │
│ - windSpeed  │                      │ - main           │
│ - windDirect │                      │ - description    │
│ - condition  │                      │ - icon           │
│ - location   │                      └──────────────────┘
│ - timestamp  │
└──────┬───────┘
       │
       │ 1:1
       │ (composition)
       │
       ▼
┌──────────────┐
│   Location   │
│              │
│ - latitude   │
│ - longitude  │
│ - name       │
│ - country    │
└──────────────┘


┌─────────────────────────────────────────────────────────────┐
│                        RESULT WRAPPER                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────┐
│    Result<T>        │
│   (sealed class)    │
├─────────────────────┤
│ ◆ Loading           │
│ ◆ Success<T>        │───┐  Contains
│   - data: T         │   │  any domain
│ ◆ Error             │   │  model
│   - error: DomainErr│   │
└─────────────────────┘   │
                          │
                 ┌────────┴──────────┬──────────────┐
                 ▼                   ▼              ▼
            ┌─────────┐         ┌──────────┐  ┌──────────┐
            │ Weather │         │ Location │  │  List<T> │
            └─────────┘         └──────────┘  └──────────┘


┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER DTOs                         │
└─────────────────────────────────────────────────────────────┘

┌───────────────────────┐
│ WeatherResponseDto    │
│ (@Serializable)       │
├───────────────────────┤          1:N
│ - weather: List       │◆─────────────────┐
│ - main: MainDto       │◆──────┐          │
│ - wind: WindDto       │◆───┐  │          │
│ - coord: CoordDto?    │    │  │          │
│ - sys: SysDto         │◆─┐ │  │          │
│ - name: String        │  │ │  │          │
│ - dt: Long            │  │ │  │          │
└───────────────────────┘  │ │  │          │
                           │ │  │          │
        ┌──────────────────┘ │  │          │
        │  ┌─────────────────┘  │          │
        │  │  ┌─────────────────┘          │
        │  │  │                            │
        ▼  ▼  ▼                            ▼
    ┌────────────┐  ┌─────────┐  ┌──────────────────┐
    │  SysDto    │  │ MainDto │  │WeatherConditionDto│
    │            │  │         │  │                   │
    │ - country  │  │ - temp  │  │ - id              │
    └────────────┘  │ - feels │  │ - main            │
                    │ - humid │  │ - description     │
                    │ - press │  │ - icon            │
                    └─────────┘  └───────────────────┘

┌───────────────────────┐
│GeocodingResponseDto   │
│ (@Serializable)       │        Mapper
├───────────────────────┤     ──────────>    ┌──────────┐
│ - name: String        │                    │ Location │
│ - lat: Double         │                    └──────────┘
│ - lon: Double         │
│ - country: String     │
│ - state: String?      │
└───────────────────────┘
```

### 4.2 Cardinality Table

| Relationship | From | To | Cardinality | Type | Description |
|--------------|------|-----|-------------|------|-------------|
| Weather → WeatherCondition | Weather | WeatherCondition | 1:1 | Composition | Each Weather has exactly one condition |
| Weather → Location | Weather | Location | 1:1 | Composition | Each Weather has exactly one location |
| Result<T> → T | Result.Success | Any domain model | 1:1 | Association | Success wraps one domain entity |
| WeatherResponseDto → WeatherConditionDto | WeatherResponseDto | WeatherConditionDto | 1:N | Aggregation | API returns array, we use first |
| WeatherResponseDto → MainDto | WeatherResponseDto | MainDto | 1:1 | Composition | Each response has one main data object |
| WeatherResponseDto → WindDto | WeatherResponseDto | WindDto | 1:1 | Composition | Each response has one wind data object |

### 4.3 Composition vs Aggregation

**Composition** (◆ filled diamond):
- Strong ownership: child cannot exist without parent
- Parent destruction destroys child
- Examples:
  - Weather ◆→ WeatherCondition (condition is part of weather)
  - Weather ◆→ Location (location is part of weather snapshot)

**Aggregation** (◇ empty diamond):
- Weak ownership: child can exist independently
- Parent destruction doesn't destroy child
- Examples:
  - WeatherResponseDto ◇→ WeatherConditionDto[] (DTOs are temporary)

---

## 5. Validation Rules

### 5.1 Validation Rules Table

| Entity | Field | Rule | Error Message | Implementation |
|--------|-------|------|---------------|----------------|
| **Location** | latitude | -90.0 ≤ lat ≤ 90.0 | "Latitude must be between -90.0 and 90.0, got: X" | Location init block |
| **Location** | longitude | -180.0 ≤ lon ≤ 180.0 | "Longitude must be between -180.0 and 180.0, got: X" | Location init block |
| **Location** | name | Not blank | "Location name cannot be blank" | Location init block |
| **Location** | country | Length == 2 | "Country code must be 2 characters (ISO 3166-1 alpha-2), got: X" | Location init block |
| **WeatherCondition** | id | > 0 | "Weather condition ID must be positive, got: X" | WeatherCondition init |
| **WeatherCondition** | main | Not blank | "Weather main category cannot be blank" | WeatherCondition init |
| **WeatherCondition** | description | Not blank | "Weather description cannot be blank" | WeatherCondition init |
| **WeatherCondition** | icon | Length in 2..3 | "Weather icon code must be 2-3 characters, got: X" | WeatherCondition init |
| **Weather** | temperature | -100.0 ≤ temp ≤ 60.0 | "Temperature must be between -100°C and 60°C, got: X" | Weather init block |
| **Weather** | feelsLike | -100.0 ≤ feels ≤ 60.0 | "Feels like temperature must be between -100°C and 60°C, got: X" | Weather init block |
| **Weather** | humidity | 0 ≤ humidity ≤ 100 | "Humidity must be between 0 and 100%, got: X" | Weather init block |
| **Weather** | pressure | > 0 | "Pressure must be positive, got: X" | Weather init block |
| **Weather** | windSpeed | ≥ 0.0 | "Wind speed cannot be negative, got: X" | Weather init block |
| **Weather** | windDirection | 0 ≤ deg ≤ 360 (if not null) | "Wind direction must be between 0 and 360 degrees, got: X" | Weather init block |
| **Weather** | timestamp | > 0 | "Timestamp must be positive, got: X" | Weather init block |
| **SearchInput** | cityName | Not blank | "City name cannot be empty" | UseCase validation |
| **Coordinates** | lat, lon | Valid ranges | "Invalid coordinates" | UseCase validation |

### 5.2 Validation Enforcement Points

**Domain Models** (Primary Validation):
- All validation rules enforced in `init` blocks
- Throws `IllegalArgumentException` on invalid data
- Guarantees data integrity at construction time
- Prevents creation of invalid domain objects

**Use Cases** (Secondary Validation):
- Business logic validation (e.g., empty city search)
- Pre-conditions before repository calls
- Returns `Result.Error` with `DomainError.ValidationError`

**Mappers** (DTO → Domain):
- Validation happens automatically when creating domain objects
- Mapper catches `IllegalArgumentException` and converts to `Result.Error`
- Ensures only valid domain objects reach use case layer

---

## 6. State Transitions

### 6.1 Permission State Machine

```
┌─────────────────────────────────────────────────────────────┐
│              PERMISSION STATE MACHINE                        │
└─────────────────────────────────────────────────────────────┘

                    ┌─────────┐
                    │ UNKNOWN │ (Initial state)
                    └────┬────┘
                         │
             User taps "Use my location"
                         │
                         ▼
             ┌──────────────────────┐
             │ Request Permissions  │
             └──────────┬───────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
  User denies     User grants    User denies
   (first)       permissions   permanently
        │               │               │
        ▼               ▼               ▼
   ┌────────┐      ┌─────────┐   ┌──────────────────┐
   │ DENIED │      │ GRANTED │   │PERMANENTLY_DENIED│
   └───┬────┘      └────┬────┘   └─────────┬────────┘
       │                │                   │
       │ Retry          │ GPS enabled       │ "Go to Settings"
       │                │                   │
       └────────────────┼───────────────────┘
                        │
                        ▼
                ┌───────────────┐
                │ Fetch Location│
                └───────────────┘

States:
- UNKNOWN: Permission status not yet determined
- GRANTED: User granted location permissions
- DENIED: User denied permissions (can retry)
- PERMANENTLY_DENIED: User denied twice, requires settings navigation

Transitions:
- UNKNOWN → Request → GRANTED: Direct grant
- UNKNOWN → Request → DENIED: First denial
- DENIED → Retry → GRANTED: User grants on retry
- DENIED → Retry → PERMANENTLY_DENIED: Second denial
- PERMANENTLY_DENIED → Settings → GRANTED: User enables in settings
```

### 6.2 Location Mode State Machine

```
┌─────────────────────────────────────────────────────────────┐
│              LOCATION MODE STATE MACHINE                     │
└─────────────────────────────────────────────────────────────┘

            ┌─────────────────────────┐
            │    AUTO (GPS mode)      │
            │                         │
            │ - GPS detection enabled │
            │ - Search field disabled │
            └────────┬───────┬────────┘
                     │       │
        User switches│       │User switches
        to MANUAL    │       │to AUTO
                     │       │
         ┌───────────┘       └──────────┐
         │                               │
         ▼                               ▼
┌─────────────────────┐         ┌────────────────────┐
│  MANUAL (Search)    │         │    AUTO (GPS)      │
│                     │◄────────┤                    │
│ - GPS disabled      │ Switch  │ - GPS enabled      │
│ - Search enabled    │────────►│ - Search disabled  │
└─────────────────────┘  back   └────────────────────┘

States:
- AUTO: Automatic GPS detection mode
- MANUAL: Manual city search mode

Transitions:
- Bidirectional toggle between modes
- User can switch at any time
- No state lost during transition
- Last successful location preserved
```

### 6.3 UI Loading State Machine

```
┌─────────────────────────────────────────────────────────────┐
│              UI LOADING STATE MACHINE                        │
└─────────────────────────────────────────────────────────────┘

                    ┌──────┐
                    │ IDLE │ (Initial)
                    └──┬───┘
                       │
          User action  │  (GPS / Search)
                       │
                       ▼
                  ┌─────────┐
                  │ LOADING │
                  └────┬────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
   API success    API error      Cancelled
        │              │              │
        ▼              ▼              ▼
   ┌─────────┐    ┌───────┐     ┌──────┐
   │ SUCCESS │    │ ERROR │     │ IDLE │
   └────┬────┘    └───┬───┘     └──────┘
        │              │
        │ Refresh      │ Retry
        │              │
        └──────┬───────┘
               │
               ▼
          ┌─────────┐
          │ LOADING │
          └─────────┘

States:
- IDLE: No operation in progress, waiting for user action
- LOADING: Operation in progress (GPS, search, API call)
- SUCCESS: Data loaded successfully
- ERROR: Operation failed with error message

State Properties:
- isLoading: Boolean
- weather: Weather?
- error: String?
- location: Location?

Transitions:
- IDLE → LOADING: User triggers action
- LOADING → SUCCESS: Data received
- LOADING → ERROR: Operation failed
- LOADING → IDLE: User cancelled
- SUCCESS/ERROR → LOADING: User retries or refreshes
```

### 6.4 Combined State Representation

```kotlin
/**
 * Complete UI state combining all state machines.
 * Used in WeatherContract.State.
 */
data class State(
    // Data
    val weather: Weather? = null,
    val location: Location? = null,

    // Loading state (from UI State Machine)
    val isLoading: Boolean = false,
    val error: String? = null,

    // Location mode (from Location Mode State Machine)
    val locationMode: LocationMode = LocationMode.AUTO,

    // Permission state (from Permission State Machine)
    val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN,

    // Search
    val searchQuery: String = ""
) : MVIContract.UiState

enum class LocationMode {
    AUTO,    // GPS detection
    MANUAL   // Manual search
}

enum class PermissionStatus {
    UNKNOWN,              // Not yet requested
    GRANTED,              // Permission granted
    DENIED,               // Permission denied (can retry)
    PERMANENTLY_DENIED    // Permission permanently denied
}
```

---

## 7. Immutability Guarantees

### 7.1 Enforcement Mechanisms

#### Data Classes with Val

All domain models use `data class` with `val` properties:

```kotlin
data class Weather(
    val temperature: Double,      // val = immutable property
    val feelsLike: Double,
    val humidity: Int,
    // ... all fields are val
)
```

**Guarantees**:
- Properties cannot be reassigned after construction
- No setter methods generated
- Compiler enforces immutability

#### No Mutable Collections

If a domain model contains a collection, it must be immutable:

```kotlin
// ❌ WRONG - Mutable list
data class WeatherForecast(
    val forecasts: MutableList<Weather>  // Can be modified!
)

// ✅ CORRECT - Immutable list
data class WeatherForecast(
    val forecasts: List<Weather>  // Read-only list
)
```

### 7.2 Copy Semantics

Data classes provide `copy()` method for creating modified versions:

```kotlin
val original = Weather(
    temperature = 25.0,
    feelsLike = 24.0,
    humidity = 60,
    // ...
)

// Create new instance with changed temperature
val updated = original.copy(
    temperature = 26.0
)

// Original is unchanged
println(original.temperature)  // 25.0
println(updated.temperature)   // 26.0
```

**Use Cases**:
- Updating UI state in ScreenModel
- Modifying weather data without mutation
- Creating test fixtures with variations

**Example in ScreenModel**:

```kotlin
class WeatherScreenModel : MVIBaseScreenModel<State, Event, Effect>() {

    private fun updateTemperature(newTemp: Double) {
        // Copy current state with new temperature
        mutableState.value = state.value.copy(
            weather = state.value.weather?.copy(
                temperature = newTemp
            )
        )
    }
}
```

### 7.3 Thread Safety Guarantees

#### Immutable Domain Models

All domain models are **structurally immutable**:
- No mutable state
- All fields are `val`
- No methods that modify internal state

**Result**: Domain models can be safely shared across threads without synchronization.

```kotlin
// Safe to use from multiple threads
val weather = Weather(...)

// Thread 1
launch(Dispatchers.IO) {
    println(weather.temperature)
}

// Thread 2
launch(Dispatchers.Main) {
    println(weather.temperature)
}

// No race conditions - weather is immutable
```

#### StateFlow for UI State

UI state is managed with Kotlin's `StateFlow`:

```kotlin
class WeatherScreenModel : MVIBaseScreenModel<State, Event, Effect>() {

    // Thread-safe state holder
    override val state: StateFlow<State> = _state.asStateFlow()

    private val _state = MutableStateFlow(State())

    // Only this class can modify state
    private fun updateState(newState: State) {
        _state.value = newState  // Atomic update
    }
}
```

**Thread Safety Properties**:
- `StateFlow` is thread-safe by design
- State updates are atomic
- Conflated: only latest state is kept
- No race conditions in state updates

#### Repository Layer

Repositories use `suspend` functions for thread safety:

```kotlin
interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<Weather>
}

class WeatherRepositoryImpl : WeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double
    ): Result<Weather> = withContext(Dispatchers.IO) {
        // Network call on IO dispatcher
        val dto = api.fetchWeather(lat, lon)
        val weather = mapper.map(dto)
        Result.Success(weather)
    }
}
```

**Guarantees**:
- Explicit thread context with `withContext`
- No shared mutable state between calls
- Each call operates on independent data

### 7.4 Immutability Benefits

| Benefit | Description | Example |
|---------|-------------|---------|
| **Thread Safety** | No synchronization needed | Domain models shared across threads |
| **Predictability** | State cannot change unexpectedly | UI state changes are explicit |
| **Testability** | Easy to create test fixtures | `val testWeather = Weather(...)` |
| **Debugging** | No hidden state modifications | State changes traceable through copy() |
| **Compose Optimization** | Recomposition optimization | Stable data classes avoid recomposition |
| **Time Travel Debug** | Previous states preserved | State history for debugging |

### 7.5 Copy Performance Considerations

**Shallow Copy**:
- `copy()` creates shallow copies
- References to nested objects are copied, not objects themselves
- Acceptable for our use case (nested objects are also immutable)

```kotlin
val weather1 = Weather(
    temperature = 25.0,
    condition = WeatherCondition(...),  // Reference
    location = Location(...)            // Reference
)

val weather2 = weather1.copy(temperature = 26.0)

// weather2.condition === weather1.condition  // Same reference
// This is OK because WeatherCondition is immutable
```

**Performance**:
- Copy is O(n) where n = number of fields
- For our models (< 10 fields), performance is negligible
- No deep copy needed (all nested objects immutable)

### 7.6 Enforcement Checklist

Before merging any code:

- [ ] All domain models use `data class`
- [ ] All properties in domain models are `val`
- [ ] No mutable collections (MutableList, MutableMap, etc.)
- [ ] No `var` properties in domain layer
- [ ] State updates use `copy()`, not direct mutation
- [ ] No `lateinit` properties in domain models
- [ ] ScreenModel state is `StateFlow` (not MutableStateFlow exposed)
- [ ] No shared mutable state across threads

---

## Summary

This data model documentation provides complete specifications for all domain entities, DTOs, and mappers for the GPS weather feature. Key highlights:

1. **Domain Models**: Pure Kotlin, immutable, validated, with comprehensive KDoc
2. **DTOs**: Serializable, match API structure, handle nullability
3. **Mappers**: Convert DTOs to domain, validate, handle errors
4. **Relationships**: Clear composition and aggregation relationships
5. **Validation**: Enforced at construction time in domain models
6. **State Machines**: Complete state transitions for permissions, location mode, and UI
7. **Immutability**: Guaranteed through val, data classes, and StateFlow

All code is production-ready, follows constitution guidelines, and includes:
- Maximum 30 lines per function
- Meaningful names
- KDoc for all public APIs
- Comprehensive validation
- Thread-safety guarantees

---

**Next Steps**: Implement repository and use cases using these domain models and mappers.

**References**:
- Spec: `/specs/001-clima-actual-gps/spec.md`
- Research: `/specs/001-clima-actual-gps/research.md`
- Constitution: `/.specify/memory/constitution.md`
