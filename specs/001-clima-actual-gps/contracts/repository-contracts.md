# Repository Contracts Documentation

**Project**: MyWeatherApp
**Feature**: Clima Actual con GPS
**Version**: 1.0.0
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Overview](#overview)
2. [WeatherDataRepository](#weatherdatarepository)
3. [WeatherGeoRepository](#weathergeorepository)
4. [Implementation Requirements](#implementation-requirements)
5. [Threading & Coroutines](#threading--coroutines)
6. [Error Handling Contract](#error-handling-contract)
7. [Testing Contracts](#testing-contracts)

---

## Overview

Repository interfaces define the contract between the domain layer and the data layer. These interfaces MUST be implemented by the data layer and consumed by use cases in the domain layer.

### Design Principles

- **Abstraction**: Repositories abstract data sources from domain logic
- **Single Responsibility**: Each repository handles a single domain entity
- **Dependency Inversion**: Domain depends on repository interfaces, not implementations
- **Testability**: Interfaces enable easy mocking for unit tests
- **Thread Safety**: All methods are suspend functions (coroutine-safe)

### Architecture Layer

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (ScreenModels, Composables)          │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│  ┌───────────────────────────────┐     │
│  │        Use Cases              │     │
│  └───────────────────────────────┘     │
│              ↓                          │
│  ┌───────────────────────────────┐     │
│  │  Repository Interfaces ⭐     │     │  ← THIS DOCUMENT
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  ┌───────────────────────────────┐     │
│  │  Repository Implementations   │     │
│  └───────────────────────────────┘     │
│              ↓                          │
│  ┌───────────────────────────────┐     │
│  │      Data Sources             │     │
│  │  (Remote, Local, Cache)       │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
```

---

## WeatherDataRepository

### Interface Definition

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/WeatherDataRepository.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.WeatherDataResult

/**
 * Repository interface for weather data operations.
 *
 * This interface defines the contract for retrieving current weather information
 * based on geographic coordinates. It abstracts the data source implementation
 * from the domain layer.
 *
 * @see WeatherDataResult for the result type definition
 */
interface WeatherDataRepository {

    /**
     * Retrieves current weather data for the specified geographic coordinates.
     *
     * @param lat Latitude in decimal degrees. Must be in range [-90.0, 90.0].
     * @param lon Longitude in decimal degrees. Must be in range [-180.0, 180.0].
     *
     * @return [WeatherDataResult] containing either:
     *   - [WeatherDataResult.Data] with the weather information on success
     *   - [WeatherDataResult.Error] with the error information on failure
     *
     * @throws IllegalArgumentException if lat/lon are out of valid range (implementation responsibility)
     *
     * ## Thread Safety
     * This method is a suspend function and must be called from a coroutine context.
     * Implementations MUST be thread-safe and coroutine-safe.
     *
     * ## Performance Expectations
     * - Network request timeout: 10 seconds maximum
     * - Should complete within 3 seconds under normal network conditions
     * - Implementations SHOULD implement caching to reduce API calls
     *
     * ## Error Scenarios
     * Implementations MUST handle and return appropriate errors for:
     * - Network connectivity issues → [WeatherConectionException]
     * - API errors (4xx, 5xx) → [WeatherServiceException]
     * - Invalid coordinates → [WeatherException]
     * - Timeout → [WeatherConectionException]
     * - Unknown errors → [WeatherUnknowException]
     *
     * ## Example Usage
     * ```kotlin
     * class GetWeatherDataUseCase(
     *     private val repository: WeatherDataRepository
     * ) {
     *     suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult {
     *         return repository.getWeatherData(lat, lon)
     *     }
     * }
     * ```
     *
     * ## Implementation Contract
     * Implementations MUST:
     * 1. Validate coordinates before making API calls
     * 2. Use suspend functions for all IO operations
     * 3. Map data layer exceptions to domain exceptions
     * 4. Return [WeatherDataResult] (never throw exceptions)
     * 5. Map DTOs to domain entities
     * 6. Log errors appropriately
     *
     * @since 1.0.0
     */
    suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult
}
```

### Result Type Definition

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/WeatherDataResult.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain

import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity

/**
 * Sealed class representing the result of a weather data operation.
 *
 * This follows the Result pattern to encapsulate success and error states
 * without using exceptions for flow control.
 */
sealed class WeatherDataResult {

    /**
     * Represents a successful weather data retrieval.
     *
     * @property data The weather data entity containing all weather information
     */
    data class Data(val data: WeatherDataEntity) : WeatherDataResult()

    /**
     * Represents a failed weather data retrieval.
     *
     * @property error The domain exception explaining the failure
     */
    data class Error(val error: WeatherException) : WeatherDataResult()
}
```

### Domain Entity

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/entity/WeatherDataEntity.kt`

```kotlin
/**
 * Domain entity representing complete weather information.
 *
 * This is a pure domain model with no dependencies on Android or data layer.
 * All fields are nullable to handle incomplete API responses gracefully.
 */
data class WeatherDataEntity(
    /** Geographic coordinates of the location */
    val coord: WeatherCoordEntity?,

    /** List of weather conditions (can have multiple) */
    val weather: List<WeatherEntity>,

    /** Internal parameter (stations base) */
    val base: String?,

    /** Main weather metrics (temperature, pressure, humidity) */
    val main: WeatherMainEntity?,

    /** Visibility in meters */
    val visibility: Int?,

    /** Wind information */
    val wind: WeatherWindEntity?,

    /** Cloudiness information */
    val clouds: WeatherCloudsEntity?,

    /** Time of data calculation (Unix timestamp, UTC) */
    val dt: Int?,

    /** System information (country, sunrise, sunset) */
    val sys: WeatherSysEntity?,

    /** Timezone shift from UTC in seconds */
    val timezone: Int?,

    /** City ID */
    val id: Int?,

    /** City name */
    val name: String?,

    /** HTTP status code from API */
    val cod: Int?
)
```

### Method Contract Details

#### getWeatherData(lat, lon)

**Contract Guarantees**:

| Aspect | Guarantee |
|--------|-----------|
| **Return Type** | Always returns `WeatherDataResult` (Data or Error) |
| **Exceptions** | MUST NOT throw exceptions (wrap in Error result) |
| **Thread Safety** | MUST be thread-safe and coroutine-safe |
| **Null Safety** | Never returns null; always Data or Error |
| **Idempotency** | Multiple calls with same params should return same data (until cache expires) |
| **Side Effects** | May cache results; no other side effects |

**Input Validation**:

```kotlin
// Implementations MUST validate:
require(lat in -90.0..90.0) { "Latitude must be between -90 and 90" }
require(lon in -180.0..180.0) { "Longitude must be between -180 and 180" }
```

**Error Handling Pattern**:

```kotlin
// Implementation pattern
override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
    return try {
        // Validate inputs
        validateCoordinates(lat, lon)

        // Fetch from data source
        val response = remoteDataSource.fetchWeather(lat, lon)

        // Map to domain entity
        val entity = response.toEntity()

        // Return success
        WeatherDataResult.Data(entity)

    } catch (e: Exception) {
        // Map exception to domain error
        val domainError = errorFactory.create(e)
        WeatherDataResult.Error(domainError)
    }
}
```

**Caching Expectations**:

Implementations SHOULD implement caching:
- Cache TTL: 10 minutes (weather data doesn't change frequently)
- Cache key: Combination of lat/lon (rounded to 2 decimals)
- Cache invalidation: Time-based or manual refresh

```kotlin
// Recommended caching strategy
private val cache = LruCache<String, CachedWeather>(maxSize = 50)

private fun getCacheKey(lat: Double, lon: Double): String {
    return "${lat.roundTo(2)},${lon.roundTo(2)}"
}

private fun isCacheValid(cachedWeather: CachedWeather): Boolean {
    val age = System.currentTimeMillis() - cachedWeather.timestamp
    return age < 600_000 // 10 minutes
}
```

---

## WeatherGeoRepository

### Interface Definition

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/WeatherGeoRepository.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain.repository

import com.mtzdev.mywheatherapp.domain.WeatherGeoResult

/**
 * Repository interface for geocoding operations.
 *
 * This interface defines the contract for converting city names to geographic
 * coordinates using geocoding services. It abstracts the geocoding implementation
 * from the domain layer.
 *
 * @see WeatherGeoResult for the result type definition
 */
interface WeatherGeoRepository {

    /**
     * Searches for geographic coordinates based on a city name.
     *
     * @param city City name to search for. Can include country code (e.g., "Madrid,ES").
     *             Must not be empty or blank.
     *
     * @return [WeatherGeoResult] containing either:
     *   - [WeatherGeoResult.Data] with the location information on success
     *   - [WeatherGeoResult.Error] with the error information on failure
     *
     * @throws IllegalArgumentException if city name is blank (implementation responsibility)
     *
     * ## Thread Safety
     * This method is a suspend function and must be called from a coroutine context.
     * Implementations MUST be thread-safe and coroutine-safe.
     *
     * ## Performance Expectations
     * - Network request timeout: 10 seconds maximum
     * - Should complete within 3 seconds under normal network conditions
     * - Implementations SHOULD implement caching (geocoding results rarely change)
     *
     * ## Error Scenarios
     * Implementations MUST handle and return appropriate errors for:
     * - Network connectivity issues → [WeatherConectionException]
     * - City not found → [WeatherUnknowGeoDataException]
     * - API errors (4xx, 5xx) → [WeatherServiceException]
     * - Empty/blank city name → [WeatherException]
     * - Timeout → [WeatherConectionException]
     * - Unknown errors → [WeatherUnknowException]
     *
     * ## Behavior Specification
     * - When multiple locations match, return the FIRST result (highest relevance)
     * - When no locations match, return [WeatherGeoResult.Error] with [WeatherUnknowGeoDataException]
     * - City name is case-insensitive
     * - Supports international characters (UTF-8)
     *
     * ## Example Usage
     * ```kotlin
     * class SearchLocationUseCase(
     *     private val repository: WeatherGeoRepository
     * ) {
     *     suspend operator fun invoke(cityName: String): WeatherGeoResult {
     *         // Validate input
     *         if (cityName.isBlank()) {
     *             return WeatherGeoResult.Error(
     *                 WeatherException("City name cannot be empty")
     *             )
     *         }
     *
     *         return repository.getGeoData(cityName)
     *     }
     * }
     * ```
     *
     * ## Implementation Contract
     * Implementations MUST:
     * 1. Validate city name is not blank
     * 2. Trim whitespace from city name
     * 3. Use suspend functions for all IO operations
     * 4. Map data layer exceptions to domain exceptions
     * 5. Return [WeatherGeoResult] (never throw exceptions)
     * 6. Handle empty API results as "city not found" error
     * 7. Map DTOs to domain entities
     * 8. Limit API request to 1 result (most relevant)
     *
     * ## Caching Recommendation
     * Geocoding results are stable and SHOULD be cached indefinitely:
     * - Use persistent cache (SharedPreferences or Room)
     * - Cache key: Normalized city name (lowercase, trimmed)
     * - No TTL (coordinates don't change)
     *
     * @since 1.0.0
     */
    suspend fun getGeoData(city: String): WeatherGeoResult
}
```

### Result Type Definition

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/WeatherGeoResult.kt`

```kotlin
package com.mtzdev.mywheatherapp.domain

import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

/**
 * Sealed class representing the result of a geocoding operation.
 *
 * This follows the Result pattern to encapsulate success and error states
 * without using exceptions for flow control.
 */
sealed class WeatherGeoResult {

    /**
     * Represents a successful geocoding operation.
     *
     * @property data The location entity containing coordinates and metadata
     */
    data class Data(val data: WeatherGeoDataEntity) : WeatherGeoResult()

    /**
     * Represents a failed geocoding operation.
     *
     * @property error The domain exception explaining the failure
     */
    data class Error(val error: WeatherException) : WeatherGeoResult()
}
```

### Domain Entity

**File**: `/app/src/main/java/com/mtzdev/mywheatherapp/domain/entity/WeatherGeoDataEntity.kt`

```kotlin
/**
 * Domain entity representing geographic location information.
 *
 * This is a pure domain model with no dependencies on Android or data layer.
 *
 * @property name City name in English
 * @property localNames Localized city names in different languages
 * @property lat Latitude in decimal degrees (REQUIRED, defaults provided for safety)
 * @property lon Longitude in decimal degrees (REQUIRED, defaults provided for safety)
 * @property country Country code (ISO 3166-1 alpha-2)
 */
data class WeatherGeoDataEntity(
    val name: String? = null,
    val localNames: LocalNamesEntity?,
    val lat: Double,  // Required, has default fallback
    val lon: Double,  // Required, has default fallback
    val country: String? = null
)

/**
 * Localized names for the location.
 *
 * @property es Spanish name
 * @property en English name
 */
data class LocalNamesEntity(
    val es: String? = null,
    val en: String? = null
)
```

### Method Contract Details

#### getGeoData(city)

**Contract Guarantees**:

| Aspect | Guarantee |
|--------|-----------|
| **Return Type** | Always returns `WeatherGeoResult` (Data or Error) |
| **Exceptions** | MUST NOT throw exceptions (wrap in Error result) |
| **Thread Safety** | MUST be thread-safe and coroutine-safe |
| **Null Safety** | Never returns null; always Data or Error |
| **Case Sensitivity** | Case-insensitive search |
| **Whitespace** | Trims leading/trailing whitespace |
| **Results** | Returns FIRST (most relevant) result only |

**Input Validation**:

```kotlin
// Implementations MUST validate:
val trimmedCity = city.trim()
require(trimmedCity.isNotBlank()) { "City name cannot be blank" }
```

**Error Handling Pattern**:

```kotlin
// Implementation pattern
override suspend fun getGeoData(city: String): WeatherGeoResult {
    return try {
        // Validate and normalize input
        val trimmedCity = city.trim()
        require(trimmedCity.isNotBlank()) { "City name cannot be blank" }

        // Fetch from data source
        val response = remoteDataSource.searchCity(trimmedCity)

        // Handle empty results
        if (response.isEmpty()) {
            return WeatherGeoResult.Error(
                WeatherUnknowGeoDataException("City '$city' not found")
            )
        }

        // Map first result to domain entity
        val entity = response.first().toEntity()

        // Return success
        WeatherGeoResult.Data(entity)

    } catch (e: Exception) {
        // Map exception to domain error
        val domainError = errorFactory.create(e)
        WeatherGeoResult.Error(domainError)
    }
}
```

**Caching Expectations**:

Implementations SHOULD implement persistent caching:

```kotlin
// Recommended caching strategy
private val geoCache = mutableMapOf<String, WeatherGeoDataEntity>()

private fun getCacheKey(city: String): String {
    return city.trim().lowercase()
}

// Cache indefinitely (coordinates don't change)
private suspend fun getCachedOrFetch(city: String): WeatherGeoDataEntity {
    val cacheKey = getCacheKey(city)

    // Check cache first
    geoCache[cacheKey]?.let { return it }

    // Fetch from API
    val result = remoteDataSource.searchCity(city)
    val entity = result.first().toEntity()

    // Store in cache
    geoCache[cacheKey] = entity

    return entity
}
```

---

## Implementation Requirements

### General Requirements

All repository implementations MUST:

1. **Be located in the data layer**
   ```
   app/src/main/java/com/mtzdev/mywheatherapp/data/repository/
   ```

2. **Implement the interface from domain layer**
   ```kotlin
   class WeatherDataRepositoryData(
       private val remoteDataSource: WeatherDataDataSource,
       private val errorFactory: FactoryWeatherException
   ) : WeatherDataRepository {
       // Implementation
   }
   ```

3. **Use dependency injection (Koin)**
   ```kotlin
   val dataModule = module {
       single<WeatherDataRepository> {
           WeatherDataRepositoryData(get(), get())
       }
       single<WeatherGeoRepository> {
           WeatherGeoRepositoryData(get(), get())
       }
   }
   ```

4. **Map data models to domain entities**
   ```kotlin
   // Use extension functions for mapping
   fun WeatherDataModel.toEntity(): WeatherDataEntity {
       return WeatherDataEntity(
           coord = this.coord?.toEntity(),
           weather = this.weather.map { it.toEntity() },
           // ... other fields
       )
   }
   ```

5. **Handle all exceptions gracefully**
   ```kotlin
   // Never let exceptions escape
   try {
       // ... operation
   } catch (e: Exception) {
       return Result.Error(errorFactory.create(e))
   }
   ```

### Validation Requirements

**WeatherDataRepository Implementation MUST validate**:

```kotlin
private fun validateCoordinates(lat: Double, lon: Double) {
    require(lat in -90.0..90.0) {
        "Latitude must be between -90 and 90, got: $lat"
    }
    require(lon in -180.0..180.0) {
        "Longitude must be between -180 and 180, got: $lon"
    }
}
```

**WeatherGeoRepository Implementation MUST validate**:

```kotlin
private fun validateCityName(city: String) {
    val trimmed = city.trim()
    require(trimmed.isNotBlank()) {
        "City name cannot be blank"
    }
    require(trimmed.length <= 255) {
        "City name too long (max 255 characters)"
    }
}
```

### Logging Requirements

Implementations SHOULD log:

```kotlin
// Success cases (debug level)
Log.d(TAG, "Weather data fetched successfully for ($lat, $lon)")

// Error cases (error level)
Log.e(TAG, "Failed to fetch weather data for ($lat, $lon)", exception)

// Performance metrics (verbose level)
Log.v(TAG, "Weather API call took ${duration}ms")
```

---

## Threading & Coroutines

### Coroutine Context

All repository methods are `suspend` functions and MUST:

1. **Use appropriate dispatchers**
   ```kotlin
   withContext(Dispatchers.IO) {
       // Network or database operations
   }
   ```

2. **Not block the calling thread**
   ```kotlin
   // ❌ WRONG - blocks thread
   fun getData(): Result = runBlocking {
       api.fetch()
   }

   // ✅ CORRECT - suspend function
   suspend fun getData(): Result {
       return api.fetch()
   }
   ```

3. **Be cancellation-cooperative**
   ```kotlin
   suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
       return withContext(Dispatchers.IO) {
           // Check for cancellation
           ensureActive()

           // Perform operation
           val response = api.fetchWeather(lat, lon)

           // Map result
           WeatherDataResult.Data(response.toEntity())
       }
   }
   ```

### Thread Safety Guarantees

Implementations MUST be thread-safe:

```kotlin
class WeatherDataRepositoryData : WeatherDataRepository {
    // Use thread-safe collections
    private val cache = ConcurrentHashMap<String, CachedWeather>()

    // Or use mutex for synchronization
    private val mutex = Mutex()

    override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
        mutex.withLock {
            // Critical section
        }
    }
}
```

### Timeout Handling

Implement timeouts for network operations:

```kotlin
suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
    return try {
        withTimeout(10_000) { // 10 second timeout
            val response = remoteDataSource.fetchWeather(lat, lon)
            WeatherDataResult.Data(response.toEntity())
        }
    } catch (e: TimeoutCancellationException) {
        WeatherDataResult.Error(
            WeatherConectionException("Request timed out")
        )
    }
}
```

---

## Error Handling Contract

### Exception Mapping

Repositories MUST map data layer exceptions to domain exceptions:

```kotlin
private fun mapToDomainException(exception: Exception): WeatherException {
    return when (exception) {
        is UnknownHostException,
        is ConnectException,
        is HttpRequestTimeoutException ->
            WeatherConectionException("Error de conexión")

        is ClientRequestException -> when (exception.response.status.value) {
            401 -> WeatherServiceException("API key inválida")
            404 -> WeatherServiceException("Ubicación no encontrada")
            429 -> WeatherServiceException("Límite de solicitudes excedido")
            else -> WeatherServiceException("Error en la solicitud")
        }

        is ServerResponseException ->
            WeatherServiceException("El servicio no está disponible")

        is IllegalArgumentException ->
            WeatherException(exception.message ?: "Parámetros inválidos")

        else ->
            WeatherUnknowException("Error desconocido: ${exception.message}")
    }
}
```

### Error Result Construction

```kotlin
// Always wrap errors in Result.Error
override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
    return try {
        // ... operation
        WeatherDataResult.Data(entity)
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching weather data", e)
        WeatherDataResult.Error(mapToDomainException(e))
    }
}
```

### Special Error Cases

**Empty Geocoding Results**:
```kotlin
val results = api.searchCity(city)
if (results.isEmpty()) {
    return WeatherGeoResult.Error(
        WeatherUnknowGeoDataException("Ciudad '$city' no encontrada")
    )
}
```

**Null Required Fields**:
```kotlin
val geoModel = results.first()
if (geoModel.lat == null || geoModel.lon == null) {
    return WeatherGeoResult.Error(
        WeatherServiceException("Respuesta de API incompleta")
    )
}
```

---

## Testing Contracts

### Unit Test Requirements

All repository implementations MUST have unit tests with 80%+ coverage:

```kotlin
class WeatherDataRepositoryDataTest {

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
    fun `getWeatherData returns Data on successful API call`() = runTest {
        // Given
        val mockResponse = WeatherDataModel(/* ... */)
        coEvery { mockDataSource.fetchWeather(any(), any()) } returns mockResponse

        // When
        val result = repository.getWeatherData(40.4165, -3.7026)

        // Then
        assertTrue(result is WeatherDataResult.Data)
        assertEquals("Madrid", (result as WeatherDataResult.Data).data.name)
    }

    @Test
    fun `getWeatherData returns Error on network failure`() = runTest {
        // Given
        coEvery { mockDataSource.fetchWeather(any(), any()) } throws
            UnknownHostException()

        // When
        val result = repository.getWeatherData(40.4165, -3.7026)

        // Then
        assertTrue(result is WeatherDataResult.Error)
        assertTrue((result as WeatherDataResult.Error).error is WeatherConectionException)
    }

    @Test
    fun `getWeatherData validates latitude range`() = runTest {
        // When
        val result = repository.getWeatherData(91.0, 0.0) // Invalid lat

        // Then
        assertTrue(result is WeatherDataResult.Error)
    }

    @Test
    fun `getWeatherData validates longitude range`() = runTest {
        // When
        val result = repository.getWeatherData(0.0, 181.0) // Invalid lon

        // Then
        assertTrue(result is WeatherDataResult.Error)
    }
}
```

### Test Coverage Checklist

For **WeatherDataRepository**:
- [ ] Returns Data on successful API call
- [ ] Returns Error on network failure
- [ ] Returns Error on API error (4xx)
- [ ] Returns Error on server error (5xx)
- [ ] Returns Error on timeout
- [ ] Validates latitude range [-90, 90]
- [ ] Validates longitude range [-180, 180]
- [ ] Maps DTO to entity correctly
- [ ] Handles null fields in response gracefully
- [ ] Caches results (if implemented)
- [ ] Respects cache TTL (if implemented)

For **WeatherGeoRepository**:
- [ ] Returns Data on successful city search
- [ ] Returns Error when city not found (empty results)
- [ ] Returns Error on network failure
- [ ] Returns Error on API error
- [ ] Validates city name is not blank
- [ ] Trims whitespace from city name
- [ ] Returns first result when multiple matches
- [ ] Maps DTO to entity correctly
- [ ] Handles missing lat/lon in response
- [ ] Caches results (if implemented)

### Mock Data Helpers

```kotlin
object RepositoryTestData {

    fun createWeatherDataModel(
        temp: Double = 25.5,
        cityName: String = "Madrid"
    ) = WeatherDataModel(
        coord = WeatherCoordModel(lon = -3.7026, lat = 40.4165),
        weather = listOf(
            WeatherModel(id = 800, main = "Clear", description = "clear sky", icon = "01d")
        ),
        main = WeatherMainModel(temp = temp, humidity = 60),
        name = cityName,
        cod = 200
    )

    fun createWeatherGeoModel(
        cityName: String = "Madrid",
        lat: Double = 40.4165,
        lon: Double = -3.7026
    ) = WeatherGeoModel(
        name = cityName,
        lat = lat,
        lon = lon,
        country = "ES",
        localNames = LocalNamesModel(es = cityName, en = cityName)
    )
}
```

---

## Performance Benchmarks

### Expected Performance

| Operation | Max Duration | Typical Duration | Timeout |
|-----------|-------------|------------------|---------|
| `getWeatherData()` | 3 seconds | 1-2 seconds | 10 seconds |
| `getGeoData()` | 3 seconds | 1-2 seconds | 10 seconds |
| Cached `getWeatherData()` | 50ms | 10-20ms | N/A |
| Cached `getGeoData()` | 50ms | 10-20ms | N/A |

### Performance Testing

```kotlin
@Test
fun `getWeatherData completes within timeout`() = runTest {
    // Given
    val startTime = System.currentTimeMillis()

    // When
    repository.getWeatherData(40.4165, -3.7026)
    val duration = System.currentTimeMillis() - startTime

    // Then
    assertTrue(duration < 10_000, "Request took ${duration}ms, expected < 10000ms")
}
```

---

## Appendix: Complete Implementation Example

### WeatherDataRepositoryData

```kotlin
package com.mtzdev.mywheatherapp.data.repository

import com.mtzdev.mywheatherapp.commons.FactoryWeatherException
import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.entity.toEntity
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class WeatherDataRepositoryData(
    private val remoteDataSource: WeatherDataDataSource,
    private val errorFactory: FactoryWeatherException
) : WeatherDataRepository {

    override suspend fun getWeatherData(lat: Double, lon: Double): WeatherDataResult {
        return withContext(Dispatchers.IO) {
            try {
                // Validate inputs
                validateCoordinates(lat, lon)

                // Fetch with timeout
                val response = withTimeout(10_000) {
                    remoteDataSource.getWeatherData(lat, lon)
                }

                // Map to domain entity
                val entity = response.toEntity()

                // Return success
                WeatherDataResult.Data(entity)

            } catch (e: Exception) {
                // Map to domain exception
                val domainError = errorFactory.create(e)
                WeatherDataResult.Error(domainError)
            }
        }
    }

    private fun validateCoordinates(lat: Double, lon: Double) {
        require(lat in -90.0..90.0) {
            "Latitude must be between -90 and 90, got: $lat"
        }
        require(lon in -180.0..180.0) {
            "Longitude must be between -180 and 180, got: $lon"
        }
    }
}
```

---

**Document Status**: Complete
**Reviewed By**: Development Team
**Next Review**: 2025-11-29
