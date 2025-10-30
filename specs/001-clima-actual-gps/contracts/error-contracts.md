# Error Contracts Documentation

**Project**: MyWeatherApp
**Feature**: Clima Actual con GPS
**Version**: 1.0.0
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Overview](#overview)
2. [Error Hierarchy](#error-hierarchy)
3. [Error Types](#error-types)
4. [Error Factory Pattern](#error-factory-pattern)
5. [Error Message Mapping](#error-message-mapping)
6. [Recovery Strategies](#recovery-strategies)
7. [Logging Requirements](#logging-requirements)
8. [Testing Error Handling](#testing-error-handling)

---

## Overview

This document defines the complete error handling strategy for the MyWeatherApp application. It covers error types, mapping strategies, user-facing messages, and recovery mechanisms.

### Error Handling Philosophy

1. **User-Friendly**: Technical errors are translated to actionable user messages
2. **Typed Errors**: Use sealed classes for compile-time safety
3. **Centralized Mapping**: Factory pattern for consistent error handling
4. **Graceful Degradation**: Always provide recovery options
5. **Comprehensive Logging**: All errors are logged for debugging

### Error Flow

```
┌─────────────────────────────────────────────────────┐
│            Data Layer                               │
│  ┌──────────────────────────────────┐              │
│  │  Network/API Exceptions          │              │
│  │  - UnknownHostException          │              │
│  │  - HttpRequestTimeoutException   │              │
│  │  - ClientRequestException (4xx)  │              │
│  │  - ServerResponseException (5xx) │              │
│  └──────────────┬───────────────────┘              │
│                 │                                    │
│                 ▼                                    │
│  ┌──────────────────────────────────┐              │
│  │   FactoryWeatherException        │              │
│  │   (Maps to Domain Exceptions)    │              │
│  └──────────────┬───────────────────┘              │
└─────────────────┼───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│            Domain Layer                             │
│  ┌──────────────────────────────────┐              │
│  │   WeatherException Hierarchy     │              │
│  │   - WeatherConectionException    │              │
│  │   - WeatherServiceException      │              │
│  │   - WeatherUnknowGeoDataException│              │
│  │   - WeatherUnknowException       │              │
│  └──────────────┬───────────────────┘              │
└─────────────────┼───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│         Presentation Layer                          │
│  ┌──────────────────────────────────┐              │
│  │   User-Friendly Messages         │              │
│  │   + Recovery Actions             │              │
│  └──────────────────────────────────┘              │
└─────────────────────────────────────────────────────┘
```

---

## Error Hierarchy

### Base Exception Class

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/WeatherException.kt`

```kotlin
package com.mtzdev.mywheatherapp.commons

/**
 * Base exception class for all weather-related errors.
 *
 * This is the root of the exception hierarchy for the weather domain.
 * All domain-specific exceptions MUST extend this class.
 *
 * ## Design Principles
 * - Open class to allow inheritance
 * - Extends Exception (checked exception semantics)
 * - Provides meaningful error messages
 * - Used in domain and presentation layers only
 *
 * @property message Human-readable error message
 *
 * @since 1.0.0
 */
open class WeatherException(message: String) : Exception(message)
```

### Exception Hierarchy Diagram

```
                   Exception (Kotlin/Java)
                          │
                          │
                    WeatherException
                    (Base Domain Error)
                          │
         ┌────────────────┼────────────────┬─────────────────┐
         │                │                │                 │
         ▼                ▼                ▼                 ▼
WeatherConection    WeatherService   WeatherUnknow    WeatherUnknow
   Exception          Exception        Exception      GeoDataException

   (Network/          (API/Server     (Unexpected      (City Not
   Connectivity        Errors)          Errors)         Found)
    Errors)
```

---

## Error Types

### 1. WeatherConectionException

**Purpose**: Network connectivity and timeout errors

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/WeatherException.kt`

```kotlin
/**
 * Exception for network connectivity issues.
 *
 * ## When to Use
 * - No internet connection
 * - Request timeout
 * - DNS resolution failure
 * - Socket connection failure
 *
 * ## Mapped From
 * - UnknownHostException
 * - ConnectException
 * - HttpRequestTimeoutException
 * - SocketTimeoutException
 *
 * ## User Message
 * "Error de conexión. Verifica tu internet"
 *
 * ## Recovery Strategy
 * - Check internet connection
 * - Retry request
 * - Use cached data (if available)
 *
 * @property message Error message describing the connection issue
 *
 * @since 1.0.0
 */
class WeatherConectionException(message: String) : WeatherException(message)
```

**Usage Examples**:

```kotlin
// From factory
when (error) {
    is UnknownHostException -> WeatherConectionException("Error de conexión")
    is HttpRequestTimeoutException -> WeatherConectionException("Error de conexión")
}

// Manual creation
throw WeatherConectionException("No se pudo conectar al servidor")
```

**Common Scenarios**:
- User has no internet connection
- API server is unreachable
- Request takes longer than timeout (10 seconds)
- DNS lookup fails

### 2. WeatherServiceException

**Purpose**: API and server-side errors

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/WeatherException.kt`

```kotlin
/**
 * Exception for API and service errors.
 *
 * ## When to Use
 * - API returns 4xx error (client error)
 * - API returns 5xx error (server error)
 * - Invalid API response format
 * - API key authentication failure
 * - Rate limit exceeded
 *
 * ## Mapped From
 * - ClientRequestException (4xx)
 * - ServerResponseException (5xx)
 * - SerializationException
 * - Generic API errors
 *
 * ## User Message
 * "Error de servicio. Intenta nuevamente"
 *
 * ## Recovery Strategy
 * - Retry request (for 5xx errors)
 * - Check API configuration (for 4xx errors)
 * - Wait before retry (for rate limiting)
 *
 * @property message Error message describing the service issue
 *
 * @since 1.0.0
 */
class WeatherServiceException(message: String) : WeatherException(message)
```

**Usage Examples**:

```kotlin
// 401 Unauthorized
WeatherServiceException("API key inválida")

// 429 Too Many Requests
WeatherServiceException("Límite de solicitudes excedido")

// 500 Internal Server Error
WeatherServiceException("Error de servicio")
```

**Common Scenarios**:
- Invalid or missing API key (401)
- Rate limit exceeded (429)
- Server internal error (500)
- Service temporarily unavailable (503)
- Malformed API response

### 3. WeatherUnknowGeoDataException

**Purpose**: Location not found errors

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/WeatherException.kt`

```kotlin
/**
 * Exception for unknown or invalid geographic location.
 *
 * ## When to Use
 * - City name not found in geocoding API
 * - Geocoding API returns empty results
 * - Invalid city name format
 *
 * ## Mapped From
 * - Empty geocoding API response
 * - Invalid location data
 *
 * ## User Message
 * "Ciudad no encontrada. Verifica el nombre"
 *
 * ## Recovery Strategy
 * - Suggest correct city name spelling
 * - Offer location alternatives
 * - Allow GPS location instead
 *
 * @property message Error message describing the location issue
 *
 * @since 1.0.0
 */
class WeatherUnknowGeoDataException(message: String) : WeatherException(message)
```

**Usage Examples**:

```kotlin
// Empty geocoding results
if (geoResults.isEmpty()) {
    throw WeatherUnknowGeoDataException("Ciudad '$cityName' no encontrada")
}

// Invalid coordinates
if (lat == null || lon == null) {
    throw WeatherUnknowGeoDataException("Coordenadas inválidas")
}
```

**Common Scenarios**:
- User searches for non-existent city
- Typo in city name
- Geocoding API returns no results
- Missing coordinates in API response

### 4. WeatherUnknowException

**Purpose**: Unexpected and unclassified errors

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/WeatherException.kt`

```kotlin
/**
 * Exception for unexpected or unknown errors.
 *
 * ## When to Use
 * - Errors that don't fit other categories
 * - Unexpected exceptions during processing
 * - Fallback for unhandled errors
 *
 * ## Mapped From
 * - Any exception not matching other patterns
 * - Unexpected runtime errors
 *
 * ## User Message
 * "Ocurrió un error inesperado"
 *
 * ## Recovery Strategy
 * - Log full error details
 * - Report to crash analytics
 * - Offer generic retry option
 *
 * @property message Error message describing the unknown issue
 *
 * @since 1.0.0
 */
class WeatherUnknowException(message: String) : WeatherException(message)
```

**Usage Examples**:

```kotlin
// Catch-all for unknown errors
catch (e: Exception) {
    throw WeatherUnknowException("Error inesperado: ${e.message}")
}

// Unexpected state
if (unexpectedCondition) {
    throw WeatherUnknowException("Estado inesperado")
}
```

**Common Scenarios**:
- Unexpected runtime exceptions
- Programming errors
- Edge cases not covered by other exceptions
- Unknown API response formats

---

## Error Factory Pattern

### FactoryWeatherException

**Purpose**: Centralized exception mapping from data layer to domain layer

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/commons/FactoryWeatherException.kt`

```kotlin
package com.mtzdev.mywheatherapp.commons

import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Factory for creating domain-specific weather exceptions.
 *
 * This factory maps data layer exceptions (network, API) to domain layer
 * exceptions. It provides centralized error handling and consistent error
 * messages across the application.
 *
 * ## Responsibilities
 * - Map network exceptions to WeatherConectionException
 * - Map API exceptions to WeatherServiceException
 * - Provide fallback for unknown exceptions
 * - Generate user-friendly error messages
 *
 * ## Usage
 * ```kotlin
 * class WeatherRepositoryImpl(
 *     private val errorFactory: FactoryWeatherException
 * ) {
 *     suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
 *         return try {
 *             // ... API call
 *         } catch (e: Exception) {
 *             val domainError = errorFactory.create(e)
 *             WeatherDataResult.Error(domainError)
 *         }
 *     }
 * }
 * ```
 *
 * ## Design Pattern
 * Simple Factory Pattern - creates domain exceptions based on input type
 *
 * @since 1.0.0
 */
class FactoryWeatherException {

    /**
     * Creates a domain-specific exception from a generic exception.
     *
     * ## Mapping Rules
     * - Network errors → WeatherConectionException
     * - API errors → WeatherServiceException (default)
     * - Unknown errors → WeatherServiceException (fallback)
     *
     * @param error The original exception from data layer
     * @return A domain-specific WeatherException
     *
     * @since 1.0.0
     */
    fun create(error: Exception): WeatherException {
        return when (error) {
            // Network connectivity errors
            is UnknownHostException,
            is ConnectException,
            is HttpRequestTimeoutException -> {
                WeatherConectionException("Error de conexión")
            }

            // All other errors (including API errors)
            else -> {
                WeatherServiceException("Error de servicio")
            }
        }
    }
}
```

### Enhanced Factory with Detailed Mapping

**Recommended Enhancement** (not currently implemented):

```kotlin
class FactoryWeatherException {

    fun create(error: Exception): WeatherException {
        return when (error) {
            // Network connectivity errors
            is UnknownHostException ->
                WeatherConectionException("Sin conexión a internet")

            is ConnectException ->
                WeatherConectionException("No se pudo conectar al servidor")

            is HttpRequestTimeoutException ->
                WeatherConectionException("La solicitud tardó demasiado")

            is SocketTimeoutException ->
                WeatherConectionException("Tiempo de espera agotado")

            // HTTP client errors (4xx)
            is ClientRequestException -> when (error.response.status.value) {
                400 -> WeatherServiceException("Solicitud inválida")
                401 -> WeatherServiceException("API key inválida")
                403 -> WeatherServiceException("Acceso denegado")
                404 -> WeatherServiceException("Recurso no encontrado")
                429 -> WeatherServiceException("Límite de solicitudes excedido")
                else -> WeatherServiceException("Error en la solicitud")
            }

            // HTTP server errors (5xx)
            is ServerResponseException -> when (error.response.status.value) {
                500 -> WeatherServiceException("Error interno del servidor")
                502 -> WeatherServiceException("Servidor no disponible")
                503 -> WeatherServiceException("Servicio temporalmente no disponible")
                504 -> WeatherServiceException("Tiempo de espera del servidor agotado")
                else -> WeatherServiceException("Error del servidor")
            }

            // Serialization errors
            is SerializationException ->
                WeatherServiceException("Error al procesar respuesta")

            // Validation errors
            is IllegalArgumentException ->
                WeatherException(error.message ?: "Parámetros inválidos")

            // Unknown errors
            else ->
                WeatherUnknowException("Error inesperado: ${error.message}")
        }
    }
}
```

### Dependency Injection

**Koin Module**:

```kotlin
val commonModule = module {
    single { FactoryWeatherException() }
}
```

**Usage in Repository**:

```kotlin
class WeatherDataRepositoryData(
    private val remoteDataSource: WeatherDataDataSource,
    private val errorFactory: FactoryWeatherException
) : WeatherDataRepository {

    override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
        return try {
            val response = remoteDataSource.getWeatherData(lat, lon)
            WeatherDataResult.Data(response.toEntity())
        } catch (e: Exception) {
            val domainError = errorFactory.create(e)
            WeatherDataResult.Error(domainError)
        }
    }
}
```

---

## Error Message Mapping

### Technical to User-Friendly Messages

| Domain Exception | Technical Message | User-Friendly Message (Spanish) | User-Friendly Message (English) |
|------------------|-------------------|----------------------------------|----------------------------------|
| `WeatherConectionException` | "UnknownHostException" | "Error de conexión. Verifica tu internet" | "Connection error. Check your internet" |
| `WeatherConectionException` | "Request timeout" | "La solicitud tardó demasiado. Intenta nuevamente" | "Request timed out. Try again" |
| `WeatherServiceException` | "401 Unauthorized" | "Error de autenticación. Contacta soporte" | "Authentication error. Contact support" |
| `WeatherServiceException` | "429 Too Many Requests" | "Demasiadas solicitudes. Espera un momento" | "Too many requests. Wait a moment" |
| `WeatherServiceException` | "500 Internal Server Error" | "El servicio no está disponible. Intenta más tarde" | "Service unavailable. Try later" |
| `WeatherUnknowGeoDataException` | "Empty results" | "Ciudad no encontrada. Verifica el nombre" | "City not found. Check the name" |
| `WeatherUnknowException` | "Unexpected error" | "Ocurrió un error inesperado" | "An unexpected error occurred" |

### Message Provider Pattern

**Recommended Implementation**:

```kotlin
object ErrorMessageProvider {

    fun getMessage(error: WeatherException): String {
        return when (error) {
            is WeatherConectionException ->
                "Error de conexión. Verifica tu internet y vuelve a intentar."

            is WeatherServiceException ->
                "El servicio no está disponible en este momento. Por favor, intenta más tarde."

            is WeatherUnknowGeoDataException ->
                "No pudimos encontrar esa ubicación. Verifica el nombre de la ciudad."

            is WeatherUnknowException ->
                "Ocurrió un error inesperado. Por favor, intenta nuevamente."

            else ->
                error.message ?: "Error desconocido"
        }
    }

    fun getActionableMessage(error: WeatherException): Pair<String, String> {
        return when (error) {
            is WeatherConectionException ->
                Pair(
                    "Sin conexión a internet",
                    "Verifica tu conexión y toca 'Reintentar'"
                )

            is WeatherServiceException ->
                Pair(
                    "Servicio no disponible",
                    "El problema es temporal. Intenta en unos minutos."
                )

            is WeatherUnknowGeoDataException ->
                Pair(
                    "Ubicación no encontrada",
                    "Prueba con el nombre completo de la ciudad o usa tu ubicación GPS."
                )

            else ->
                Pair(
                    "Error",
                    "Algo salió mal. Toca 'Reintentar' para intentar nuevamente."
                )
        }
    }
}
```

---

## Recovery Strategies

### Recovery Matrix

| Error Type | Automatic Retry | User Action | Fallback Strategy | Cache Usage |
|------------|----------------|-------------|-------------------|-------------|
| `WeatherConectionException` | Yes (3x with backoff) | "Reintentar" button | Show cached data | Yes |
| `WeatherServiceException` | Yes (1x, 5xx only) | "Reintentar" button | Show error screen | Optional |
| `WeatherUnknowGeoDataException` | No | "Buscar nuevamente" | Suggest GPS mode | No |
| `WeatherUnknowException` | No | "Reintentar" button | Show error screen | No |

### Retry Strategy Implementation

**Exponential Backoff**:

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    maxDelay: Long = 10000L,
    factor: Double = 2.0,
    shouldRetry: (Exception) -> Boolean = { it is WeatherConectionException },
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    var lastException: Exception? = null

    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e

            if (shouldRetry(e) && attempt < maxRetries - 1) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            } else {
                throw e
            }
        }
    }

    throw lastException!!
}
```

**Usage**:

```kotlin
suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
    return try {
        retryWithExponentialBackoff {
            val response = remoteDataSource.fetchWeather(lat, lon)
            WeatherDataResult.Data(response.toEntity())
        }
    } catch (e: Exception) {
        val domainError = errorFactory.create(e)
        WeatherDataResult.Error(domainError)
    }
}
```

### Cache Fallback Strategy

```kotlin
class WeatherRepositoryWithCache(
    private val remoteDataSource: WeatherDataDataSource,
    private val cache: WeatherCache,
    private val errorFactory: FactoryWeatherException
) : WeatherDataRepository {

    override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
        return try {
            // Try to fetch from network
            val response = remoteDataSource.fetchWeather(lat, lon)
            val entity = response.toEntity()

            // Update cache on success
            cache.store(lat, lon, entity)

            WeatherDataResult.Data(entity)

        } catch (e: Exception) {
            val domainError = errorFactory.create(e)

            // Fallback to cache on connection error
            if (domainError is WeatherConectionException) {
                val cachedData = cache.get(lat, lon)
                if (cachedData != null) {
                    return WeatherDataResult.Data(cachedData)
                }
            }

            // No cache available, return error
            WeatherDataResult.Error(domainError)
        }
    }
}
```

### User-Facing Recovery Actions

**UI Components**:

```kotlin
@Composable
fun ErrorScreen(
    error: WeatherException,
    onRetry: () -> Unit,
    onUseGPS: () -> Unit = {}
) {
    val (title, message) = ErrorMessageProvider.getActionableMessage(error)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (error) {
                is WeatherConectionException -> Icons.Default.CloudOff
                is WeatherUnknowGeoDataException -> Icons.Default.LocationOff
                else -> Icons.Default.Error
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }

        // Show GPS option for location errors
        if (error is WeatherUnknowGeoDataException) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onUseGPS) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usar mi ubicación")
            }
        }
    }
}
```

---

## Logging Requirements

### Logging Levels

| Error Type | Log Level | Include Stack Trace | Report to Analytics |
|------------|-----------|---------------------|---------------------|
| `WeatherConectionException` | WARNING | No | Yes (count only) |
| `WeatherServiceException` | ERROR | Yes | Yes (full details) |
| `WeatherUnknowGeoDataException` | INFO | No | Yes (search terms) |
| `WeatherUnknowException` | ERROR | Yes | Yes (full details) |

### Logging Implementation

```kotlin
class WeatherDataRepositoryData(
    private val remoteDataSource: WeatherDataDataSource,
    private val errorFactory: FactoryWeatherException
) : WeatherDataRepository {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
        return try {
            Log.d(TAG, "Fetching weather data for ($lat, $lon)")
            val response = remoteDataSource.fetchWeather(lat, lon)
            Log.d(TAG, "Weather data fetched successfully")
            WeatherDataResult.Data(response.toEntity())

        } catch (e: Exception) {
            val domainError = errorFactory.create(e)

            // Log based on error type
            when (domainError) {
                is WeatherConectionException -> {
                    Log.w(TAG, "Connection error: ${domainError.message}")
                }
                is WeatherServiceException -> {
                    Log.e(TAG, "Service error: ${domainError.message}", e)
                }
                is WeatherUnknowException -> {
                    Log.e(TAG, "Unknown error: ${domainError.message}", e)
                }
                else -> {
                    Log.e(TAG, "Error: ${domainError.message}", e)
                }
            }

            WeatherDataResult.Error(domainError)
        }
    }
}
```

### Analytics Tracking

```kotlin
object ErrorAnalytics {

