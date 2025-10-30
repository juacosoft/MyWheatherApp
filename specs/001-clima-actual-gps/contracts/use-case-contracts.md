# Use Case Contracts Documentation

**Project**: MyWeatherApp
**Feature**: Clima Actual con GPS
**Version**: 1.0.0
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Overview](#overview)
2. [GetWeatherDataUseCase](#getweatherdatausecase)
3. [GetWeatherGeoDataUseCase](#getweathergeodatausecase)
4. [Use Case Design Principles](#use-case-design-principles)
5. [Testing Contracts](#testing-contracts)
6. [Usage Examples](#usage-examples)

---

## Overview

Use Cases represent the business logic layer in Clean Architecture. They orchestrate the flow of data between repositories and the presentation layer, encapsulating specific application operations.

### Architecture Position

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (ScreenModels, ViewModels)           │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│  ┌───────────────────────────────┐     │
│  │       Use Cases ⭐            │     │  ← THIS DOCUMENT
│  │  (Business Logic)             │     │
│  └───────────────────────────────┘     │
│              ↓                          │
│  ┌───────────────────────────────┐     │
│  │   Repository Interfaces       │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (Repository Implementations)           │
└─────────────────────────────────────────┘
```

### Design Philosophy

Use Cases in this application follow these principles:

1. **Single Responsibility**: Each use case does ONE thing
2. **Operator Invoke**: Use `operator fun invoke()` for callable objects
3. **Suspend Functions**: All use cases are suspending (async by default)
4. **No Android Dependencies**: Pure Kotlin (testable on JVM)
5. **Repository Delegation**: Delegate data operations to repositories
6. **Minimal Logic**: Only coordination and business rules

---

## GetWeatherDataUseCase

### Purpose

Retrieves current weather data for specific geographic coordinates. This use case orchestrates the weather data retrieval from the repository.

### File Location

```
/app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetWeatherDataUseCase.kt
```

### Interface Definition

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository

/**
 * Use case for retrieving current weather data by coordinates.
 *
 * This use case encapsulates the business logic for fetching weather information
 * based on geographic coordinates (latitude and longitude). It delegates the
 * actual data retrieval to the [WeatherDataRepository].
 *
 * ## Responsibilities
 * - Validate input coordinates (lat, lon)
 * - Delegate data retrieval to repository
 * - Return weather data result (success or error)
 *
 * ## Thread Safety
 * This use case uses suspend functions and is coroutine-safe.
 * It MUST be called from a coroutine context.
 *
 * ## Usage Example
 * ```kotlin
 * class WeatherScreenModel(
 *     private val getWeatherDataUseCase: GetWeatherDataUseCase
 * ) {
 *     fun loadWeather(lat: Double, lon: Double) {
 *         viewModelScope.launch {
 *             when (val result = getWeatherDataUseCase(lat, lon)) {
 *                 is WeatherDataResult.Data -> {
 *                     // Update UI with weather data
 *                     updateState { copy(weatherData = result.data) }
 *                 }
 *                 is WeatherDataResult.Error -> {
 *                     // Handle error
 *                     sendEffect(WeatherContract.Effect.ShowError(result.error))
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @property repository The weather data repository for data retrieval
 *
 * @since 1.0.0
 */
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {

    /**
     * Executes the use case to retrieve weather data.
     *
     * @param lat Latitude in decimal degrees. Valid range: [-90.0, 90.0]
     * @param lon Longitude in decimal degrees. Valid range: [-180.0, 180.0]
     *
     * @return [WeatherDataResult] containing either:
     *   - [WeatherDataResult.Data] with weather information on success
     *   - [WeatherDataResult.Error] with error information on failure
     *
     * ## Input Validation
     * This use case delegates validation to the repository layer.
     * Invalid coordinates will result in [WeatherDataResult.Error].
     *
     * ## Error Scenarios
     * Returns [WeatherDataResult.Error] for:
     * - Invalid coordinates (out of range)
     * - Network connectivity issues
     * - API errors (4xx, 5xx)
     * - Timeout errors
     * - Unknown errors
     *
     * ## Performance
     * - Expected duration: 1-3 seconds (network-dependent)
     * - Timeout: 10 seconds (repository timeout)
     * - This is a suspend function (non-blocking)
     *
     * ## Thread Safety
     * This method is thread-safe and can be called from any coroutine context.
     * The repository handles dispatcher switching internally.
     *
     * @since 1.0.0
     */
    suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult =
        repository.getWeatherData(lat, lon)
}
```

### Contract Details

#### Method Signature

```kotlin
suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult
```

#### Input Parameters

| Parameter | Type | Description | Valid Range | Required |
|-----------|------|-------------|-------------|----------|
| `lat` | Double | Latitude in decimal degrees | -90.0 to 90.0 | Yes |
| `lon` | Double | Longitude in decimal degrees | -180.0 to 180.0 | Yes |

#### Return Type

**Type**: `WeatherDataResult` (sealed class)

**Possible Values**:
- `WeatherDataResult.Data(data: WeatherDataEntity)` - Success
- `WeatherDataResult.Error(error: WeatherException)` - Failure

#### Guarantees

| Aspect | Guarantee |
|--------|-----------|
| **Return Type** | Always returns `WeatherDataResult` (never null) |
| **Exceptions** | NEVER throws exceptions |
| **Thread Safety** | Thread-safe, coroutine-safe |
| **Side Effects** | None (pure delegation to repository) |
| **Idempotency** | Multiple calls with same params return same data (until cache expires) |
| **Timeout** | Completes or times out within 10 seconds (repository timeout) |

#### Business Rules

1. **No additional validation**: Delegates all validation to repository
2. **Direct delegation**: No data transformation or filtering
3. **No caching**: Caching is repository responsibility
4. **No retry logic**: Retry is handled at repository or presentation layer

### Dependency Injection

**Koin Module** (`/app/src/main/java/com/mtzdev/mywheatherapp/di/ViewModelModule.kt`):

```kotlin
val domainModule = module {
    factory { GetWeatherDataUseCase(get()) }
}
```

**Usage in ScreenModel**:

```kotlin
class WeatherDataScreenModel(
    private val getWeatherDataUseCase: GetWeatherDataUseCase,
    // ... other dependencies
) : MVIBaseScreenModel<WeatherDataContract.State, WeatherDataContract.Event, WeatherDataContract.Effect>() {

    private fun loadWeather(lat: Double, lon: Double) {
        screenModelScope.launch {
            updateState { copy(loadingWeather = true) }

            when (val result = getWeatherDataUseCase(lat, lon)) {
                is WeatherDataResult.Data -> {
                    updateState {
                        copy(
                            loadingWeather = false,
                            weatherData = result.data
                        )
                    }
                }
                is WeatherDataResult.Error -> {
                    updateState { copy(loadingWeather = false) }
                    sendEffect(WeatherDataContract.Effect.ShowError(result.error))
                }
            }
        }
    }
}
```

---

## GetWeatherGeoDataUseCase

### Purpose

Searches for geographic location information (coordinates) based on a city name. This use case orchestrates the geocoding operation.

### File Location

```
/app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetWeatherGeoDataUseCase.kt
```

### Interface Definition

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository

/**
 * Use case for searching location data by city name.
 *
 * This use case encapsulates the business logic for geocoding operations,
 * converting city names into geographic coordinates. It delegates the actual
 * geocoding to the [WeatherGeoRepository].
 *
 * ## Responsibilities
 * - Validate city name input
 * - Delegate geocoding to repository
 * - Return location data result (success or error)
 *
 * ## Thread Safety
 * This use case uses suspend functions and is coroutine-safe.
 * It MUST be called from a coroutine context.
 *
 * ## Usage Example
 * ```kotlin
 * class HomeScreenModel(
 *     private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase
 * ) {
 *     fun searchCity(cityName: String) {
 *         viewModelScope.launch {
 *             updateState { copy(loadingGeo = true) }
 *
 *             when (val result = getWeatherGeoDataUseCase(cityName)) {
 *                 is WeatherGeoResult.Data -> {
 *                     // Use coordinates to fetch weather
 *                     updateState {
 *                         copy(
 *                             loadingGeo = false,
 *                             geoData = result.data
 *                         )
 *                     }
 *                 }
 *                 is WeatherGeoResult.Error -> {
 *                     updateState { copy(loadingGeo = false) }
 *                     sendEffect(HomeContract.Effect.ShowError(result.error))
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @property weatherGeoRepository The geocoding repository for location data
 *
 * @since 1.0.0
 */
class GetWeatherGeoDataUseCase(
    private val weatherGeoRepository: WeatherGeoRepository
) {

    /**
     * Executes the use case to search for location by city name.
     *
     * @param city City name to search for. Can include country code (e.g., "Madrid,ES").
     *             Must not be blank.
     *
     * @return [WeatherGeoResult] containing either:
     *   - [WeatherGeoResult.Data] with location information on success
     *   - [WeatherGeoResult.Error] with error information on failure
     *
     * ## Input Validation
     * This use case delegates validation to the repository layer.
     * Blank city names will result in [WeatherGeoResult.Error].
     *
     * ## Behavior
     * - Case-insensitive search
     * - Trims whitespace automatically
     * - Returns first (most relevant) result
     * - Empty results = city not found error
     *
     * ## Error Scenarios
     * Returns [WeatherGeoResult.Error] for:
     * - Blank or empty city name
     * - City not found (no results)
     * - Network connectivity issues
     * - API errors (4xx, 5xx)
     * - Timeout errors
     * - Unknown errors
     *
     * ## Performance
     * - Expected duration: 1-3 seconds (network-dependent)
     * - Timeout: 10 seconds (repository timeout)
     * - Results may be cached indefinitely (coordinates don't change)
     * - This is a suspend function (non-blocking)
     *
     * ## Thread Safety
     * This method is thread-safe and can be called from any coroutine context.
     * The repository handles dispatcher switching internally.
     *
     * @since 1.0.0
     */
    suspend operator fun invoke(city: String): WeatherGeoResult =
        weatherGeoRepository.getGeoData(city)
}
```

### Contract Details

#### Method Signature

```kotlin
suspend operator fun invoke(city: String): WeatherGeoResult
```

#### Input Parameters

| Parameter | Type | Description | Validation | Required |
|-----------|------|-------------|------------|----------|
| `city` | String | City name (can include country code) | Non-blank, max 255 chars | Yes |

**Valid Examples**:
- `"Madrid"`
- `"Madrid,ES"`
- `"New York"`
- `"Barcelona, Spain"`
- `"São Paulo"`

**Invalid Examples**:
- `""` (empty)
- `"   "` (blank)
- `null` (compilation error)

#### Return Type

**Type**: `WeatherGeoResult` (sealed class)

**Possible Values**:
- `WeatherGeoResult.Data(data: WeatherGeoDataEntity)` - Success
- `WeatherGeoResult.Error(error: WeatherException)` - Failure

#### Guarantees

| Aspect | Guarantee |
|--------|-----------|
| **Return Type** | Always returns `WeatherGeoResult` (never null) |
| **Exceptions** | NEVER throws exceptions |
| **Thread Safety** | Thread-safe, coroutine-safe |
| **Side Effects** | None (pure delegation to repository) |
| **Case Sensitivity** | Case-insensitive (handled by repository) |
| **Whitespace** | Trimmed automatically (handled by repository) |
| **Results** | First (most relevant) result only |

#### Business Rules

1. **No additional validation**: Delegates validation to repository
2. **Direct delegation**: No data transformation
3. **No caching**: Caching is repository responsibility
4. **First result only**: If multiple cities match, repository returns most relevant

### Dependency Injection

**Koin Module**:

```kotlin
val domainModule = module {
    factory { GetWeatherGeoDataUseCase(get()) }
}
```

**Usage in ScreenModel**:

```kotlin
class HomeScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase
) : MVIBaseScreenModel<HomeContract.State, HomeContract.Event, HomeContract.Effect>() {

    private fun searchCity(cityName: String) {
        screenModelScope.launch {
            updateState { copy(loadingGeo = true) }

            // Step 1: Get coordinates for city
            when (val geoResult = getWeatherGeoDataUseCase(cityName)) {
                is WeatherGeoResult.Data -> {
                    val location = geoResult.data
                    updateState {
                        copy(
                            loadingGeo = false,
                            geoData = location
                        )
                    }

                    // Step 2: Get weather for coordinates
                    loadWeatherForLocation(location.lat, location.lon)
                }

                is WeatherGeoResult.Error -> {
                    updateState { copy(loadingGeo = false) }
                    sendEffect(HomeContract.Effect.ShowError(geoResult.error))
                }
            }
        }
    }
}
```

---

## Use Case Design Principles

### 1. Single Responsibility Principle

Each use case should do ONE thing:

```kotlin
// ✅ GOOD - Single responsibility
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) =
        repository.getWeatherData(lat, lon)
}

// ❌ BAD - Multiple responsibilities
class WeatherUseCase(
    private val weatherRepo: WeatherDataRepository,
    private val geoRepo: WeatherGeoRepository
) {
    suspend fun getWeatherByCity(city: String): WeatherDataResult {
        // Too much logic in one use case
        val geoResult = geoRepo.getGeoData(city)
        return if (geoResult is WeatherGeoResult.Data) {
            weatherRepo.getWeatherData(geoResult.data.lat, geoResult.data.lon)
        } else {
            // ... error handling
        }
    }
}
```

### 2. Operator Invoke Pattern

Use `operator fun invoke()` to make use cases callable as functions:

```kotlin
// Use case definition
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) =
        repository.getWeatherData(lat, lon)
}

// Usage - clean syntax
val result = getWeatherDataUseCase(40.4165, -3.7026)

// Instead of verbose syntax
val result = getWeatherDataUseCase.execute(40.4165, -3.7026)
```

### 3. Suspend Functions

All use cases MUST be suspend functions:

```kotlin
// ✅ GOOD - Suspend function
class GetWeatherDataUseCase {
    suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult {
        return repository.getWeatherData(lat, lon)
    }
}

// ❌ BAD - Blocking function
class GetWeatherDataUseCase {
    fun invoke(lat: Double, lon: Double): WeatherDataResult = runBlocking {
        repository.getWeatherData(lat, lon)
    }
}
```

### 4. No Android Dependencies

Use cases MUST NOT depend on Android framework:

```kotlin
// ✅ GOOD - Pure Kotlin
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) =
        repository.getWeatherData(lat, lon)
}

// ❌ BAD - Android dependency
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository,
    private val context: Context  // ❌ Android dependency
) {
    suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult {
        // ...
    }
}
```

### 5. Minimal Logic

Use cases should contain minimal logic (coordination only):

```kotlin
// ✅ GOOD - Simple delegation
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) =
        repository.getWeatherData(lat, lon)
}

// ⚠️ ACCEPTABLE - Simple coordination
class GetWeatherByCityUseCase(
    private val geoRepository: WeatherGeoRepository,
    private val weatherRepository: WeatherDataRepository
) {
    suspend operator fun invoke(city: String): WeatherDataResult {
        return when (val geoResult = geoRepository.getGeoData(city)) {
            is WeatherGeoResult.Data ->
                weatherRepository.getWeatherData(geoResult.data.lat, geoResult.data.lon)
            is WeatherGeoResult.Error ->
                WeatherDataResult.Error(geoResult.error)
        }
    }
}

// ❌ BAD - Too much logic
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository,
    private val cache: Cache,
    private val analytics: Analytics
) {
    suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult {
        // Too much logic - should be in repository or separate use cases
        val cacheKey = "$lat,$lon"
        val cached = cache.get(cacheKey)
        if (cached != null && cached.isValid()) {
            analytics.log("cache_hit")
            return WeatherDataResult.Data(cached.data)
        }

        analytics.log("cache_miss")
        val result = repository.getWeatherData(lat, lon)

        if (result is WeatherDataResult.Data) {
            cache.put(cacheKey, result.data)
        }

        return result
    }
}
```

### 6. Factory Pattern via Koin

Use dependency injection (Koin) with `factory` scope:

```kotlin
val domainModule = module {
    // Use 'factory' for use cases (new instance each time)
    factory { GetWeatherDataUseCase(get()) }
    factory { GetWeatherGeoDataUseCase(get()) }

    // NOT 'single' - use cases should not be singletons
    // single { GetWeatherDataUseCase(get()) } // ❌ WRONG
}
```

**Why `factory`?**
- Use cases are stateless
- Lightweight objects (no need to reuse instances)
- Prevents accidental state sharing between components

---

## Testing Contracts

### Unit Test Requirements

All use cases MUST have unit tests with 90%+ coverage (they're simple):

```kotlin
class GetWeatherDataUseCaseTest {

    private lateinit var useCase: GetWeatherDataUseCase
    private lateinit var mockRepository: WeatherDataRepository

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetWeatherDataUseCase(mockRepository)
    }

    @Test
    fun `invoke returns Data when repository returns success`() = runTest {
        // Given
        val expectedData = WeatherDataEntity(/* ... */)
        val repositoryResult = WeatherDataResult.Data(expectedData)
        coEvery { mockRepository.getWeatherData(any(), any()) } returns repositoryResult

        // When
        val result = useCase(40.4165, -3.7026)

        // Then
        assertTrue(result is WeatherDataResult.Data)
        assertEquals(expectedData, (result as WeatherDataResult.Data).data)
        coVerify(exactly = 1) { mockRepository.getWeatherData(40.4165, -3.7026) }
    }

    @Test
    fun `invoke returns Error when repository returns error`() = runTest {
        // Given
        val expectedError = WeatherConectionException("Network error")
        val repositoryResult = WeatherDataResult.Error(expectedError)
        coEvery { mockRepository.getWeatherData(any(), any()) } returns repositoryResult

        // When
        val result = useCase(40.4165, -3.7026)

        // Then
        assertTrue(result is WeatherDataResult.Error)
        assertEquals(expectedError, (result as WeatherDataResult.Error).error)
    }

    @Test
    fun `invoke passes correct parameters to repository`() = runTest {
        // Given
        val lat = 40.4165
        val lon = -3.7026
        coEvery { mockRepository.getWeatherData(lat, lon) } returns
            WeatherDataResult.Data(WeatherDataEntity(/* ... */))

        // When
        useCase(lat, lon)

        // Then
        coVerify { mockRepository.getWeatherData(lat, lon) }
    }
}
```

### Test Coverage Checklist

For **GetWeatherDataUseCase**:
- [ ] Returns Data when repository returns success
- [ ] Returns Error when repository returns error
- [ ] Passes correct parameters to repository
- [ ] Is suspending function (can be tested in coroutine)
- [ ] Does not throw exceptions

For **GetWeatherGeoDataUseCase**:
- [ ] Returns Data when repository returns success
- [ ] Returns Error when repository returns error (city not found)
- [ ] Returns Error when repository returns network error
- [ ] Passes correct city name to repository
- [ ] Is suspending function (can be tested in coroutine)
- [ ] Does not throw exceptions

### Mock Data

```kotlin
object UseCaseTestData {

    fun createWeatherDataEntity(
        temp: Double = 25.5,
        cityName: String = "Madrid"
    ) = WeatherDataEntity(
        coord = WeatherCoordEntity(lon = -3.7026, lat = 40.4165),
        weather = listOf(
            WeatherEntity(
                id = 800,
                main = "Clear",
                description = "clear sky",
                icon = "01d"
            )
        ),
        main = WeatherMainEntity(temp = temp, humidity = 60),
        wind = WeatherWindEntity(speed = 3.5),
        name = cityName,
        cod = 200,
        base = "stations",
        visibility = 10000,
        clouds = WeatherCloudsEntity(all = 0),
        dt = 1698595200,
        sys = WeatherSysEntity(country = "ES"),
        timezone = 3600,
        id = 3117735
    )

    fun createWeatherGeoDataEntity(
        cityName: String = "Madrid",
        lat: Double = 40.4165,
        lon: Double = -3.7026
    ) = WeatherGeoDataEntity(
        name = cityName,
        lat = lat,
        lon = lon,
        country = "ES",
        localNames = LocalNamesEntity(es = cityName, en = cityName)
    )
}
```

---

## Usage Examples

### Example 1: Simple Weather Fetch

```kotlin
class WeatherDataScreenModel(
    private val getWeatherDataUseCase: GetWeatherDataUseCase
) : MVIBaseScreenModel<State, Event, Effect>() {

    fun loadWeather(lat: Double, lon: Double) {
        screenModelScope.launch {
            // Show loading
            updateState { copy(loadingWeather = true) }

            // Execute use case
            val result = getWeatherDataUseCase(lat, lon)

            // Handle result
            when (result) {
                is WeatherDataResult.Data -> {
                    updateState {
                        copy(
                            loadingWeather = false,
                            weatherData = result.data
                        )
                    }
                }

                is WeatherDataResult.Error -> {
                    updateState { copy(loadingWeather = false) }
                    sendEffect(Effect.ShowError(result.error))
                }
            }
        }
    }
}
```

### Example 2: City Search with Chaining

```kotlin
class HomeScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase
) : MVIBaseScreenModel<State, Event, Effect>() {

    fun searchAndLoadWeather(cityName: String) {
        screenModelScope.launch {
            // Step 1: Get coordinates
            updateState { copy(loadingGeo = true) }

            when (val geoResult = getWeatherGeoDataUseCase(cityName)) {
                is WeatherGeoResult.Data -> {
                    val location = geoResult.data
                    updateState {
                        copy(
                            loadingGeo = false,
                            geoData = location
                        )
                    }

                    // Step 2: Get weather for coordinates
                    updateState { copy(loadingWeather = true) }

                    when (val weatherResult = getWeatherDataUseCase(location.lat, location.lon)) {
                        is WeatherDataResult.Data -> {
                            updateState {
                                copy(
                                    loadingWeather = false,
                                    weatherData = weatherResult.data
                                )
                            }
                        }

                        is WeatherDataResult.Error -> {
                            updateState { copy(loadingWeather = false) }
                            sendEffect(Effect.ShowError(weatherResult.error))
                        }
                    }
                }

                is WeatherGeoResult.Error -> {
                    updateState { copy(loadingGeo = false) }
                    sendEffect(Effect.ShowError(geoResult.error))
                }
            }
        }
    }
}
```

### Example 3: Retry with Exponential Backoff

```kotlin
class WeatherDataScreenModel(
    private val getWeatherDataUseCase: GetWeatherDataUseCase
) : MVIBaseScreenModel<State, Event, Effect>() {

    fun loadWeatherWithRetry(lat: Double, lon: Double, maxRetries: Int = 3) {
        screenModelScope.launch {
            updateState { copy(loadingWeather = true) }

            var attempt = 0
            var delay = 1000L

            while (attempt < maxRetries) {
                val result = getWeatherDataUseCase(lat, lon)

                when (result) {
                    is WeatherDataResult.Data -> {
                        updateState {
                            copy(
                                loadingWeather = false,
                                weatherData = result.data
                            )
                        }
                        return@launch // Success, exit
                    }

                    is WeatherDataResult.Error -> {
                        if (result.error is WeatherConectionException && attempt < maxRetries - 1) {
                            // Retry on connection errors
                            attempt++
                            kotlinx.coroutines.delay(delay)
                            delay *= 2 // Exponential backoff
                        } else {
                            // Give up
                            updateState { copy(loadingWeather = false) }
                            sendEffect(Effect.ShowError(result.error))
                            return@launch
                        }
                    }
                }
            }
        }
    }
}
```

### Example 4: Parallel Requests

```kotlin
class MultiCityWeatherScreenModel(
    private val getWeatherGeoDataUseCase: GetWeatherGeoDataUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase
) : MVIBaseScreenModel<State, Event, Effect>() {

    fun loadMultipleCities(cities: List<String>) {
        screenModelScope.launch {
            updateState { copy(loading = true) }

            // Execute all searches in parallel
            val results = cities.map { city ->
                async {
                    // Get coordinates
                    val geoResult = getWeatherGeoDataUseCase(city)
                    if (geoResult is WeatherGeoResult.Data) {
                        // Get weather
                        getWeatherDataUseCase(geoResult.data.lat, geoResult.data.lon)
                    } else {
                        null
                    }
                }
            }.awaitAll()

            // Filter successful results
            val weatherData = results.filterNotNull()
                .filterIsInstance<WeatherDataResult.Data>()
                .map { it.data }

            updateState {
                copy(
                    loading = false,
                    citiesWeather = weatherData
                )
            }
        }
    }
}
```

### Example 5: Flow-based Use Case (Alternative Pattern)

**Note**: Current implementation uses suspend functions, but here's how you could convert to Flow-based for reactive streams:

```kotlin
// Alternative pattern (not currently used)
class GetWeatherDataUseCaseFlow(
    private val repository: WeatherDataRepository
) {
    operator fun invoke(lat: Double, lon: Double): Flow<WeatherDataResult> = flow {
        // Emit loading state (optional)
        // emit(WeatherDataResult.Loading)

        // Fetch data
        val result = repository.getWeatherData(lat, lon)

        // Emit result
        emit(result)
    }.flowOn(Dispatchers.IO)
}

// Usage with Flow
class WeatherScreenModel {
    fun loadWeather(lat: Double, lon: Double) {
        getWeatherDataUseCase(lat, lon)
            .onEach { result ->
                when (result) {
                    is WeatherDataResult.Data -> {
                        updateState { copy(weatherData = result.data) }
                    }
                    is WeatherDataResult.Error -> {
                        sendEffect(Effect.ShowError(result.error))
                    }
                }
            }
            .launchIn(screenModelScope)
    }
}
```

---

## Best Practices Summary

### DO ✅

- Use `operator fun invoke()` for callable syntax
- Make all use cases suspend functions
- Keep use cases stateless
- Delegate validation to repositories
- Use dependency injection (Koin `factory`)
- Write comprehensive unit tests (90%+ coverage)
- Keep use cases simple (single responsibility)
- Return domain result types (never throw exceptions)

### DON'T ❌

- Don't add Android dependencies to use cases
- Don't add complex business logic to use cases
- Don't make use cases stateful
- Don't use `runBlocking` in use cases
- Don't perform validation in use cases (delegate to repo)
- Don't implement caching in use cases
- Don't make use cases singletons (`single` scope)
- Don't throw exceptions from use cases

---

## Appendix: Complete Use Case Examples

### Complete GetWeatherDataUseCase

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.WeatherDataResult
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository

/**
 * Use case for retrieving current weather data by coordinates.
 *
 * @property repository The weather data repository
 */
class GetWeatherDataUseCase(
    private val repository: WeatherDataRepository
) {
    /**
     * Retrieves weather data for the specified coordinates.
     *
     * @param lat Latitude in decimal degrees [-90.0, 90.0]
     * @param lon Longitude in decimal degrees [-180.0, 180.0]
     * @return Weather data result (Data or Error)
     */
    suspend operator fun invoke(lat: Double, lon: Double): WeatherDataResult =
        repository.getWeatherData(lat, lon)
}
```

### Complete GetWeatherGeoDataUseCase

```kotlin
package com.mtzdev.mywheatherapp.domain.usecase

import com.mtzdev.mywheatherapp.domain.WeatherGeoResult
import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository

/**
 * Use case for searching location data by city name.
 *
 * @property weatherGeoRepository The geocoding repository
 */
class GetWeatherGeoDataUseCase(
    private val weatherGeoRepository: WeatherGeoRepository
) {
    /**
     * Searches for location by city name.
     *
     * @param city City name to search (can include country code)
     * @return Location data result (Data or Error)
     */
    suspend operator fun invoke(city: String): WeatherGeoResult =
        weatherGeoRepository.getGeoData(city)
}
```

---

**Document Status**: Complete
**Reviewed By**: Development Team
**Next Review**: 2025-11-29
