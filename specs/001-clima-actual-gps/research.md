# Technical Research: Clima Actual con GPS

**Feature**: 001-clima-actual-gps
**Date**: 2025-10-29
**Author**: Technical Research Team
**Status**: Ready for Implementation

---

## Table of Contents

1. [Google Play Services Location](#1-google-play-services-location)
2. [Android Permission Handling](#2-android-permission-handling)
3. [OpenWeatherMap API Integration](#3-openweathermap-api-integration)
4. [Result Pattern](#4-result-pattern)
5. [MVI State Management](#5-mvi-state-management)
6. [Testing Strategy](#6-testing-strategy)
7. [Performance Optimization](#7-performance-optimization)
8. [Error Handling](#8-error-handling)
9. [Koin Dependency Injection Modules](#9-koin-dependency-injection-modules)
10. [Dependencies Summary](#10-dependencies-summary)
11. [Architecture Decision Records](#11-architecture-decision-records)
12. [Risk Mitigation](#12-risk-mitigation)

---

## 1. Google Play Services Location

### 1.1 Decision: FusedLocationProviderClient

**Chosen Solution**: Google Play Services Location API with FusedLocationProviderClient

**Rationale**:
- Official Google API for location services
- Automatically chooses best location provider (GPS, Network, WiFi)
- Battery-efficient with smart location updates
- Well-documented and widely adopted
- Integrates seamlessly with Android framework

**Alternative Considered**: Android LocationManager
- **Rejected**: Deprecated, requires manual provider selection, less accurate, more battery consumption

### 1.2 Implementation Details

#### Dependency
```toml
# gradle/libs.versions.toml
[versions]
playServicesLocation = "21.3.0"

[libraries]
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }
```

#### Core Implementation

```kotlin
// data/location/LocationProvider.kt
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
 * Handles GPS location requests with timeout and error handling.
 */
class LocationProvider(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Gets current device location with 10-second timeout.
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission.
     *
     * @return Coordinates with latitude and longitude
     * @throws LocationException if location cannot be obtained
     * @throws SecurityException if permissions not granted
     */
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

#### Usage Example

```kotlin
// domain/usecase/GetCurrentLocationUseCase.kt
class GetCurrentLocationUseCase(
    private val locationProvider: LocationProvider
) {
    suspend operator fun invoke(): Result<Coordinates> = try {
        val coordinates = locationProvider.getCurrentLocation()
        Result.Success(coordinates)
    } catch (e: SecurityException) {
        Result.Error(LocationError.PermissionDenied)
    } catch (e: LocationException) {
        Result.Error(LocationError.Unavailable(e.message ?: "Unknown error"))
    } catch (e: TimeoutCancellationException) {
        Result.Error(LocationError.Timeout)
    }
}
```

### 1.3 AndroidManifest Configuration

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Location permissions -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Optional: Indicate that GPS is used but not required -->
    <uses-feature
        android:name="android.hardware.location.gps"
        android:required="false" />

    <application>
        <!-- ... -->
    </application>
</manifest>
```

### 1.4 Fallback Strategy

If Google Play Services is not available:
- Detect availability using `GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)`
- Fallback to manual search mode
- Show informative message: "Location detection requires Google Play Services"

---

## 2. Android Permission Handling

### 2.1 Decision: Accompanist Permissions

**Chosen Solution**: Google Accompanist Permissions library with Jetpack Compose

**Rationale**:
- Seamless Compose integration
- Declarative permission requests
- Built-in state management for permission status
- Handles all permission states (granted, denied, rationale needed)
- Maintained by Google

**Alternative Considered**: Manual permission handling with registerForActivityResult
- **Rejected**: More boilerplate, imperative API, harder to test

### 2.2 Implementation Details

#### Dependency
```toml
# gradle/libs.versions.toml
[versions]
accompanist = "0.36.0"

[libraries]
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
```

#### Permission Handler Component

```kotlin
// ui/weather/components/LocationPermissionHandler.kt
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
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Composable that handles location permission requests.
 * Manages permission states and shows appropriate dialogs.
 *
 * @param onPermissionGranted Callback when all permissions are granted
 * @param onPermissionDenied Callback when permissions are denied
 * @param onPermanentlyDenied Callback when permissions are permanently denied
 */
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

    // Handle permission state changes
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        when {
            permissionsState.allPermissionsGranted -> {
                onPermissionGranted()
            }
            permissionsState.shouldShowRationale -> {
                showRationale.value = true
            }
            !permissionsState.shouldShowRationale && permissionsState.permissions.any { !it.status.isGranted } -> {
                // Permissions permanently denied
                onPermanentlyDenied()
            }
        }
    }

    // Rationale dialog
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

#### Usage in Screen

```kotlin
// ui/weather/WeatherScreen.kt
@Composable
fun WeatherScreen(screenModel: WeatherScreenModel) {
    val state by screenModel.state.collectAsState()

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

    // ... rest of UI
}
```

### 2.3 Permission State Flow

```
App Launch
    → Check permission status
    → If UNKNOWN → Show "Use my location" button

User taps "Use my location"
    → Request permissions
    → If DENIED (first time) → Show rationale → Retry
    → If GRANTED → Fetch location → Get weather
    → If PERMANENTLY_DENIED → Show "Go to Settings" message
```

---

## 3. OpenWeatherMap API Integration

### 3.1 Existing Ktor Setup

The project already has Ktor 3.3.0 configured:
- `ktor-client-core`: Core client
- `ktor-client-android`: Android engine
- `ktor-client-content-negotiation`: JSON serialization
- `ktor-client-logging`: Request/response logging
- `ktor-serialization-kotlinx-json`: Kotlinx serialization

**No new HTTP dependencies required** - we will reuse existing Ktor setup.

### 3.2 API Endpoints

#### 3.2.1 Current Weather Data API

**Endpoint**: `https://api.openweathermap.org/data/2.5/weather`

**Request Parameters**:
```kotlin
data class CurrentWeatherRequest(
    val lat: Double,
    val lon: Double,
    val appid: String,        // API key
    val units: String = "metric",  // Celsius
    val lang: String = "es"   // Spanish
)
```

**Response DTO**:
```kotlin
// data/remote/dto/WeatherResponseDto.kt
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

**Example Response**:
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
  "name": "Madrid",
  "sys": {
    "country": "ES"
  }
}
```

#### 3.2.2 Geocoding API

**Endpoint**: `https://api.openweathermap.org/geo/1.0/direct`

**Request Parameters**:
```kotlin
data class GeocodingRequest(
    val q: String,      // City name (e.g., "Madrid", "New York")
    val appid: String,  // API key
    val limit: Int = 1  // Number of results
)
```

**Response DTO**:
```kotlin
// data/remote/dto/GeocodingResponseDto.kt
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

// API returns List<GeocodingResponseDto>
```

**Example Response**:
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

### 3.3 Data Source Implementation

```kotlin
// data/remote/datasource/WeatherRemoteDataSource.kt
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

    /**
     * Fetches current weather data for given coordinates.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return WeatherResponseDto from API
     * @throws ClientRequestException if request fails (4xx)
     * @throws ServerResponseException if server error (5xx)
     */
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

```kotlin
// data/remote/datasource/GeocodingRemoteDataSource.kt
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

    /**
     * Searches for location by city name.
     *
     * @param cityName City name to search
     * @return List of matching locations (limited to 1)
     * @throws ClientRequestException if request fails
     */
    suspend fun searchLocation(cityName: String): List<GeocodingResponseDto> {
        return client.get("https://api.openweathermap.org/geo/1.0/direct") {
            parameter("q", cityName)
            parameter("appid", apiKey)
            parameter("limit", 1)
        }.body()
    }
}
```

### 3.4 Error Handling

```kotlin
// data/remote/ErrorHandler.kt
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode

fun handleApiError(exception: Throwable): WeatherApiError {
    return when (exception) {
        is ClientRequestException -> {
            when (exception.response.status) {
                HttpStatusCode.Unauthorized -> WeatherApiError.InvalidApiKey
                HttpStatusCode.NotFound -> WeatherApiError.LocationNotFound
                HttpStatusCode.TooManyRequests -> WeatherApiError.RateLimitExceeded
                else -> WeatherApiError.ClientError(exception.message)
            }
        }
        is ServerResponseException -> WeatherApiError.ServerError
        is java.net.UnknownHostException -> WeatherApiError.NoInternet
        is java.net.SocketTimeoutException -> WeatherApiError.Timeout
        else -> WeatherApiError.Unknown(exception.message)
    }
}

sealed class WeatherApiError {
    object InvalidApiKey : WeatherApiError()
    object LocationNotFound : WeatherApiError()
    object RateLimitExceeded : WeatherApiError()
    object NoInternet : WeatherApiError()
    object Timeout : WeatherApiError()
    object ServerError : WeatherApiError()
    data class ClientError(val message: String?) : WeatherApiError()
    data class Unknown(val message: String?) : WeatherApiError()
}
```

---

## 4. Result Pattern

### 4.1 Sealed Class Design

**Purpose**: Encapsulate operation results with type-safe success/error states

```kotlin
// domain/model/Result.kt
package com.mtzdev.mywheatherapp.domain.model

/**
 * A generic wrapper for operation results in the domain layer.
 * Represents the three states of an asynchronous operation: Loading, Success, Error.
 *
 * @param T The type of data held by Success state
 */
sealed class Result<out T> {

    /**
     * Represents a loading state (operation in progress).
     */
    data object Loading : Result<Nothing>()

    /**
     * Represents a successful operation with data.
     *
     * @param data The result data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with error information.
     *
     * @param error The error that occurred
     */
    data class Error(val error: DomainError) : Result<Nothing>()
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
 * Extension to get data or null.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Extension to get error or null.
 */
fun <T> Result<T>.errorOrNull(): DomainError? = when (this) {
    is Result.Error -> error
    else -> null
}
```

### 4.2 Domain Error Types

```kotlin
// domain/model/DomainError.kt
package com.mtzdev.mywheatherapp.domain.model

/**
 * Sealed hierarchy of domain-level errors.
 * These are business logic errors, not infrastructure errors.
 */
sealed class DomainError {

    /**
     * Location-related errors.
     */
    sealed class LocationError : DomainError() {
        object PermissionDenied : LocationError()
        object Unavailable : LocationError()
        object Timeout : LocationError()
        object GpsDisabled : LocationError()
    }

    /**
     * Weather API-related errors.
     */
    sealed class WeatherError : DomainError() {
        object CityNotFound : WeatherError()
        object InvalidApiKey : WeatherError()
        object RateLimitExceeded : WeatherError()
        object NoInternetConnection : WeatherError()
        object ServerError : WeatherError()
        data class Unknown(val message: String?) : WeatherError()
    }

    /**
     * Validation errors.
     */
    sealed class ValidationError : DomainError() {
        object EmptyCityName : ValidationError()
        object InvalidCoordinates : ValidationError()
    }
}
```

### 4.3 Usage in Use Cases

```kotlin
// domain/usecase/GetCurrentWeatherByCoordinatesUseCase.kt
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

---

## 5. MVI State Management

### 5.1 StateFlow + Channel Pattern

The project uses **Voyager ScreenModel** with custom MVI base classes:
- `MVIContract`: Base interfaces for State, Event, Effect
- `MVIBaseScreenMode` (note: typo in existing code): Base ScreenModel implementation

**Pattern**:
- **StateFlow**: For UI state (single state holder)
- **SharedFlow (Channel)**: For one-time effects (navigation, snackbars)

### 5.2 Contract Definition

```kotlin
// ui/weather/WeatherContract.kt
package com.mtzdev.mywheatherapp.ui.weather

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.domain.model.Location
import com.mtzdev.mywheatherapp.domain.model.Weather

/**
 * MVI Contract for Weather feature.
 * Defines State, Events, and Effects.
 */
object WeatherContract {

    /**
     * UI State for Weather screen.
     * Implements MVIContract.UiState marker interface.
     */
    data class State(
        val weather: Weather? = null,
        val location: Location? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val locationMode: LocationMode = LocationMode.AUTO,
        val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN,
        val searchQuery: String = ""
    ) : MVIContract.UiState

    /**
     * Location detection mode.
     */
    enum class LocationMode {
        AUTO,    // GPS detection
        MANUAL   // Manual city search
    }

    /**
     * Permission status states.
     */
    enum class PermissionStatus {
        UNKNOWN,              // Not yet requested
        GRANTED,              // Permission granted
        DENIED,               // Permission denied (can retry)
        PERMANENTLY_DENIED    // Permission permanently denied
    }

    /**
     * UI Events (user interactions).
     * Implements MVIContract.UiEvent marker interface.
     */
    sealed interface Event : MVIContract.UiEvent {
        /**
         * User requests automatic location detection.
         */
        data object RequestAutoDetection : Event

        /**
         * User searches for a city by name.
         */
        data class SearchCity(val cityName: String) : Event

        /**
         * User switches between AUTO/MANUAL mode.
         */
        data class SwitchLocationMode(val mode: LocationMode) : Event

        /**
         * Permission request result received.
         */
        data class OnPermissionResult(val granted: Boolean) : Event

        /**
         * User wants to open app settings for permissions.
         */
        data object ShowPermissionSettings : Event

        /**
         * User retries last failed action.
         */
        data object RetryLastAction : Event

        /**
         * User dismisses error message.
         */
        data object ClearError : Event

        /**
         * Search query text changed.
         */
        data class OnSearchQueryChanged(val query: String) : Event
    }

    /**
     * Side Effects (one-time events).
     * Implements MVIContract.Effect marker interface.
     */
    sealed interface Effect : MVIContract.Effect {
        /**
         * Navigate to system settings for app permissions.
         */
        data object NavigateToSettings : Effect

        /**
         * Show snackbar with message.
         */
        data class ShowSnackbar(val message: String) : Effect

        /**
         * Request location permissions.
         */
        data object RequestLocationPermission : Effect
    }
}
```

### 5.3 ScreenModel Implementation

```kotlin
// ui/weather/WeatherScreenModel.kt
package com.mtzdev.mywheatherapp.ui.weather

import cafe.adriel.voyager.core.model.screenModelScope
import com.mtzdev.mywheatherapp.commons.MVIBaseScreenMode
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentWeatherByCoordinatesUseCase
import com.mtzdev.mywheatherapp.domain.usecase.SearchLocationUseCase
import kotlinx.coroutines.launch

/**
 * ScreenModel for Weather screen implementing MVI pattern.
 * Extends MVIBaseScreenMode with State, Event, and Effect.
 */
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

            // Get current location
            when (val locationResult = getCurrentLocationUseCase()) {
                is Result.Success -> {
                    val coordinates = locationResult.data
                    // Fetch weather for coordinates
                    fetchWeatherForCoordinates(coordinates.latitude, coordinates.longitude)
                }
                is Result.Error -> {
                    handleLocationError(locationResult.error)
                }
                is Result.Loading -> { /* Not used in use cases */ }
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
            is Result.Loading -> { /* Not used */ }
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

            // Search location
            when (val locationResult = searchLocationUseCase(cityName)) {
                is Result.Success -> {
                    val location = locationResult.data
                    mutableState.value = state.value.copy(location = location)
                    // Fetch weather
                    fetchWeatherForCoordinates(location.latitude, location.longitude)
                }
                is Result.Error -> {
                    handleSearchError(locationResult.error)
                }
                is Result.Loading -> { /* Not used */ }
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

### 5.4 State Collection in Composable

```kotlin
// ui/weather/WeatherScreen.kt
@Composable
fun WeatherScreen(screenModel: WeatherScreenModel = getScreenModel()) {
    val state by screenModel.state.collectAsState()

    // Collect effects
    LaunchedEffect(Unit) {
        screenModel.effect.collect { effect ->
            when (effect) {
                is WeatherContract.Effect.NavigateToSettings -> {
                    // Open app settings
                }
                is WeatherContract.Effect.ShowSnackbar -> {
                    // Show snackbar
                }
                is WeatherContract.Effect.RequestLocationPermission -> {
                    // Trigger permission request
                }
            }
        }
    }

    // Render UI based on state
    WeatherContent(
        state = state,
        onEvent = screenModel::setEvent
    )
}
```

---

## 6. Testing Strategy

### 6.1 Test Libraries (Already Configured)

- **JUnit 4.13.2**: Test framework
- **MockK 1.14.6**: Mocking library for Kotlin
- **Turbine 1.2.1**: Flow testing utilities
- **Kotlinx-Coroutines-Test 1.10.2**: Coroutine testing support

### 6.2 Data Layer Testing

#### Repository Test Example

```kotlin
// app/src/test/java/com/mtzdev/mywheatherapp/data/repository/WeatherRepositoryImplTest.kt
package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.dto.*
import com.mtzdev.mywheatherapp.domain.model.DomainError
import com.mtzdev.mywheatherapp.domain.model.Result
import io.ktor.client.plugins.ClientRequestException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private lateinit var weatherRemoteDataSource: WeatherRemoteDataSource
    private lateinit var geocodingRemoteDataSource: GeocodingRemoteDataSource
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        weatherRemoteDataSource = mockk()
        geocodingRemoteDataSource = mockk()
        repository = WeatherRepositoryImpl(
            weatherRemoteDataSource = weatherRemoteDataSource,
            geocodingRemoteDataSource = geocodingRemoteDataSource
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

    @Test
    fun `searchLocation when city found emits location data`() = runTest {
        // Given
        val cityName = "Madrid"
        val geocodingDto = createGeocodingResponseDto()

        coEvery {
            geocodingRemoteDataSource.searchLocation(cityName)
        } returns listOf(geocodingDto)

        // When
        val result = repository.searchLocation(cityName)

        // Then
        assertTrue(result is Result.Success)
        val location = (result as Result.Success).data
        assertEquals("Madrid", location.name)
        assertEquals("ES", location.country)
    }

    @Test
    fun `searchLocation when city not found emits error result`() = runTest {
        // Given
        val cityName = "NonExistentCity"

        coEvery {
            geocodingRemoteDataSource.searchLocation(cityName)
        } returns emptyList()

        // When
        val result = repository.searchLocation(cityName)

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.WeatherError.CityNotFound)
    }

    // Helper functions
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

    private fun createGeocodingResponseDto() = GeocodingResponseDto(
        name = "Madrid",
        lat = 40.4165,
        lon = -3.7026,
        country = "ES",
        state = "Madrid"
    )
}
```

#### Mapper Test Example

```kotlin
// app/src/test/java/com/mtzdev/mywheatherapp/data/mapper/WeatherMapperTest.kt
package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.*
import org.junit.Assert.*
import org.junit.Test

class WeatherMapperTest {

    private val mapper = WeatherMapper()

    @Test
    fun `mapDtoToDomain when valid dto returns domain model`() {
        // Given
        val dto = WeatherResponseDto(
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

        // When
        val weather = mapper.mapToDomain(dto)

        // Then
        assertEquals(25.5, weather.temperature, 0.01)
        assertEquals("Madrid", weather.cityName)
        assertEquals("ES", weather.country)
        assertEquals("cielo claro", weather.description)
        assertEquals(60, weather.humidity)
        assertEquals(3.5, weather.windSpeed, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mapDtoToDomain when empty weather list throws exception`() {
        // Given
        val dto = WeatherResponseDto(
            coord = null,
            weather = emptyList(), // Invalid
            main = MainWeatherDto(temp = 25.5, feelsLike = 24.8, humidity = 60, pressure = 1013),
            wind = WindDto(speed = 3.5),
            name = "Madrid",
            sys = SysDto(country = "ES")
        )

        // When/Then - expect exception
        mapper.mapToDomain(dto)
    }
}
```

### 6.3 Domain Layer Testing

#### Use Case Test Example

```kotlin
// app/src/test/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentWeatherByCoordinatesUseCaseTest.kt
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
    fun `invoke when repository returns error emits error`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        val error = DomainError.WeatherError.ServerError

        coEvery {
            weatherRepository.getCurrentWeatherByCoordinates(lat, lon)
        } returns Result.Error(error)

        // When
        val result = useCase(lat, lon)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).error)
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

        // Should not call repository
        coVerify(exactly = 0) {
            weatherRepository.getCurrentWeatherByCoordinates(any(), any())
        }
    }

    @Test
    fun `invoke when invalid longitude emits validation error`() = runTest {
        // Given
        val lat = 40.4165
        val invalidLon = -200.0 // Out of range [-180, 180]

        // When
        val result = useCase(lat, invalidLon)

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is DomainError.ValidationError.InvalidCoordinates)
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

### 6.4 Flow Testing with Turbine

```kotlin
// Example: Testing StateFlow emissions in ScreenModel
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest

class WeatherScreenModelTest {

    @Test
    fun `when RequestAutoDetection event state transitions correctly`() = runTest {
        // Given
        val useCase: GetCurrentLocationUseCase = mockk()
        val screenModel = WeatherScreenModel(useCase, ...)

        coEvery { useCase() } returns Result.Success(coordinates)

        // When/Then
        screenModel.state.test {
            // Initial state
            val initialState = awaitItem()
            assertEquals(false, initialState.isLoading)

            // Send event
            screenModel.setEvent(WeatherContract.Event.RequestAutoDetection)

            // Loading state
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // Success state
            val successState = awaitItem()
            assertEquals(false, successState.isLoading)
            assertNotNull(successState.weather)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 6.5 Coverage Requirements

**Target**: 80%+ coverage for Data and Domain layers

**Exclusions**:
- DTOs (data classes)
- UI layer (tested manually)
- Dependency injection modules

**Measurement**:
```bash
./gradlew testDebugUnitTestCoverage
# Report: app/build/reports/coverage/test/debug/index.html
```

---

## 7. Performance Optimization

### 7.1 Coroutine Dispatchers

**Best Practices**:

```kotlin
// Use appropriate dispatchers for different operations

// Network operations: Dispatchers.IO
suspend fun fetchWeather(): Weather = withContext(Dispatchers.IO) {
    client.get("...").body()
}

// Database operations: Dispatchers.IO
suspend fun saveWeather(weather: Weather) = withContext(Dispatchers.IO) {
    database.weatherDao().insert(weather)
}

// Heavy computations: Dispatchers.Default
suspend fun processLargeData(data: List<Int>) = withContext(Dispatchers.Default) {
    data.map { it * 2 }.sum()
}

// UI updates: Dispatchers.Main (default in ScreenModel scope)
screenModelScope.launch {
    mutableState.value = state.value.copy(weather = newWeather)
}
```

**Implementation in Repository**:

```kotlin
class WeatherRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getCurrentWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<Weather> = withContext(ioDispatcher) {
        try {
            val dto = weatherRemoteDataSource.getCurrentWeather(latitude, longitude)
            val weather = WeatherMapper.mapToDomain(dto)
            Result.Success(weather)
        } catch (e: Exception) {
            Result.Error(handleApiError(e))
        }
    }
}
```

### 7.2 Timeouts

**Network Timeouts** (already configured in Ktor):

```kotlin
// commons/KtorClientConfigExtensions.kt (existing)
fun HttpClientConfig<*>.defaultConfing() {
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000  // 15 seconds
        connectTimeoutMillis = 10_000  // 10 seconds
        socketTimeoutMillis = 15_000
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}
```

**Location Timeout**:

```kotlin
// Already implemented in LocationProvider
companion object {
    private const val LOCATION_TIMEOUT_MS = 10_000L // 10 seconds
}

suspend fun getCurrentLocation(): Coordinates {
    return withTimeout(LOCATION_TIMEOUT_MS) {
        // FusedLocationProviderClient call
    }
}
```

### 7.3 Compose Optimization

#### Stable State Classes

```kotlin
// Use @Immutable for state that never changes
@Immutable
data class Weather(
    val temperature: Double,
    val description: String,
    // ...
)

// Contract state is already immutable (data class with val)
data class State(
    val weather: Weather? = null,
    val isLoading: Boolean = false,
    // ...
) : MVIContract.UiState
```

#### Remember and Keys

```kotlin
@Composable
fun WeatherDisplay(weather: Weather) {
    // Expensive calculation - remember it
    val formattedTemp = remember(weather.temperature) {
        "%.1f°C".format(weather.temperature)
    }

    // Recompose only when weather changes
    Text(formattedTemp)
}
```

#### Lazy Layouts

```kotlin
@Composable
fun WeatherDetailsList(details: List<WeatherDetail>) {
    LazyColumn {
        items(
            items = details,
            key = { it.id } // Stable keys for recomposition
        ) { detail ->
            WeatherDetailItem(detail)
        }
    }
}
```

#### derivedStateOf

```kotlin
@Composable
fun WeatherScreen(screenModel: WeatherScreenModel) {
    val state by screenModel.state.collectAsState()

    // Only recompute when weather changes, not on every state update
    val hasWeatherData by remember {
        derivedStateOf { state.weather != null }
    }

    if (hasWeatherData) {
        WeatherDisplay(state.weather!!)
    }
}
```

### 7.4 Memory Management

**Image Loading** (for weather icons):

```kotlin
// Use Coil for efficient image loading (add if needed)
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://openweathermap.org/img/wn/${weather.icon}@2x.png")
        .crossfade(true)
        .build(),
    contentDescription = weather.description,
    modifier = Modifier.size(64.dp)
)
```

**ScreenModel Lifecycle**:

```kotlin
// ScreenModel is automatically cleared when screen is removed
// Use screenModelScope for coroutines - they're cancelled automatically
class WeatherScreenModel(...) : MVIBaseScreenMode(...) {

    private fun loadWeather() {
        screenModelScope.launch { // Cancelled when screen is removed
            // Network call
        }
    }

    override fun onDispose() {
        // Manual cleanup if needed
        super.onDispose()
    }
}
```

---

## 8. Error Handling

### 8.1 User-Friendly Messages

**Principle**: Every error should provide:
1. **What happened**: Clear description
2. **Why it happened**: Context when helpful
3. **What to do**: Recovery action

### 8.2 Error Message Mapping

```kotlin
// presentation/error/ErrorMessageMapper.kt
package com.mtzdev.mywheatherapp.presentation.error

import com.mtzdev.mywheatherapp.domain.model.DomainError

/**
 * Maps domain errors to user-friendly messages.
 */
object ErrorMessageMapper {

    fun mapToUserMessage(error: DomainError): ErrorMessage {
        return when (error) {
            // Location Errors
            is DomainError.LocationError.PermissionDenied -> ErrorMessage(
                title = "Permiso necesario",
                message = "Necesitamos acceso a tu ubicación para mostrarte el clima de tu zona.",
                action = "Permitir acceso",
                secondaryAction = "Buscar manualmente"
            )

            is DomainError.LocationError.GpsDisabled -> ErrorMessage(
                title = "GPS desactivado",
                message = "Activa el GPS en la configuración de tu dispositivo para detectar tu ubicación.",
                action = "Abrir configuración",
                secondaryAction = "Buscar manualmente"
            )

            is DomainError.LocationError.Timeout -> ErrorMessage(
                title = "No se pudo obtener ubicación",
                message = "La detección de ubicación está tardando mucho. Intenta con búsqueda manual.",
                action = "Reintentar",
                secondaryAction = "Buscar manualmente"
            )

            is DomainError.LocationError.Unavailable -> ErrorMessage(
                title = "Ubicación no disponible",
                message = "No pudimos obtener tu ubicación en este momento. Intenta más tarde o busca manualmente.",
                action = "Reintentar",
                secondaryAction = "Buscar manualmente"
            )

            // Weather API Errors
            is DomainError.WeatherError.CityNotFound -> ErrorMessage(
                title = "Ciudad no encontrada",
                message = "No encontramos esa ubicación. Verifica el nombre e intenta nuevamente.",
                action = "Reintentar",
                secondaryAction = null
            )

            is DomainError.WeatherError.NoInternetConnection -> ErrorMessage(
                title = "Sin conexión",
                message = "Verifica tu conexión a internet e intenta nuevamente.",
                action = "Reintentar",
                secondaryAction = null
            )

            is DomainError.WeatherError.ServerError -> ErrorMessage(
                title = "Error del servidor",
                message = "El servicio no está disponible en este momento. Intenta más tarde.",
                action = "Reintentar",
                secondaryAction = null
            )

            is DomainError.WeatherError.RateLimitExceeded -> ErrorMessage(
                title = "Demasiadas solicitudes",
                message = "Has realizado muchas búsquedas. Espera un momento e intenta nuevamente.",
                action = "Entendido",
                secondaryAction = null
            )

            is DomainError.WeatherError.InvalidApiKey -> ErrorMessage(
                title = "Error de configuración",
                message = "Hay un problema con la configuración de la aplicación.",
                action = "Contactar soporte",
                secondaryAction = null
            )

            is DomainError.WeatherError.Unknown -> ErrorMessage(
                title = "Error inesperado",
                message = error.message ?: "Ocurrió un error. Intenta nuevamente.",
                action = "Reintentar",
                secondaryAction = null
            )

            // Validation Errors
            is DomainError.ValidationError.EmptyCityName -> ErrorMessage(
                title = "Campo vacío",
                message = "Por favor ingresa un nombre de ciudad.",
                action = "Entendido",
                secondaryAction = null
            )

            is DomainError.ValidationError.InvalidCoordinates -> ErrorMessage(
                title = "Coordenadas inválidas",
                message = "Las coordenadas proporcionadas no son válidas.",
                action = "Entendido",
                secondaryAction = null
            )
        }
    }
}

/**
 * Structured error message for UI display.
 */
data class ErrorMessage(
    val title: String,
    val message: String,
    val action: String,
    val secondaryAction: String?
)
```

### 8.3 Error UI Component

```kotlin
// ui/weather/components/ErrorMessage.kt
@Composable
fun ErrorMessageDisplay(
    error: ErrorMessage,
    onActionClick: () -> Unit,
    onSecondaryActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = error.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(error.action)
                }

                if (error.secondaryAction != null && onSecondaryActionClick != null) {
                    OutlinedButton(
                        onClick = onSecondaryActionClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(error.secondaryAction)
                    }
                }
            }
        }
    }
}
```

### 8.4 Error Logging

```kotlin
// commons/Logger.kt
object Logger {

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(tag, message, throwable)
        }
        // In production: send to crash reporting (Firebase Crashlytics, etc.)
    }

    fun logWarning(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, message)
        }
    }

    fun logInfo(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, message)
        }
    }
}

// Usage in repository
try {
    val dto = weatherRemoteDataSource.getCurrentWeather(lat, lon)
    Result.Success(mapper.mapToDomain(dto))
} catch (e: Exception) {
    Logger.logError("WeatherRepository", "Failed to fetch weather", e)
    Result.Error(handleApiError(e))
}
```

---

## 9. Koin Dependency Injection Modules

### 9.1 Module Structure

Following the existing project pattern, we'll create a single `WeatherModule.kt` that will be included in the main `appModule`.

### 9.2 Complete Module Implementation

```kotlin
// di/WeatherModule.kt
package com.mtzdev.mywheatherapp.di

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

/**
 * Koin module for Weather feature.
 * Includes Data, Domain, and Presentation layer dependencies.
 */
val weatherModule = module {

    // ========== DATA LAYER ==========

    // Location Provider (Android-specific)
    single {
        LocationProvider(androidContext())
    }

    // Remote Data Sources (use single - they're stateless singletons)
    singleOf(::WeatherRemoteDataSource) {
        // Inject existing HTTP client and API key from apiModule
        bind<WeatherRemoteDataSource>()
    }

    singleOf(::GeocodingRemoteDataSource) {
        bind<GeocodingRemoteDataSource>()
    }

    // Mappers (use single - pure functions, no state)
    singleOf(::WeatherMapper)
    singleOf(::LocationMapper)

    // Repositories (use single - they coordinate data sources)
    singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class


    // ========== DOMAIN LAYER ==========

    // Use Cases (use factory - create new instance per injection)
    // Rationale: Use cases are lightweight, stateless operations.
    // Factory ensures clean separation and testability.
    factoryOf(::GetCurrentLocationUseCase)
    factoryOf(::GetCurrentWeatherByCoordinatesUseCase)
    factoryOf(::SearchLocationUseCase)


    // ========== PRESENTATION LAYER ==========

    // ScreenModels (use factory - scoped to screen lifecycle)
    // Rationale: Each screen instance gets its own ScreenModel.
    // Voyager manages lifecycle; factory ensures fresh state per screen.
    factoryOf(::WeatherScreenModel)
}
```

### 9.3 Detailed Module Breakdown

#### 9.3.1 Data Module Components

```kotlin
// Data Sources - SINGLE (stateless, reusable)
single {
    WeatherRemoteDataSource(
        client = get(named(WEATHER_HTTP_CLIENT)),  // From existing apiModule
        apiKey = get(named(WEATHER_API_KEY))       // From existing apiModule
    )
}

single {
    GeocodingRemoteDataSource(
        client = get(named(WEATHER_HTTP_CLIENT)),
        apiKey = get(named(WEATHER_API_KEY))
    )
}

// Repositories - SINGLE (coordinate data sources)
singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class

// Equivalent expanded form:
single<WeatherRepository> {
    WeatherRepositoryImpl(
        weatherRemoteDataSource = get(),
        geocodingRemoteDataSource = get(),
        weatherMapper = get(),
        locationMapper = get()
    )
}

// Mappers - SINGLE (pure functions, no state)
singleOf(::WeatherMapper)
singleOf(::LocationMapper)

// Location Provider - SINGLE (manages Android location services)
single { LocationProvider(androidContext()) }
```

**Why `single`?**
- Data sources are stateless and can be reused across the app
- Repositories coordinate data sources and should have single instances
- Mappers are pure functions with no state
- LocationProvider manages system services (should be singleton)

#### 9.3.2 Domain Module Components

```kotlin
// Use Cases - FACTORY (lightweight, stateless operations)
factoryOf(::GetCurrentLocationUseCase)
factoryOf(::GetCurrentWeatherByCoordinatesUseCase)
factoryOf(::SearchLocationUseCase)

// Equivalent expanded form:
factory {
    GetCurrentWeatherByCoordinatesUseCase(
        weatherRepository = get()
    )
}

factory {
    SearchLocationUseCase(
        locationRepository = get()
    )
}

factory {
    GetCurrentLocationUseCase(
        locationProvider = get()
    )
}
```

**Why `factory`?**
- Use cases are lightweight and stateless
- Creating new instances ensures clean separation between callers
- Better testability (each test gets fresh instance)
- No shared state between different consumers

#### 9.3.3 Presentation Module Components

```kotlin
// ScreenModels - FACTORY (scoped to screen lifecycle)
factoryOf(::WeatherScreenModel)

// Equivalent expanded form:
factory {
    WeatherScreenModel(
        getCurrentLocationUseCase = get(),
        getCurrentWeatherByCoordinatesUseCase = get(),
        searchLocationUseCase = get()
    )
}
```

**Why `factory`?**
- Each screen instance should have its own ScreenModel
- Prevents state leaking between screen instances
- Voyager manages ScreenModel lifecycle
- Cleaned up when screen is removed from backstack

### 9.4 Integration with Existing Modules

```kotlin
// di/AppModule.kt (MODIFY EXISTING)
package com.mtzdev.mywheatherapp.di

import org.koin.dsl.module

val appModule = module {
    includes(
        apiModule,      // Existing
        dataModule,     // Existing
        viewModelModule, // Existing
        weatherModule   // NEW - add this line
    )
}
```

### 9.5 Koin Scope Decision Matrix

| Component Type | Scope | Rationale |
|----------------|-------|-----------|
| **HttpClient** | `single` | Expensive to create, manages connection pool |
| **API Key** | `single` | Static configuration value |
| **Data Sources** | `single` | Stateless, reusable |
| **Repositories** | `single` | Coordinate data sources, single source of truth |
| **Mappers** | `single` | Pure functions, no state |
| **LocationProvider** | `single` | Manages Android system services |
| **Use Cases** | `factory` | Lightweight, stateless, better separation |
| **ScreenModels** | `factory` | Scoped to screen lifecycle |

### 9.6 Testing with Koin

```kotlin
// Test example with Koin
class WeatherRepositoryImplTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(testWeatherModule)
    }

    private val repository: WeatherRepository by inject()

    @Test
    fun `test repository behavior`() = runTest {
        // Test implementation
    }
}

// Test module
val testWeatherModule = module {
    single<WeatherRemoteDataSource> { mockk() }
    single<GeocodingRemoteDataSource> { mockk() }
    singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class
}
```

---

## 10. Dependencies Summary

### 10.1 New Dependencies to Add

Add these to `gradle/libs.versions.toml`:

```toml
# gradle/libs.versions.toml

[versions]
playServicesLocation = "21.3.0"
accompanist = "0.36.0"

[libraries]
# Google Play Services - Location
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }

# Accompanist - Permissions (Compose)
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
```

### 10.2 Update app/build.gradle.kts

```kotlin
// app/build.gradle.kts

dependencies {
    // ... existing dependencies ...

    // NEW: Google Play Services Location
    implementation(libs.play.services.location)

    // NEW: Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // ... rest of dependencies ...
}
```

### 10.3 Existing Dependencies (No Changes)

These are already configured and will be reused:

**Networking**:
- `ktor-client-core`: 3.3.0
- `ktor-client-android`: 3.3.0
- `ktor-client-content-negotiation`: 3.3.0
- `ktor-client-logging`: 3.3.0
- `ktor-serialization-kotlinx-json`: 3.3.0

**Dependency Injection**:
- `koin-bom`: 4.1.0
- `koin-core`: (from BOM)
- `koin-android`: (from BOM)

**Navigation**:
- `voyager-navigator`: 1.1.0-beta02
- `voyager-screenmodel`: 1.1.0-beta02
- `voyager-koin`: 1.1.0-beta02

**Testing**:
- `junit`: 4.13.2
- `mockk`: 1.14.6
- `turbine`: 1.2.1
- `kotlinx-coroutines-test`: 1.10.2
- `koin-test`: (from BOM)
- `koin-test-junit4`: (from BOM)

**Compose**:
- `compose-bom`: 2024.09.00
- All Compose libraries from BOM

### 10.4 Complete libs.versions.toml (New Sections Only)

```toml
[versions]
agp = "8.13.0"
kotlin = "2.1.0"
coreKtx = "1.17.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.9.4"
activityCompose = "1.11.0"
composeBom = "2024.09.00"
koin = "4.1.0"
voyager = "1.1.0-beta02"
ktor = "3.3.0"
# NEW
playServicesLocation = "21.3.0"
accompanist = "0.36.0"

[libraries]
# ... existing libraries ...

# NEW: Google Play Services Location
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }

# NEW: Accompanist Permissions
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }

# ... rest of existing libraries ...
```

---

## 11. Architecture Decision Records

### ADR-001: Use FusedLocationProviderClient for GPS

**Status**: Accepted

**Context**:
Need to obtain device location for weather queries. Multiple options available:
- Android LocationManager (legacy)
- FusedLocationProviderClient (Google Play Services)
- Third-party location libraries

**Decision**:
Use FusedLocationProviderClient from Google Play Services Location API.

**Rationale**:
- Official Google recommendation
- Automatically selects best provider (GPS, network, WiFi)
- Battery-efficient with location priority settings
- Well-maintained and documented
- Seamless Android integration

**Consequences**:
- **Positive**: Better accuracy, lower battery consumption, simpler API
- **Negative**: Requires Google Play Services (not available on all devices)
- **Mitigation**: Fallback to manual search if Google Play Services unavailable

---

### ADR-002: Use Accompanist Permissions for Permission Handling

**Status**: Accepted

**Context**:
Need to request location permissions in Compose UI. Options:
- Manual permission handling with registerForActivityResult
- Accompanist Permissions library
- Custom permission utility

**Decision**:
Use Accompanist Permissions library.

**Rationale**:
- Declarative Compose API
- Built-in state management
- Handles all permission states (granted, denied, rationale)
- Maintained by Google
- Reduces boilerplate code

**Consequences**:
- **Positive**: Cleaner code, better Compose integration, less boilerplate
- **Negative**: Additional dependency
- **Trade-off**: Worth it for developer experience and code clarity

---

### ADR-003: Reuse Existing Ktor 3.3.0 Client

**Status**: Accepted

**Context**:
Need HTTP client for OpenWeatherMap API. Project already has Ktor 3.3.0 configured.

**Decision**:
Reuse existing Ktor client configuration from `apiModule`.

**Rationale**:
- Already configured with JSON serialization
- Logging already set up
- Timeout policies defined
- Consistent with project architecture
- Avoids dependency duplication

**Consequences**:
- **Positive**: No new dependencies, consistent API layer, reuse existing config
- **Negative**: None
- **Note**: Weather data sources will inject existing HTTP client via Koin

---

### ADR-004: Use Sealed Class Result Pattern

**Status**: Accepted

**Context**:
Need type-safe way to represent operation results (success/error/loading).

**Decision**:
Implement `sealed class Result<T>` in domain layer.

**Rationale**:
- Type-safe exhaustive when expressions
- Clear separation of success/error states
- Composable and chainable
- Better than exceptions for expected failures
- Aligns with functional programming principles

**Consequences**:
- **Positive**: Type safety, explicit error handling, no exception-based control flow
- **Negative**: More verbose than throwing exceptions
- **Trade-off**: Worth it for robustness and clarity

---

### ADR-005: MVI Pattern with StateFlow + SharedFlow

**Status**: Accepted

**Context**:
Need state management pattern for UI. Project uses Voyager ScreenModel with custom MVI base.

**Decision**:
Continue using existing MVI pattern:
- `StateFlow` for UI state
- `SharedFlow` for one-time effects
- `MVIBaseScreenMode` as base class

**Rationale**:
- Already established in project
- Constitutional requirement
- Clear unidirectional data flow
- Predictable state updates
- Testable

**Consequences**:
- **Positive**: Consistency with existing code, constitutional compliance
- **Negative**: More boilerplate than ViewModel with mutableState
- **Trade-off**: Worth it for architecture consistency

---

### ADR-006: Factory Scope for Use Cases, Single Scope for Repositories

**Status**: Accepted

**Context**:
Need to decide Koin scopes for different layers.

**Decision**:
- **Use Cases**: `factory` scope (new instance per injection)
- **Repositories**: `single` scope (singleton)
- **Data Sources**: `single` scope (singleton)
- **ScreenModels**: `factory` scope (per screen instance)

**Rationale**:
- Use cases are lightweight and stateless → factory ensures clean separation
- Repositories coordinate data sources → single ensures single source of truth
- Data sources are stateless → single for reusability
- ScreenModels are tied to screen lifecycle → factory prevents state leaking

**Consequences**:
- **Positive**: Clear separation, better testability, prevents state sharing issues
- **Negative**: Slightly more memory for factory instances (negligible)
- **Trade-off**: Worth it for correctness and testability

---

### ADR-007: User-Friendly Error Messages with Recovery Actions

**Status**: Accepted

**Context**:
Need to handle errors and present them to users in understandable way.

**Decision**:
Map all domain errors to structured `ErrorMessage` with:
- Title (what happened)
- Message (why it happened, what to do)
- Primary action (recovery)
- Optional secondary action (alternative)

**Rationale**:
- Users need actionable information, not technical errors
- Every error should have recovery path
- Consistent error UX across app
- Separates domain errors from presentation

**Consequences**:
- **Positive**: Better UX, clearer error communication, consistent experience
- **Negative**: Requires mapping layer
- **Trade-off**: Worth it for user experience

---

### ADR-008: 80%+ Test Coverage for Data and Domain Layers

**Status**: Accepted

**Context**:
Need to ensure code quality and reliability.

**Decision**:
Require minimum 80% test coverage for Data and Domain layers.

**Rationale**:
- Data layer contains critical API and mapping logic
- Domain layer contains business rules
- UI layer can be tested manually
- 80% is achievable and meaningful (not 100% for diminishing returns)

**Consequences**:
- **Positive**: Higher confidence in refactoring, catches regression bugs
- **Negative**: More initial development time
- **Trade-off**: Worth it for long-term maintainability

---

## 12. Risk Mitigation

| # | Risk | Probability | Impact | Mitigation Strategy | Contingency Plan |
|---|------|-------------|--------|---------------------|------------------|
| **R-001** | User denies location permissions | **High** (40%) | **Medium** | 1. Show clear rationale dialog explaining why permission is needed<br>2. Provide manual search as alternative<br>3. Allow switching between auto/manual modes | If permanently denied: Guide user to app settings with clear instructions. Ensure manual search works perfectly. |
| **R-002** | GPS disabled or unavailable | **Medium** (25%) | **Medium** | 1. Detect GPS status before requesting location<br>2. Show helpful message to enable GPS<br>3. Provide "Go to Settings" button<br>4. Offer manual search immediately | If GPS unavailable: Default to manual search mode. Consider using network location as fallback (less accurate). |
| **R-003** | Google Play Services not available | **Low** (10%) | **High** | 1. Check GoogleApiAvailability before using location services<br>2. Show informative message<br>3. Direct user to install/update Play Services<br>4. Enable manual search only | If unavailable: Disable GPS features, show clear message, ensure manual search works. Consider fallback to Android LocationManager for basic devices. |
| **R-004** | OpenWeatherMap API rate limit exceeded | **Low** (15%) | **High** | 1. Implement request throttling (max 1 request per 10 seconds)<br>2. Show user-friendly message when limit hit<br>3. Log rate limit errors for monitoring<br>4. Consider caching recent results (future) | If hit: Show clear message "Too many requests", disable search for 60 seconds, show countdown timer. |
| **R-005** | Network connection lost during API call | **Medium** (30%) | **Low** | 1. Set Ktor timeouts (15s request, 10s connect)<br>2. Catch network exceptions (UnknownHostException)<br>3. Show "No internet" message with retry<br>4. Test offline scenarios | If offline: Show persistent message, enable retry button, consider showing last cached result (future enhancement). |
| **R-006** | API returns unexpected/invalid data | **Low** (10%) | **Medium** | 1. Use Kotlinx serialization with ignoreUnknownKeys<br>2. Validate DTOs before mapping to domain<br>3. Use default values in DTO fields<br>4. Log serialization errors<br>5. Comprehensive mapper tests | If invalid: Show generic error message, log full response for debugging, allow retry. |
| **R-007** | Location timeout (>10 seconds) | **Medium** (20%) | **Low** | 1. Set 10-second timeout with withTimeout<br>2. Show helpful message<br>3. Suggest manual search<br>4. Use HIGH_ACCURACY priority | If timeout: Cancel location request, show timeout message, switch to manual mode, suggest checking GPS settings. |
| **R-008** | App crashes due to permission state edge cases | **Low** (5%) | **High** | 1. Thoroughly test all permission states<br>2. Use Accompanist Permissions (handles edge cases)<br>3. Defensive coding in permission handlers<br>4. Test on different Android versions (API 24-34+) | If crash: Comprehensive error boundaries, try-catch in permission code, default to manual mode on any permission error. |
| **R-009** | Memory leak in ScreenModel | **Low** (5%) | **Medium** | 1. Use screenModelScope for all coroutines (auto-cancelled)<br>2. Avoid storing Context references<br>3. Test with LeakCanary<br>4. Clear observers properly | If leak: Review all coroutine scopes, ensure proper lifecycle handling, use WeakReference if needed. |
| **R-010** | Weather icon loading failures | **Low** (10%) | **Low** | 1. Use reliable image loading library (Coil/Glide)<br>2. Provide fallback icon<br>3. Handle 404 gracefully<br>4. Cache icons | If fails: Show default weather icon based on condition text, log missing icons for future fallback set. |
| **R-011** | API key exposure in version control | **Low** (5%) | **Critical** | 1. API key in local.properties (gitignored)<br>2. BuildConfig for injection<br>3. Code review checklist<br>4. Never commit local.properties | If exposed: Immediately revoke API key, generate new one, rotate in all environments, git history cleanup. |
| **R-012** | Incompatibility with specific Android versions | **Low** (10%) | **Medium** | 1. Test on multiple API levels (24, 28, 31, 34)<br>2. Use @RequiresApi annotations<br>3. Provide API-level fallbacks<br>4. minSdk = 24 covers 95%+ devices | If incompatible: Add version checks, provide graceful degradation, document minimum version requirements clearly. |

### Risk Priority Matrix

```
Impact
  ^
H |  R-003, R-008       R-011
I |  R-006              R-004
G |
H |
  |
M |  R-001, R-002       R-009, R-012
E |  R-007, R-010
D |
I |
U |
M |
  |
L |  R-005
O |
W |
  +---------------------->
    Low    Medium    High
        Probability
```

### Top 5 Risks (Prioritized)

1. **R-011**: API key exposure (Critical impact) - Prevention through code review
2. **R-003**: Google Play Services unavailable (High impact) - Mitigation through fallback
3. **R-004**: API rate limit (High impact) - Mitigation through throttling
4. **R-001**: Permission denial (Common) - Mitigation through manual search
5. **R-005**: Network issues (Common) - Mitigation through error handling

---

## Summary

This research document provides comprehensive technical specifications for implementing the GPS-based weather feature. Key highlights:

1. **Location**: FusedLocationProviderClient with 10-second timeout and fallback to manual search
2. **Permissions**: Accompanist Permissions with declarative Compose API
3. **API**: Reuse existing Ktor 3.3.0 client for OpenWeatherMap Current Weather + Geocoding APIs
4. **Architecture**: Result pattern with sealed classes, MVI state management with StateFlow/SharedFlow
5. **DI**: Koin modules with proper scopes (single for repositories, factory for use cases/ScreenModels)
6. **Testing**: 80%+ coverage using MockK, JUnit4, Turbine, and Coroutines-Test
7. **Performance**: Proper dispatchers, timeouts, and Compose optimization
8. **Errors**: User-friendly messages with recovery actions for all error scenarios

**Dependencies to Add**:
- `play-services-location`: 21.3.0
- `accompanist-permissions`: 0.36.0

All decisions are documented in ADRs and risks are identified with mitigation strategies. Implementation can proceed following this research.

---

**Next Steps**: Proceed to Phase 1 (Data Model & Contracts) → `/speckit.plan` command