    fun trackError(error: WeatherException, context: String) {
        val errorType = when (error) {
            is WeatherConectionException -> "connection_error"
            is WeatherServiceException -> "service_error"
            is WeatherUnknowGeoDataException -> "location_not_found"
            is WeatherUnknowException -> "unknown_error"
            else -> "other_error"
        }

        // Track to analytics (e.g., Firebase Analytics)
        Analytics.logEvent("error_occurred") {
            param("error_type", errorType)
            param("error_message", error.message ?: "")
            param("context", context)
        }
    }
}

// Usage
catch (e: Exception) {
    val domainError = errorFactory.create(e)
    ErrorAnalytics.trackError(domainError, "weather_fetch")
    WeatherDataResult.Error(domainError)
}
```

---

## Testing Error Handling

### Unit Tests for Error Factory

```kotlin
class FactoryWeatherExceptionTest {

    private lateinit var factory: FactoryWeatherException

    @Before
    fun setup() {
        factory = FactoryWeatherException()
    }

    @Test
    fun `create returns WeatherConectionException for UnknownHostException`() {
        val exception = UnknownHostException("api.openweathermap.org")

        val result = factory.create(exception)

        assertTrue(result is WeatherConectionException)
        assertEquals("Error de conexión", result.message)
    }

    @Test
    fun `create returns WeatherConectionException for HttpRequestTimeoutException`() {
        val exception = HttpRequestTimeoutException(/* ... */)

        val result = factory.create(exception)

        assertTrue(result is WeatherConectionException)
    }

