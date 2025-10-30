# Quickstart Guide: GPS Weather Feature

**Feature**: 001-clima-actual-gps
**Target Implementation Time**: 2-3 days for experienced developer
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Quick Setup (5-10 minutes)](#2-quick-setup-5-10-minutes)
3. [Implementation Steps (Layer by Layer)](#3-implementation-steps-layer-by-layer)
4. [Running the Feature](#4-running-the-feature)
5. [Troubleshooting](#5-troubleshooting)
6. [Next Steps](#6-next-steps)
7. [Quick Reference](#7-quick-reference)

---

## 1. Prerequisites

### 1.1 Required Dependencies

The following dependencies need to be added to your project:

**New Dependencies**:
- `play-services-location`: 21.3.0 (Google Play Services Location API)
- `accompanist-permissions`: 0.36.0 (Compose permission handling)

**Existing Dependencies** (already configured):
- Ktor 3.3.0 (HTTP client)
- Koin 4.1.0 (Dependency injection)
- Voyager 1.1.0-beta02 (Navigation + ScreenModel)
- Compose BOM 2024.09.00
- Testing: JUnit 4.13.2, MockK 1.14.6, Turbine 1.2.1

### 1.2 Android API Level Requirements

- **minSdk**: 24 (Android 7.0+) - Already configured
- **targetSdk**: 36 - Already configured
- **compileSdk**: 36 - Already configured

### 1.3 OpenWeatherMap API Key Setup

You need an API key from OpenWeatherMap:

1. Go to [https://openweathermap.org/api](https://openweathermap.org/api)
2. Sign up for a free account
3. Generate an API key (Free tier: 60 calls/minute, 1M calls/month)
4. Copy your API key

---

## 2. Quick Setup (5-10 minutes)

Follow this checklist to get started:

### Step 1: Add Dependencies to gradle/libs.versions.toml

Add these lines to your `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.13.0"
kotlin = "2.1.0"
# ... existing versions ...
playServicesLocation = "21.3.0"
accompanist = "0.36.0"

[libraries]
# ... existing libraries ...

# Google Play Services Location
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }

# Accompanist Permissions
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
```

**File**: `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/gradle/libs.versions.toml`

### Step 2: Add Dependencies to app/build.gradle.kts

Add these lines to your dependencies block in `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...

    // NEW: Google Play Services Location
    implementation(libs.play.services.location)

    // NEW: Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // ... rest of dependencies ...
}
```

**File**: `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/build.gradle.kts`

### Step 3: Add Permissions to AndroidManifest.xml

Add these permissions to your manifest:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Location permissions -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Internet permission (may already exist) -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Optional: GPS feature (not required) -->
    <uses-feature
        android:name="android.hardware.location.gps"
        android:required="false" />

    <application
        <!-- ... existing attributes ... -->
    </application>
</manifest>
```

**File**: `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/AndroidManifest.xml`

### Step 4: Configure API Key in local.properties

Add your OpenWeatherMap API key to `local.properties` (this file is gitignored):

```properties
# OpenWeatherMap API Key
weather_api="your_api_key_here"
```

**File**: `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/local.properties`

**Note**: The API key is already configured to be read by BuildConfig in `app/build.gradle.kts` (line 29).

### Step 5: Sync Gradle

Run Gradle sync:

```bash
./gradlew --refresh-dependencies
```

---

## 3. Implementation Steps (Layer by Layer)

Follow Clean Architecture: **Domain** → **Data** → **Presentation**

### Step 1: Domain Layer (Pure Kotlin)

The domain layer contains business logic and is framework-agnostic.

#### 3.1.1 Create Domain Models

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Weather.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.model

data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: Int?,
    val cityName: String,
    val country: String
)
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Location.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.model

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val state: String? = null
)
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Coordinates.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Result.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * A generic wrapper for operation results in the domain layer.
 * Represents success/error states of an asynchronous operation.
 */
sealed class Result<out T> {

    data object Loading : Result<Nothing>()

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val error: DomainError) : Result<Nothing>()
}

// Extension functions
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}
fun <T> Result<T>.errorOrNull(): DomainError? = when (this) {
    is Result.Error -> error
    else -> null
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/model/DomainError.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.model

/**
 * Sealed hierarchy of domain-level errors.
 */
sealed class DomainError {

    sealed class LocationError : DomainError() {
        data object PermissionDenied : LocationError()
        data object Unavailable : LocationError()
        data object Timeout : LocationError()
        data object GpsDisabled : LocationError()
    }

    sealed class WeatherError : DomainError() {
        data object CityNotFound : WeatherError()
        data object InvalidApiKey : WeatherError()
        data object RateLimitExceeded : WeatherError()
        data object NoInternetConnection : WeatherError()
        data object ServerError : WeatherError()
        data class Unknown(val message: String?) : WeatherError()
    }

    sealed class ValidationError : DomainError() {
        data object EmptyCityName : ValidationError()
        data object InvalidCoordinates : ValidationError()
    }
}
```

#### 3.1.2 Create Repository Interfaces

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/WeatherRepository.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather

interface WeatherRepository {
    suspend fun getCurrentWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<Weather>
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/LocationRepository.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result

interface LocationRepository {
    suspend fun searchLocation(cityName: String): Result<Location>
}
```

#### 3.1.3 Create Use Cases

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentWeatherByCoordinatesUseCase.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository

class GetCurrentWeatherByCoordinatesUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<Weather> {
        // Validation
        if (!isValidCoordinate(latitude, longitude)) {
            return Result.Error(DomainError.ValidationError.InvalidCoordinates)
        }

        // Delegate to repository
        return weatherRepository.getCurrentWeatherByCoordinates(latitude, longitude)
    }

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/SearchLocationUseCase.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository

class SearchLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(cityName: String): Result<Location> {
        // Validation
        if (cityName.isBlank()) {
            return Result.Error(DomainError.ValidationError.EmptyCityName)
        }

        // Delegate to repository
        return locationRepository.searchLocation(cityName.trim())
    }
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentLocationUseCase.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.data.location.LocationException
import com.mtzdev.mywheatherapp.data.location.LocationProvider
import com.mtzdev.mywheatherapp.domain.model.Coordinates
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import kotlinx.coroutines.TimeoutCancellationException

class GetCurrentLocationUseCase(
    private val locationProvider: LocationProvider
) {
    suspend operator fun invoke(): Result<Coordinates> = try {
        val coordinates = locationProvider.getCurrentLocation()
        Result.Success(coordinates)
    } catch (e: SecurityException) {
        Result.Error(DomainError.LocationError.PermissionDenied)
    } catch (e: LocationException) {
        Result.Error(DomainError.LocationError.Unavailable)
    } catch (e: TimeoutCancellationException) {
        Result.Error(DomainError.LocationError.Timeout)
    } catch (e: Exception) {
        Result.Error(DomainError.LocationError.Unavailable)
    }
}
```

---

### Step 2: Data Layer

The data layer handles external data sources (API, sensors).

#### 3.2.1 Create DTOs with @Serializable

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/WeatherResponseDto.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    @SerialName("coord")
    val coord: CoordinatesDto? = null,

    @SerialName("weather")
    val weather: List<WeatherConditionDto>,

    @SerialName("main")
    val main: MainWeatherDto,

    @SerialName("wind")
    val wind: WindDto,

    @SerialName("name")
    val name: String,

    @SerialName("sys")
    val sys: SysDto
)

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

@Serializable
data class MainWeatherDto(
    @SerialName("temp")
    val temp: Double,

    @SerialName("feels_like")
    val feelsLike: Double,

    @SerialName("humidity")
    val humidity: Int,

    @SerialName("pressure")
    val pressure: Int
)

@Serializable
data class WindDto(
    @SerialName("speed")
    val speed: Double,

    @SerialName("deg")
    val deg: Int? = null
)

@Serializable
data class SysDto(
    @SerialName("country")
    val country: String
)

@Serializable
data class CoordinatesDto(
    @SerialName("lat")
    val lat: Double,

    @SerialName("lon")
    val lon: Double
)
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/GeocodingResponseDto.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

#### 3.2.2 Create Remote Data Sources

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/remote/datasource/WeatherRemoteDataSource.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.remote.datasource

import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for fetching weather data from OpenWeatherMap API.
 */
class WeatherRemoteDataSource(
    private val client: HttpClient,
    private val apiKey: String
) {

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponseDto {
        return client.get("https://api.openweathermap.org/data/2.5/weather") {
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("appid", apiKey)
            parameter("units", "metric")
            parameter("lang", "es")
        }.body()
    }
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/remote/datasource/GeocodingRemoteDataSource.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.remote.datasource

import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for geocoding (city name to coordinates).
 */
class GeocodingRemoteDataSource(
    private val client: HttpClient,
    private val apiKey: String
) {

    suspend fun searchLocation(cityName: String): List<GeocodingResponseDto> {
        return client.get("https://api.openweathermap.org/geo/1.0/direct") {
            parameter("q", cityName)
            parameter("appid", apiKey)
            parameter("limit", 1)
        }.body()
    }
}
```

#### 3.2.3 Create Location Provider

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/location/LocationProvider.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mtzdev.mywheatherapp.domain.model.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Provider for obtaining device location using FusedLocationProviderClient.
 */
class LocationProvider(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates {
        return withTimeout(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            Coordinates(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        continuation.resumeWithException(
                            LocationException("Location is null - GPS may be disabled")
                        )
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(
                        LocationException("Failed to get location: ${exception.message}", exception)
                    )
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_TIMEOUT_MS = 10_000L // 10 seconds
    }
}

/**
 * Exception thrown when location cannot be obtained.
 */
class LocationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
```

#### 3.2.4 Create Mappers

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/mapper/WeatherMapper.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.WeatherResponseDto
import com.mtzdev.mywheatherapp.domain.model.Weather

class WeatherMapper {

    fun mapToDomain(dto: WeatherResponseDto): Weather {
        require(dto.weather.isNotEmpty()) { "Weather list cannot be empty" }

        return Weather(
            temperature = dto.main.temp,
            feelsLike = dto.main.feelsLike,
            description = dto.weather.first().description,
            icon = dto.weather.first().icon,
            humidity = dto.main.humidity,
            pressure = dto.main.pressure,
            windSpeed = dto.wind.speed,
            windDirection = dto.wind.deg,
            cityName = dto.name,
            country = dto.sys.country
        )
    }
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/mapper/LocationMapper.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import com.mtzdev.mywheatherapp.domain.model.Location

class LocationMapper {

    fun mapToDomain(dto: GeocodingResponseDto): Location {
        return Location(
            name = dto.name,
            latitude = dto.lat,
            longitude = dto.lon,
            country = dto.country,
            state = dto.state
        )
    }
}
```

#### 3.2.5 Implement Repositories

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/repository/WeatherRepositoryImpl.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.model.Weather
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherMapper: WeatherMapper
) : WeatherRepository {

    override suspend fun getCurrentWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<Weather> = withContext(Dispatchers.IO) {
        try {
            val dto = weatherRemoteDataSource.getCurrentWeather(latitude, longitude)
            val weather = weatherMapper.mapToDomain(dto)
            Result.Success(weather)
        } catch (e: ClientRequestException) {
            Result.Error(handleClientError(e))
        } catch (e: ServerResponseException) {
            Result.Error(DomainError.WeatherError.ServerError)
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.WeatherError.NoInternetConnection)
        } catch (e: SocketTimeoutException) {
            Result.Error(DomainError.WeatherError.Unknown("Request timeout"))
        } catch (e: Exception) {
            Result.Error(DomainError.WeatherError.Unknown(e.message))
        }
    }

    private fun handleClientError(exception: ClientRequestException): DomainError {
        return when (exception.response.status) {
            HttpStatusCode.Unauthorized -> DomainError.WeatherError.InvalidApiKey
            HttpStatusCode.NotFound -> DomainError.WeatherError.CityNotFound
            HttpStatusCode.TooManyRequests -> DomainError.WeatherError.RateLimitExceeded
            else -> DomainError.WeatherError.Unknown(exception.message)
        }
    }
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/data/repository/LocationRepositoryImpl.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.mapper.LocationMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class LocationRepositoryImpl(
    private val geocodingRemoteDataSource: GeocodingRemoteDataSource,
    private val locationMapper: LocationMapper
) : LocationRepository {

    override suspend fun searchLocation(cityName: String): Result<Location> =
        withContext(Dispatchers.IO) {
            try {
                val results = geocodingRemoteDataSource.searchLocation(cityName)

                if (results.isEmpty()) {
                    return@withContext Result.Error(DomainError.WeatherError.CityNotFound)
                }

                val location = locationMapper.mapToDomain(results.first())
                Result.Success(location)
            } catch (e: ClientRequestException) {
                Result.Error(DomainError.WeatherError.Unknown(e.message))
            } catch (e: ServerResponseException) {
                Result.Error(DomainError.WeatherError.ServerError)
            } catch (e: UnknownHostException) {
                Result.Error(DomainError.WeatherError.NoInternetConnection)
            } catch (e: Exception) {
                Result.Error(DomainError.WeatherError.Unknown(e.message))
            }
        }
}
```

---

### Step 3: Presentation Layer

The presentation layer handles UI and user interactions.

#### 3.3.1 Create WeatherContract (MVI)

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherContract.kt`

```kotlin
package com.mtzdev.mywheatherapp.ui.weather

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Weather

object WeatherContract {

    data class State(
        val weather: Weather? = null,
        val location: Location? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val locationMode: LocationMode = LocationMode.AUTO,
        val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN,
        val searchQuery: String = ""
    ) : MVIContract.UiState

    enum class LocationMode {
        AUTO,    // GPS detection
        MANUAL   // Manual city search
    }

    enum class PermissionStatus {
        UNKNOWN,              // Not yet requested
        GRANTED,              // Permission granted
        DENIED,               // Permission denied (can retry)
        PERMANENTLY_DENIED    // Permission permanently denied
    }

    sealed interface Event : MVIContract.UiEvent {
        data object RequestAutoDetection : Event
        data class SearchCity(val cityName: String) : Event
        data class SwitchLocationMode(val mode: LocationMode) : Event
        data class OnPermissionResult(val granted: Boolean) : Event
        data object ShowPermissionSettings : Event
        data object RetryLastAction : Event
        data object ClearError : Event
        data class OnSearchQueryChanged(val query: String) : Event
    }

    sealed interface Effect : MVIContract.Effect {
        data object NavigateToSettings : Effect
        data class ShowSnackbar(val message: String) : Effect
        data object RequestLocationPermission : Effect
    }
}
```

#### 3.3.2 Create WeatherScreenModel

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreenModel.kt`

```kotlin
package com.mtzdev.mywheatherapp.ui.weather

import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentWeatherByCoordinatesUseCase
import com.mtzdev.mywheatherapp.domain.usecase.SearchLocationUseCase
import kotlinx.coroutines.launch

class WeatherScreenModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getCurrentWeatherByCoordinatesUseCase: GetCurrentWeatherByCoordinatesUseCase,
    private val searchLocationUseCase: SearchLocationUseCase
) : MVIBaseScreenMode<WeatherContract.State, WeatherContract.Event, WeatherContract.Effect>(
    initialState = WeatherContract.State()
) {

    override fun handleEvent(event: WeatherContract.Event) {
        when (event) {
            is WeatherContract.Event.RequestAutoDetection -> handleAutoDetection()
            is WeatherContract.Event.SearchCity -> handleSearchCity(event.cityName)
            is WeatherContract.Event.SwitchLocationMode -> handleSwitchMode(event.mode)
            is WeatherContract.Event.OnPermissionResult -> handlePermissionResult(event.granted)
            is WeatherContract.Event.ShowPermissionSettings -> handleShowSettings()
            is WeatherContract.Event.RetryLastAction -> handleRetry()
            is WeatherContract.Event.ClearError -> handleClearError()
            is WeatherContract.Event.OnSearchQueryChanged -> handleSearchQueryChanged(event.query)
        }
    }

    private fun handleAutoDetection() {
        if (state.value.permissionStatus != WeatherContract.PermissionStatus.GRANTED) {
            sendEffect { WeatherContract.Effect.RequestLocationPermission }
            return
        }

        screenModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true, error = null)

            when (val locationResult = getCurrentLocationUseCase()) {
                is Result.Success -> {
                    val coordinates = locationResult.data
                    fetchWeatherForCoordinates(coordinates.latitude, coordinates.longitude)
                }
                is Result.Error -> {
                    handleLocationError(locationResult.error)
                }
                is Result.Loading -> { }
            }
        }
    }

    private suspend fun fetchWeatherForCoordinates(latitude: Double, longitude: Double) {
        when (val weatherResult = getCurrentWeatherByCoordinatesUseCase(latitude, longitude)) {
            is Result.Success -> {
                mutableState.value = state.value.copy(
                    weather = weatherResult.data,
                    isLoading = false,
                    error = null
                )
            }
            is Result.Error -> {
                handleWeatherError(weatherResult.error)
            }
            is Result.Loading -> { }
        }
    }

    private fun handleSearchCity(cityName: String) {
        if (cityName.isBlank()) {
            mutableState.value = state.value.copy(
                error = "Por favor ingresa un nombre de ciudad"
            )
            return
        }

        screenModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true, error = null)

            when (val locationResult = searchLocationUseCase(cityName)) {
                is Result.Success -> {
                    val location = locationResult.data
                    mutableState.value = state.value.copy(location = location)
                    fetchWeatherForCoordinates(location.latitude, location.longitude)
                }
                is Result.Error -> {
                    handleSearchError(locationResult.error)
                }
                is Result.Loading -> { }
            }
        }
    }

    private fun handleSwitchMode(mode: WeatherContract.LocationMode) {
        mutableState.value = state.value.copy(
            locationMode = mode,
            error = null
        )
    }

    private fun handlePermissionResult(granted: Boolean) {
        mutableState.value = state.value.copy(
            permissionStatus = if (granted) {
                WeatherContract.PermissionStatus.GRANTED
            } else {
                WeatherContract.PermissionStatus.DENIED
            }
        )

        if (granted) {
            handleAutoDetection()
        }
    }

    private fun handleShowSettings() {
        sendEffect { WeatherContract.Effect.NavigateToSettings }
    }

    private fun handleRetry() {
        when (state.value.locationMode) {
            WeatherContract.LocationMode.AUTO -> handleAutoDetection()
            WeatherContract.LocationMode.MANUAL -> {
                if (state.value.searchQuery.isNotBlank()) {
                    handleSearchCity(state.value.searchQuery)
                }
            }
        }
    }

    private fun handleClearError() {
        mutableState.value = state.value.copy(error = null)
    }

    private fun handleSearchQueryChanged(query: String) {
        mutableState.value = state.value.copy(searchQuery = query)
    }

    private fun handleLocationError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.LocationError.PermissionDenied ->
                "Necesitamos permisos de ubicación para esta función"
            is DomainError.LocationError.GpsDisabled ->
                "Activa el GPS para detectar tu ubicación"
            is DomainError.LocationError.Timeout ->
                "No se pudo obtener ubicación. Intenta con búsqueda manual"
            is DomainError.LocationError.Unavailable ->
                "Ubicación no disponible. Intenta más tarde"
            else -> "Error al obtener ubicación"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }

    private fun handleWeatherError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.WeatherError.NoInternetConnection ->
                "Sin conexión a internet. Verifica tu conexión"
            is DomainError.WeatherError.ServerError ->
                "Error del servidor. Intenta más tarde"
            is DomainError.WeatherError.RateLimitExceeded ->
                "Demasiadas solicitudes. Espera un momento"
            else -> "Error al obtener datos del clima"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }

    private fun handleSearchError(error: DomainError) {
        val errorMessage = when (error) {
            is DomainError.WeatherError.CityNotFound ->
                "No encontramos esa ubicación. Verifica el nombre"
            is DomainError.ValidationError.EmptyCityName ->
                "Por favor ingresa un nombre de ciudad"
            else -> "Error en la búsqueda"
        }

        mutableState.value = state.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }
}
```

#### 3.3.3 Create UI Components

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/LocationPermissionHandler.kt`

```kotlin
package com.mtzdev.mywheatherapp.ui.weather.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermanentlyDenied: () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val showRationale = remember { mutableStateOf(false) }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        when {
            permissionsState.allPermissionsGranted -> {
                onPermissionGranted()
            }
            permissionsState.shouldShowRationale -> {
                showRationale.value = true
            }
            !permissionsState.shouldShowRationale &&
                permissionsState.permissions.any { !it.status.isGranted } -> {
                onPermanentlyDenied()
            }
        }
    }

    if (showRationale.value) {
        PermissionRationaleDialog(
            onDismiss = {
                showRationale.value = false
                onPermissionDenied()
            },
            onConfirm = {
                showRationale.value = false
                permissionsState.launchMultiplePermissionRequest()
            }
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permiso de ubicación necesario") },
        text = {
            Text(
                "Esta aplicación necesita acceso a tu ubicación para mostrarte " +
                "el clima actual de tu zona. Puedes buscar ubicaciones manualmente " +
                "si prefieres no otorgar este permiso."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Buscar manualmente")
            }
        }
    )
}
```

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreen.kt`

```kotlin
package com.mtzdev.mywheatherapp.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.mtzdev.mywheatherapp.ui.weather.components.LocationPermissionHandler

class WeatherScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<WeatherScreenModel>()
        val state by screenModel.state.collectAsState()

        // Handle permission request
        LocationPermissionHandler(
            onPermissionGranted = {
                screenModel.setEvent(WeatherContract.Event.OnPermissionResult(granted = true))
            },
            onPermissionDenied = {
                screenModel.setEvent(WeatherContract.Event.OnPermissionResult(granted = false))
            },
            onPermanentlyDenied = {
                screenModel.setEvent(WeatherContract.Event.ShowPermissionSettings)
            }
        )

        WeatherContent(
            state = state,
            onEvent = screenModel::setEvent
        )
    }
}

@Composable
private fun WeatherContent(
    state: WeatherContract.State,
    onEvent: (WeatherContract.Event) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando...")
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onEvent(WeatherContract.Event.RetryLastAction) }) {
                        Text("Reintentar")
                    }
                }
                state.weather != null -> {
                    WeatherDisplay(weather = state.weather)
                }
                else -> {
                    Text("Toca 'Usar mi ubicación' para comenzar")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onEvent(WeatherContract.Event.RequestAutoDetection) }) {
                        Text("Usar mi ubicación")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDisplay(weather: com.mtzdev.mywheatherapp.domain.model.Weather) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${weather.cityName}, ${weather.country}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${weather.temperature.toInt()}°C",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = weather.description,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Humedad: ${weather.humidity}%")
                Text("Viento: ${weather.windSpeed} m/s")
                Text("Presión: ${weather.pressure} hPa")
            }
        }
    }
}
```

---

### Step 4: Dependency Injection

Configure Koin modules for the weather feature.

#### 3.4.1 Create WeatherModule.kt

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/di/WeatherModule.kt`

```kotlin
package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.BuildConfig
import com.mtzdev.mywheatherapp.data.location.LocationProvider
import com.mtzdev.mywheatherapp.data.mapper.LocationMapper
import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.data.repository.LocationRepositoryImpl
import com.mtzdev.mywheatherapp.data.repository.WeatherRepositoryImpl
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentWeatherByCoordinatesUseCase
import com.mtzdev.mywheatherapp.domain.usecase.SearchLocationUseCase
import com.mtzdev.mywheatherapp.ui.weather.WeatherScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val weatherModule = module {

    // ========== DATA LAYER ==========

    // Location Provider (Android-specific)
    single {
        LocationProvider(androidContext())
    }

    // Remote Data Sources
    single {
        WeatherRemoteDataSource(
            client = get(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }

    single {
        GeocodingRemoteDataSource(
            client = get(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }

    // Mappers
    singleOf(::WeatherMapper)
    singleOf(::LocationMapper)

    // Repositories
    singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class


    // ========== DOMAIN LAYER ==========

    // Use Cases
    factoryOf(::GetCurrentLocationUseCase)
    factoryOf(::GetCurrentWeatherByCoordinatesUseCase)
    factoryOf(::SearchLocationUseCase)


    // ========== PRESENTATION LAYER ==========

    // ScreenModels
    factoryOf(::WeatherScreenModel)
}
```

#### 3.4.2 Register Module in Application

**File**: `app/src/main/java/com/mtzdev/mywheatherapp/di/AppModule.kt`

Modify the existing AppModule to include weatherModule:

```kotlin
package com.mtzdev.mywheatherapp.di

import org.koin.dsl.module

val appModule = module {
    includes(
        apiModule,
        dataModule,
        viewModelModule,
        weatherModule  // ADD THIS LINE
    )
}
```

---

### Step 5: Testing

Create tests for repositories, use cases, and mappers.

#### 3.5.1 Repository Test Example

**File**: `app/src/test/java/com/mtzdev/mywheatherapp/data/repository/WeatherRepositoryImplTest.kt`

```kotlin
package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.dto.*
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private lateinit var weatherRemoteDataSource: WeatherRemoteDataSource
    private lateinit var weatherMapper: WeatherMapper
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        weatherRemoteDataSource = mockk()
        weatherMapper = WeatherMapper()
        repository = WeatherRepositoryImpl(
            weatherRemoteDataSource = weatherRemoteDataSource,
            weatherMapper = weatherMapper
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCurrentWeatherByCoordinates when api returns success emits weather data`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val weatherDto = createWeatherResponseDto()

        coEvery {
            weatherRemoteDataSource.getCurrentWeather(lat, lon)
        } returns weatherDto

        // When
        val result = repository.getCurrentWeatherByCoordinates(lat, lon)

        // Then
        assertTrue(result is Result.Success)
        val weather = (result as Result.Success).data
        assertEquals(25.5, weather.temperature, 0.01)
        assertEquals("Madrid", weather.cityName)

        coVerify(exactly = 1) {
            weatherRemoteDataSource.getCurrentWeather(lat, lon)
        }
    }

    @Test
    fun `getCurrentWeatherByCoordinates when api throws exception emits error result`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026

        coEvery {
            weatherRemoteDataSource.getCurrentWeather(lat, lon)
        } throws java.net.UnknownHostException()

        // When
        val result = repository.getCurrentWeatherByCoordinates(lat, lon)

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.WeatherError.NoInternetConnection)
    }

    private fun createWeatherResponseDto() = WeatherResponseDto(
        coord = CoordinatesDto(lat = 40.4165, lon = -3.7026),
        weather = listOf(
            WeatherConditionDto(
                id = 800,
                main = "Clear",
                description = "cielo claro",
                icon = "01d"
            )
        ),
        main = MainWeatherDto(
            temp = 25.5,
            feelsLike = 24.8,
            humidity = 60,
            pressure = 1013
        ),
        wind = WindDto(speed = 3.5, deg = 180),
        name = "Madrid",
        sys = SysDto(country = "ES")
    )
}
```

#### 3.5.2 Use Case Test Example

**File**: `app/src/test/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentWeatherByCoordinatesUseCaseTest.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.model.*
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCurrentWeatherByCoordinatesUseCaseTest {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var useCase: GetCurrentWeatherByCoordinatesUseCase

    @Before
    fun setup() {
        weatherRepository = mockk()
        useCase = GetCurrentWeatherByCoordinatesUseCase(weatherRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke when repository returns success emits weather data`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val weather = createWeather()

        coEvery {
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        } returns Result.Success(weather)

        // When
        val result = useCase(lat, lon)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(weather, (result as Result.Success).data)

        coVerify(exactly = 1) {
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        }
    }

    @Test
    fun `invoke when invalid latitude emits validation error`() = runTest {
        // Given
        val invalidLat = 100.0 // Out of range [-90, 90]
        val lon = -3.7026

        // When
        val result = useCase(invalidLat, lon)

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.ValidationError.InvalidCoordinates)

        coVerify(exactly = 0) {
            weatherRepository.getCurrentWeatherByCoordinates(any(), any())
        }
    }

    private fun createWeather() = Weather(
        temperature = 25.5,
        feelsLike = 24.8,
        description = "cielo claro",
        icon = "01d",
        humidity = 60,
        pressure = 1013,
        windSpeed = 3.5,
        windDirection = 180,
        cityName = "Madrid",
        country = "ES"
    )
}
```

#### 3.5.3 Run Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Or run tests for specific package
./gradlew test --tests "com.mtzdev.mywheatherapp.data.*"
./gradlew test --tests "com.mtzdev.mywheatherapp.domain.*"
```

---

## 4. Running the Feature

### 4.1 Build and Install

```bash
# Build the app
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or build and install in one command
./gradlew installDebug
```

### 4.2 Test Location Permissions Flow

1. **First Launch**: App should show initial state
2. **Tap "Usar mi ubicación"**: Permission dialog appears
3. **Grant Permission**: Location is fetched, weather is displayed
4. **Deny Permission**: Error message shown, manual search enabled

### 4.3 Test GPS Auto-Detection

1. Enable GPS on device
2. Grant location permissions
3. Tap "Usar mi ubicación"
4. Verify loading indicator appears
5. Verify weather data displays with correct location

### 4.4 Test Manual City Search

1. Switch to manual mode
2. Enter city name (e.g., "Madrid")
3. Tap search
4. Verify weather data displays for searched city

### 4.5 Test Error Handling

**No Internet**:
1. Disable WiFi and mobile data
2. Try to fetch weather
3. Verify error message: "Sin conexión a internet"

**GPS Disabled**:
1. Disable GPS in device settings
2. Request auto-detection
3. Verify error message about GPS

**Invalid City**:
1. Search for "asdfghjkl"
2. Verify error message: "No encontramos esa ubicación"

---

## 5. Troubleshooting

### 5.1 API Key Not Found

**Error**: `BuildConfig.WEATHER_API_KEY` is empty or build fails

**Solution**:
1. Verify `local.properties` has: `weather_api="your_key"`
2. Sync Gradle: File → Sync Project with Gradle Files
3. Clean and rebuild: `./gradlew clean build`
4. Verify API key format (no extra quotes or spaces)

### 5.2 Google Play Services Not Available

**Error**: Location provider crashes or null

**Solution**:
1. Check device has Google Play Services
2. Update Google Play Services on device
3. Test on real device (not all emulators have GPS)
4. Add fallback logic:
```kotlin
val availability = GoogleApiAvailability.getInstance()
val status = availability.isGooglePlayServicesAvailable(context)
if (status != ConnectionResult.SUCCESS) {
    // Show error or fallback to manual search
}
```

### 5.3 Location Permission Denied

**Error**: Permission permanently denied

**Solution**:
1. Go to device Settings → Apps → MyWeatherApp
2. Grant location permissions manually
3. Or implement "Open Settings" button:
```kotlin
val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
    data = Uri.fromParts("package", context.packageName, null)
}
context.startActivity(intent)
```

### 5.4 Network Errors

**Error**: `UnknownHostException`, `SocketTimeoutException`

**Solution**:
1. Check internet connection
2. Verify API key is valid
3. Test API manually:
```bash
curl "https://api.openweathermap.org/data/2.5/weather?lat=40.4165&lon=-3.7026&appid=YOUR_KEY&units=metric"
```
4. Check Ktor timeout settings in `KtorClientConfigExtensions.kt`

### 5.5 Build Errors

**Error**: Unresolved references or dependency issues

**Solution**:
1. Sync Gradle: `./gradlew --refresh-dependencies`
2. Invalidate caches: File → Invalidate Caches → Invalidate and Restart
3. Clean build: `./gradlew clean build`
4. Check Kotlin version matches (2.1.0)
5. Verify all dependencies are in `libs.versions.toml`

### 5.6 Compose Preview Not Working

**Error**: Preview fails to render

**Solution**:
1. Add `@Preview` annotation with sample data
2. Use `@PreviewParameter` for complex states
3. Ensure composables are `@Composable` and public
4. Rebuild project

---

## 6. Next Steps

### 6.1 Phase 1 Enhancements (Optional)

- Add weather icon display from OpenWeatherMap icons
- Add "feels like" temperature
- Add sunrise/sunset times
- Add weather condition icons (sunny, cloudy, rainy)
- Improve error UI with retry buttons
- Add pull-to-refresh

### 6.2 Phase 2: Offline Support

- Add Room database for caching
- Cache last fetched weather data
- Show cached data when offline
- Add "Last updated" timestamp

### 6.3 Phase 3: UI Improvements

- Implement dark theme
- Add animated weather backgrounds
- Add location search history
- Add favorite locations
- Add hourly forecast

### 6.4 Phase 4: Advanced Features

- Add 7-day weather forecast
- Add weather alerts/notifications
- Add weather widgets
- Add weather maps
- Add multiple location support

---

## 7. Quick Reference

### 7.1 File Structure Tree

```
app/src/main/java/com/mtzdev/mywheatherapp/
├── data/
│   ├── location/
│   │   └── LocationProvider.kt
│   ├── mapper/
│   │   ├── LocationMapper.kt
│   │   └── WeatherMapper.kt
│   ├── remote/
│   │   ├── datasource/
│   │   │   ├── GeocodingRemoteDataSource.kt
│   │   │   └── WeatherRemoteDataSource.kt
│   │   └── dto/
│   │       ├── GeocodingResponseDto.kt
│   │       └── WeatherResponseDto.kt
│   └── repository/
│       ├── LocationRepositoryImpl.kt
│       └── WeatherRepositoryImpl.kt
├── di/
│   ├── AppModule.kt (modified)
│   └── WeatherModule.kt (new)
├── domain/
│   ├── model/
│   │   ├── Coordinates.kt
│   │   ├── DomainError.kt
│   │   ├── Location.kt
│   │   ├── Result.kt
│   │   └── Weather.kt
│   ├── repository/
│   │   ├── LocationRepository.kt
│   │   └── WeatherRepository.kt
│   └── usecase/
│       ├── GetCurrentLocationUseCase.kt
│       ├── GetCurrentWeatherByCoordinatesUseCase.kt
│       └── SearchLocationUseCase.kt
└── ui/
    └── weather/
        ├── WeatherContract.kt
        ├── WeatherScreen.kt
        ├── WeatherScreenModel.kt
        └── components/
            └── LocationPermissionHandler.kt
```

### 7.2 Important Classes and Interfaces

**Domain Layer**:
- `Result<T>`: Sealed class for success/error states
- `DomainError`: Sealed hierarchy of all errors
- `Weather`: Main domain model for weather data
- `Location`: Domain model for location data
- `WeatherRepository`: Interface for weather operations

**Data Layer**:
- `WeatherRemoteDataSource`: Fetches weather from API
- `GeocodingRemoteDataSource`: Converts city name to coordinates
- `LocationProvider`: Gets device GPS location
- `WeatherMapper`: Maps DTO to domain models

**Presentation Layer**:
- `WeatherContract`: MVI contract (State, Event, Effect)
- `WeatherScreenModel`: ViewModel with MVI logic
- `WeatherScreen`: Main Compose UI
- `LocationPermissionHandler`: Permission management

### 7.3 Key Extension Functions

**Result Extensions** (`domain/model/Result.kt`):
```kotlin
fun <T> Result<T>.isSuccess(): Boolean
fun <T> Result<T>.isError(): Boolean
fun <T> Result<T>.getOrNull(): T?
fun <T> Result<T>.errorOrNull(): DomainError?
```

### 7.4 Testing Utilities

**MockK Common Patterns**:
```kotlin
// Mock suspend function
coEvery { repo.getData() } returns Result.Success(data)

// Verify call count
coVerify(exactly = 1) { repo.getData() }

// Mock exception
coEvery { repo.getData() } throws Exception("Error")
```

**Turbine Flow Testing**:
```kotlin
screenModel.state.test {
    val initialState = awaitItem()
    // Trigger event
    val nextState = awaitItem()
    // Assert
    cancelAndIgnoreRemainingEvents()
}
```

### 7.5 OpenWeatherMap API Quick Reference

**Current Weather**:
```
GET https://api.openweathermap.org/data/2.5/weather
?lat={lat}&lon={lon}&appid={key}&units=metric&lang=es
```

**Geocoding**:
```
GET https://api.openweathermap.org/geo/1.0/direct
?q={city}&appid={key}&limit=1
```

**Weather Icons**:
```
https://openweathermap.org/img/wn/{icon}@2x.png
```

### 7.6 Common Commands

```bash
# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Run tests
./gradlew testDebugUnitTest

# Clean
./gradlew clean

# Lint
./gradlew lintDebug

# Check dependencies
./gradlew dependencies
```

---

## Appendix: Implementation Checklist

Use this checklist to track your progress:

### Setup Phase
- [ ] Add dependencies to `libs.versions.toml`
- [ ] Add dependencies to `app/build.gradle.kts`
- [ ] Add permissions to `AndroidManifest.xml`
- [ ] Add API key to `local.properties`
- [ ] Sync Gradle

### Domain Layer
- [ ] Create `Weather.kt` model
- [ ] Create `Location.kt` model
- [ ] Create `Coordinates.kt` model
- [ ] Create `Result.kt` sealed class
- [ ] Create `DomainError.kt` sealed class
- [ ] Create `WeatherRepository.kt` interface
- [ ] Create `LocationRepository.kt` interface
- [ ] Create `GetCurrentWeatherByCoordinatesUseCase.kt`
- [ ] Create `SearchLocationUseCase.kt`
- [ ] Create `GetCurrentLocationUseCase.kt`

### Data Layer
- [ ] Create `WeatherResponseDto.kt` and related DTOs
- [ ] Create `GeocodingResponseDto.kt`
- [ ] Create `WeatherRemoteDataSource.kt`
- [ ] Create `GeocodingRemoteDataSource.kt`
- [ ] Create `LocationProvider.kt`
- [ ] Create `WeatherMapper.kt`
- [ ] Create `LocationMapper.kt`
- [ ] Create `WeatherRepositoryImpl.kt`
- [ ] Create `LocationRepositoryImpl.kt`

### Presentation Layer
- [ ] Create `WeatherContract.kt` (State, Event, Effect)
- [ ] Create `WeatherScreenModel.kt`
- [ ] Create `LocationPermissionHandler.kt`
- [ ] Create `WeatherScreen.kt`
- [ ] Create weather display components

### Dependency Injection
- [ ] Create `WeatherModule.kt`
- [ ] Register `weatherModule` in `AppModule.kt`

### Testing
- [ ] Create `WeatherRepositoryImplTest.kt`
- [ ] Create `LocationRepositoryImplTest.kt`
- [ ] Create `WeatherMapperTest.kt`
- [ ] Create `GetCurrentWeatherByCoordinatesUseCaseTest.kt`
- [ ] Create `SearchLocationUseCaseTest.kt`
- [ ] Run tests and verify passing

### Integration
- [ ] Build and install app
- [ ] Test permission flow
- [ ] Test GPS auto-detection
- [ ] Test manual city search
- [ ] Test error scenarios
- [ ] Test on multiple devices/API levels

---

**Estimated Time**: 2-3 days
**Difficulty**: Intermediate
**Clean Architecture**: ✓
**MVI Pattern**: ✓

Happy coding!