    @Test
    fun `create returns WeatherServiceException for generic exceptions`() {
        val exception = Exception("Something went wrong")

        val result = factory.create(exception)

        assertTrue(result is WeatherServiceException)
        assertEquals("Error de servicio", result.message)
    }
}
```

### Integration Tests for Error Handling

```kotlin
class WeatherRepositoryErrorHandlingTest {

    private lateinit var repository: WeatherDataRepository
    private lateinit var mockDataSource: WeatherDataDataSource
    private lateinit var errorFactory: FactoryWeatherException

    @Before
    fun setup() {
        mockDataSource = mockk()
        errorFactory = FactoryWeatherException()
        repository = WeatherDataRepositoryData(mockDataSource, errorFactory)
    }

    @Test
    fun `getWeatherData returns connection error when network unavailable`() = runTest {
        coEvery { mockDataSource.getWeatherData(any(), any()) } throws
            UnknownHostException()

        val result = repository.getWeatherData(40.0, -3.0)

        assertTrue(result is WeatherDataResult.Error)
        assertTrue((result as WeatherDataResult.Error).error is WeatherConectionException)
    }

    @Test
    fun `getWeatherData returns service error on API failure`() = runTest {
        coEvery { mockDataSource.getWeatherData(any(), any()) } throws
            Exception("API Error")

        val result = repository.getWeatherData(40.0, -3.0)

        assertTrue(result is WeatherDataResult.Error)
        assertTrue((result as WeatherDataResult.Error).error is WeatherServiceException)
    }
}
```

### UI Tests for Error Display

```kotlin
@Test
fun `error screen shows connection error message`() {
    val error = WeatherConectionException("Error de conexión")

    composeTestRule.setContent {
        ErrorScreen(
            error = error,
            onRetry = {}
        )
    }

    composeTestRule
        .onNodeWithText("Error de conexión")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText("Reintentar")
        .assertIsDisplayed()
}

@Test
fun `error screen shows location error with GPS option`() {
    val error = WeatherUnknowGeoDataException("Ciudad no encontrada")

    composeTestRule.setContent {
        ErrorScreen(
            error = error,
            onRetry = {},
            onUseGPS = {}
        )
    }

    composeTestRule
        .onNodeWithText("Usar mi ubicación")
        .assertIsDisplayed()
}
```

---

## Best Practices

### Error Handling DO's ✅

1. **Always use typed exceptions**
   ```kotlin
   // ✅ GOOD
   throw WeatherConectionException("Network error")

   // ❌ BAD
   throw Exception("Network error")
   ```

2. **Map exceptions at repository boundary**
   ```kotlin
   // ✅ GOOD - Map in repository
   catch (e: Exception) {
       val domainError = errorFactory.create(e)
       return Result.Error(domainError)
   }

   // ❌ BAD - Throw raw exceptions
   catch (e: Exception) {
       throw e
   }
   ```

3. **Provide user-friendly messages**
   ```kotlin
   // ✅ GOOD
   WeatherConectionException("Error de conexión. Verifica tu internet")

   // ❌ BAD
   WeatherConectionException("UnknownHostException at line 42")
   ```

4. **Log all errors appropriately**
   ```kotlin
   // ✅ GOOD
   catch (e: Exception) {
       Log.e(TAG, "Failed to fetch weather", e)
       val domainError = errorFactory.create(e)
       return Result.Error(domainError)
   }
   ```

5. **Offer recovery actions**
   ```kotlin
   // ✅ GOOD - Show retry button
   if (state.error != null) {
       ErrorScreen(error = state.error, onRetry = { ... })
   }
   ```

### Error Handling DON'Ts ❌

1. **Don't swallow exceptions**
   ```kotlin
   // ❌ BAD
   try {
       fetchWeather()
   } catch (e: Exception) {
       // Silent failure
   }
   ```

2. **Don't expose technical details to users**
   ```kotlin
   // ❌ BAD
   Text("Error: java.net.UnknownHostException: api.openweathermap.org")

   // ✅ GOOD
   Text("No pudimos conectarnos. Verifica tu internet.")
   ```

3. **Don't use generic exceptions**
   ```kotlin
   // ❌ BAD
   throw Exception("Error")

   // ✅ GOOD
   throw WeatherServiceException("Error de servicio")
   ```

4. **Don't retry indefinitely**
   ```kotlin
   // ❌ BAD
   while (true) {
       try { fetchWeather(); break }
       catch (e: Exception) { /* retry forever */ }
   }

   // ✅ GOOD
   retryWithExponentialBackoff(maxRetries = 3) {
       fetchWeather()
   }
   ```

---

## Appendix: Error Handling Checklist

### Implementation Checklist

- [ ] All domain exceptions extend `WeatherException`
- [ ] Error factory maps all network exceptions
- [ ] Error factory maps all API exceptions (4xx, 5xx)
- [ ] User-friendly messages for all error types
- [ ] Retry strategy implemented for transient errors
- [ ] Cache fallback for connection errors
- [ ] All errors are logged appropriately
- [ ] Error analytics tracking implemented
- [ ] UI displays error messages clearly
- [ ] UI provides recovery actions (retry, alternatives)
- [ ] Unit tests for error factory (90%+ coverage)
- [ ] Integration tests for error handling
- [ ] UI tests for error display

### Error Scenario Checklist

- [ ] No internet connection → Show connection error + retry
- [ ] API timeout → Show timeout error + retry
- [ ] Invalid API key (401) → Show auth error + contact support
- [ ] Rate limit (429) → Show limit error + wait message
- [ ] Server error (5xx) → Show service error + retry later
- [ ] City not found → Show location error + GPS option
- [ ] Empty API response → Handle gracefully
- [ ] Malformed API response → Show service error
- [ ] Unexpected exception → Show unknown error + log details

---

**Document Status**: Complete
**Reviewed By**: Development Team
**Next Review**: 2025-11-29
