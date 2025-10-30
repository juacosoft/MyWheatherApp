# MVI Contracts Documentation

**Project**: MyWeatherApp
**Feature**: Clima Actual con GPS
**Version**: 1.0.0
**Last Updated**: 2025-10-29

---

## Table of Contents

1. [Overview](#overview)
2. [MVIContract Base Interface](#mvicontract-base-interface)
3. [WeatherDataContract](#weatherdatacontract)
4. [HomeContract](#homecontract)
5. [State Machine Diagrams](#state-machine-diagrams)
6. [Event-State Mapping](#event-state-mapping)
7. [Effect Triggering](#effect-triggering)
8. [Implementation Guidelines](#implementation-guidelines)
9. [Testing Contracts](#testing-contracts)

---

## Overview

MVI (Model-View-Intent) is the architectural pattern used for the presentation layer in MyWeatherApp. This document defines the complete contracts for state management, user events, and side effects.

### MVI Pattern Overview

```
┌──────────────────────────────────────────────────┐
│                    View                          │
│              (Composables)                       │
│                                                  │
│  Renders State │ Emits Events │ Observes Effects│
└──────────────────────────────────────────────────┘
         │              ▲                ▲
         │ Event        │ State          │ Effect
         ▼              │                │
┌──────────────────────────────────────────────────┐
│                ScreenModel                       │
│          (MVIBaseScreenModel)                    │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │  Event   │  │  State   │  │  Effect  │      │
│  │ Handler  │→ │  Update  │  │ Emitter  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│         │            │               │           │
│         ▼            ▼               ▼           │
│  ┌────────────────────────────────────────┐     │
│  │         Use Cases                      │     │
│  │    (Business Logic)                    │     │
│  └────────────────────────────────────────┘     │
└──────────────────────────────────────────────────┘
```

### Key Principles

1. **Unidirectional Data Flow**: View → Event → ScreenModel → State → View
2. **Immutable State**: State is read-only from View
3. **Single Source of Truth**: ScreenModel holds the state
4. **Side Effects**: Effects are one-time events (navigation, toasts, etc.)
5. **Thread Safety**: All state updates are thread-safe

---

## MVIContract Base Interface

### File Location

```
/app/src/main/java/com/mtzdev/mywheatherapp/commons/MVIContract.kt
```

### Interface Definition

```kotlin
package com.mtzdev.mywheatherapp.commons

/**
 * Base contract for MVI (Model-View-Intent) pattern implementation.
 *
 * This interface defines marker interfaces for the three core components of MVI:
 * - UiState: Represents the UI state
 * - UiEvent: Represents user intentions/events
 * - Effect: Represents side effects (one-time events)
 *
 * ## Design Principles
 * - **Immutability**: All UiState implementations should be immutable (data classes)
 * - **Sealed Classes**: UiEvent and Effect should be sealed interfaces/classes
 * - **Thread Safety**: All implementations must be thread-safe
 * - **No Logic**: These are pure data containers, no business logic
 *
 * ## Usage
 * Feature contracts should extend these marker interfaces:
 * ```kotlin
 * interface WeatherContract {
 *     data class State(...) : MVIContract.UiState
 *     sealed interface Event : MVIContract.UiEvent { ... }
 *     sealed interface Effect : MVIContract.Effect { ... }
 * }
 * ```
 *
 * @since 1.0.0
 */
interface MVIContract {

    /**
     * Marker interface for UI State.
     *
     * ## Contract Requirements
     * - MUST be implemented as immutable data class
     * - MUST contain all UI-related state
     * - MUST have default values for all properties
     * - SHOULD use Kotlin data classes (for copy() method)
     * - MUST be thread-safe (no mutable collections)
     *
     * ## Example
     * ```kotlin
     * data class State(
     *     val isLoading: Boolean = false,
     *     val data: MyData? = null,
     *     val error: String? = null
     * ) : MVIContract.UiState
     * ```
     */
    interface UiState

    /**
     * Marker interface for UI Events (User Intents).
     *
     * ## Contract Requirements
     * - MUST be implemented as sealed interface/class
     * - MUST represent user actions or system events
     * - SHOULD be immutable (use data classes or objects)
     * - SHOULD have descriptive names (verbs)
     *
     * ## Example
     * ```kotlin
     * sealed interface Event : MVIContract.UiEvent {
     *     data object LoadData : Event
     *     data class SearchQuery(val query: String) : Event
     *     data object Retry : Event
     * }
     * ```
     */
    interface UiEvent

    /**
     * Marker interface for Side Effects.
     *
     * ## Contract Requirements
     * - MUST be implemented as sealed interface/class
     * - MUST represent one-time events (not state)
     * - SHOULD be immutable (use data classes or objects)
     * - SHOULD be consumed only once by the View
     *
     * ## Example
     * ```kotlin
     * sealed interface Effect : MVIContract.Effect {
     *     data object NavigateBack : Effect
     *     data class ShowToast(val message: String) : Effect
     *     data object RequestPermission : Effect
     * }
     * ```
     */
    interface Effect
}
```

### Marker Interface Pattern

The MVIContract uses the **marker interface** pattern:

- **Purpose**: Type safety and contract enforcement
- **Benefit**: Ensures all feature contracts follow the same structure
- **Implementation**: All screen contracts implement these base interfaces

---

## WeatherDataContract

### Purpose

Defines the MVI contract for the Weather Data screen, which displays current weather information for a specific location.

### File Location

```
/app/src/main/java/com/mtzdev/mywheatherapp/ui/screen/weatherdata/WeatherDataContract.kt
```

### Complete Contract Definition

```kotlin
package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherDataEntity
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

/**
 * MVI Contract for Weather Data Screen.
 *
 * This contract defines the complete MVI pattern for the weather display screen:
 * - State: Current weather data and loading states
 * - Events: User actions (init, refresh)
 * - Effects: One-time side effects (show errors)
 *
 * @since 1.0.0
 */
interface WeatherDataContract {

    /**
     * UI State for Weather Data Screen.
     *
     * ## State Properties
     * - `loadingWeather`: Indicates weather data is being fetched
     * - `currentGeoData`: Geographic location information
     * - `weatherData`: Current weather information
     * - `colors`: Theme colors based on weather condition
     *
     * ## State Invariants
     * - When `loadingWeather` is true, `weatherData` may be null or stale
     * - `currentGeoData` should be set before fetching weather
     * - `colors` should update when `weatherData` changes
     *
     * ## Thread Safety
     * This is an immutable data class, safe to share across threads.
     */
    data class State(
        /**
         * Indicates if weather data is currently being loaded.
         * - `true`: API request in progress
         * - `false`: No active request
         */
        val loadingWeather: Boolean = true,

        /**
         * Current geographic location data.
         * Set via Init event with coordinates and city name.
         */
        val currentGeoData: WeatherGeoDataEntity? = null,

        /**
         * Current weather data.
         * - `null`: No data loaded yet or error occurred
         * - Non-null: Weather data successfully loaded
         */
        val weatherData: WeatherDataEntity? = null,

        /**
         * Color theme based on weather condition.
         * Updates automatically when weatherData changes.
         */
        val colors: WeatherColors = WeatherColors.NO_DATA
    ) : MVIContract.UiState

    /**
     * UI Events for Weather Data Screen.
     *
     * ## Event Types
     * - Init: Initialize screen with location data
     * - RefreshData: Refresh weather data for current location
     *
     * ## Event Handling
     * All events are handled by WeatherDataScreenModel.handleEvent()
     */
    sealed interface Event : MVIContract.UiEvent {

        /**
         * Initialize the screen with location data.
         *
         * ## When to Emit
         * - Screen is first opened/navigated to
         * - Location changes from parent screen
         *
         * ## Expected Behavior
         * 1. Update state with currentGeoData
         * 2. Trigger weather data fetch
         * 3. Update loading state
         *
         * @property currentGeoData Location to fetch weather for
         */
        data class Init(val currentGeoData: WeatherGeoDataEntity) : Event

        /**
         * Refresh weather data for current location.
         *
         * ## When to Emit
         * - User pulls to refresh
         * - User taps refresh button
         * - Auto-refresh timer expires
         *
         * ## Expected Behavior
         * 1. Re-fetch weather data using currentGeoData
         * 2. Update loading state
         * 3. Update weatherData on success
         * 4. Show error effect on failure
         *
         * ## Preconditions
         * - currentGeoData must not be null
         */
        data object RefreshData : Event
    }

    /**
     * Side Effects for Weather Data Screen.
     *
     * ## Effect Types
     * - ShowError: Display error message to user
     *
     * ## Effect Consumption
     * Effects are one-time events and should be consumed only once by the View.
     */
    sealed interface Effect : MVIContract.Effect {

        /**
         * Show error message to user.
         *
         * ## When Triggered
         * - Weather data fetch fails
         * - Network error occurs
         * - Invalid response from API
         *
         * ## View Behavior
         * - Display error in Snackbar or Toast
         * - Log error to analytics
         * - Consume effect immediately
         *
         * @property errorType The specific error that occurred
         */
        data class ShowError(val errorType: WeatherException) : Effect
    }
}
```

### State Details

#### State Properties

| Property | Type | Default | Purpose | Nullable |
|----------|------|---------|---------|----------|
| `loadingWeather` | Boolean | `true` | Indicates loading state | No |
| `currentGeoData` | WeatherGeoDataEntity | `null` | Current location data | Yes |
| `weatherData` | WeatherDataEntity | `null` | Current weather data | Yes |
| `colors` | WeatherColors | `NO_DATA` | UI theme colors | No |

#### State Transitions

```
Initial State:
  loadingWeather = true
  currentGeoData = null
  weatherData = null
  colors = NO_DATA

After Init Event:
  loadingWeather = true
  currentGeoData = <location>
  weatherData = null
  colors = NO_DATA

After Weather Load Success:
  loadingWeather = false
  currentGeoData = <location>
  weatherData = <weather>
  colors = <based on weather>

After Weather Load Error:
  loadingWeather = false
  currentGeoData = <location>
  weatherData = null
  colors = NO_DATA
```

### Event Details

#### Init Event

**Trigger**: Screen navigation or location change

**Handler Logic**:
```kotlin
private fun handleInit(currentGeoData: WeatherGeoDataEntity) {
    screenModelScope.launch {
        // Update state with location
        updateState {
            copy(
                currentGeoData = currentGeoData,
                loadingWeather = true
            )
        }

        // Fetch weather data
        when (val result = getWeatherDataUseCase(currentGeoData.lat, currentGeoData.lon)) {
            is WeatherDataResult.Data -> {
                updateState {
                    copy(
                        loadingWeather = false,
                        weatherData = result.data,
                        colors = WeatherColors.fromWeather(result.data)
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
```

#### RefreshData Event

**Trigger**: Pull-to-refresh or refresh button

**Handler Logic**:
```kotlin
private fun handleRefreshData() {
    val currentGeo = currentState.currentGeoData ?: return

    screenModelScope.launch {
        updateState { copy(loadingWeather = true) }

        when (val result = getWeatherDataUseCase(currentGeo.lat, currentGeo.lon)) {
            is WeatherDataResult.Data -> {
                updateState {
                    copy(
                        loadingWeather = false,
                        weatherData = result.data,
                        colors = WeatherColors.fromWeather(result.data)
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
```

---

## HomeContract

### Purpose

Defines the MVI contract for the Home screen, which handles location search and selection.

### File Location

```
/app/src/main/java/com/mtzdev/mywheatherapp/ui/screen/home/HomeContract.kt
```

### Complete Contract Definition

```kotlin
package com.mtzdev.mywheatherapp.ui.screen.home

import com.mtzdev.mywheatherapp.commons.MVIContract
import com.mtzdev.mywheatherapp.commons.WeatherException
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity

/**
 * MVI Contract for Home Screen.
 *
 * This contract defines the MVI pattern for the home/search screen:
 * - State: Location data and loading states
 * - Events: User actions (search, navigation)
 * - Effects: Side effects (errors, navigation)
 *
 * @since 1.0.0
 */
interface HomeContract : MVIContract {

    /**
     * UI State for Home Screen.
     *
     * ## State Properties
     * - `loadingGeo`: Indicates location search in progress
     * - `geoData`: Current selected location
     *
     * ## State Invariants
     * - `geoData` must always be set (has default)
     * - When `loadingGeo` is true, UI should show loading indicator
     */
    data class State(
        /**
         * Indicates if location search is in progress.
         * - `true`: Geocoding API request active
         * - `false`: No active search
         */
        val loadingGeo: Boolean = false,

        /**
         * Current selected geographic location.
         * Updated when user searches for a city or uses GPS.
         */
        val geoData: WeatherGeoDataEntity
    ) : MVIContract.UiState

    /**
     * UI Events for Home Screen.
     *
     * ## Event Types
     * - ChangeUbication: Search for a new city
     * - NavigateToNotification: Navigate to notifications
     * - NavigateToSettings: Navigate to settings
     */
    sealed interface Event : MVIContract.UiEvent {

        /**
         * Search for a city and update location.
         *
         * ## When to Emit
         * - User submits city search
         * - User selects location from list
         *
         * ## Expected Behavior
         * 1. Update loading state
         * 2. Call geocoding use case
         * 3. Update geoData on success
         * 4. Show error effect on failure
         *
         * @property city City name to search for
         */
        data class ChangeUbication(val city: String) : Event

        /**
         * Navigate to notifications screen.
         *
         * ## When to Emit
         * - User taps notification icon
         *
         * ## Expected Behavior
         * - Emit NavigateToNotification effect
         */
        data object NavigateToNotification : Event

        /**
         * Navigate to settings screen.
         *
         * ## When to Emit
         * - User taps settings icon
         *
         * ## Expected Behavior
         * - Emit NavigateToSettings effect
         */
        data object NavigateToSettings : Event
    }

    /**
     * Side Effects for Home Screen.
     *
     * ## Effect Types
     * - ShowError: Display error message
     * - NavigateToNotification: Navigate to notifications
     * - NavigateToSettings: Navigate to settings
     */
    sealed interface Effect : MVIContract.Effect {

        /**
         * Show error message to user.
         *
         * @property errorType The specific error that occurred
         */
        data class ShowError(val errorType: WeatherException) : Effect

        /**
         * Navigate to notification screen.
         *
         * ## View Behavior
         * - Use navigation controller to navigate
         * - Consume effect immediately
         */
        data object NavigateToNotification : Effect

        /**
         * Navigate to settings screen.
         *
         * ## View Behavior
         * - Use navigation controller to navigate
         * - Consume effect immediately
         */
        data object NavigateToSettings : Effect
    }
}
```

### State Details

#### State Properties

| Property | Type | Default | Purpose | Nullable |
|----------|------|---------|---------|----------|
| `loadingGeo` | Boolean | `false` | Location search loading state | No |
| `geoData` | WeatherGeoDataEntity | (required) | Current selected location | No |

**Note**: `geoData` is NOT nullable and has no default. It must be provided when creating initial state.

---

## State Machine Diagrams

### WeatherDataContract State Machine

```
                     ┌─────────────┐
                     │   INITIAL   │
                     │             │
                     │ loading=true│
                     │ data=null   │
                     └──────┬──────┘
                            │
                    Init Event (with location)
                            │
                            ▼
                     ┌─────────────┐
                     │   LOADING   │
                     │             │
                     │ loading=true│
                     │ geo=set     │
                     └──────┬──────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
     Weather Load Success        Weather Load Error
              │                           │
              ▼                           ▼
       ┌─────────────┐             ┌─────────────┐
       │   SUCCESS   │             │    ERROR    │
       │             │             │             │
       │ loading=false│            │ loading=false│
       │ data=set    │             │ data=null   │
       │ colors=set  │             │ + ShowError │
       └──────┬──────┘             └──────┬──────┘
              │                           │
              │         RefreshData       │
              └───────────┬───────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │ REFRESHING  │
                   │             │
                   │ loading=true│
                   │ data=stale  │
                   └─────────────┘
```

### HomeContract State Machine

```
                     ┌─────────────┐
                     │   INITIAL   │
                     │             │
                     │ loading=false│
                     │ geo=default │
                     └──────┬──────┘
                            │
                ChangeUbication Event (city)
                            │
                            ▼
                     ┌─────────────┐
                     │  SEARCHING  │
                     │             │
                     │ loading=true│
                     │ geo=old     │
                     └──────┬──────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
      City Search Success         City Search Error
              │                           │
              ▼                           ▼
       ┌─────────────┐             ┌─────────────┐
       │   UPDATED   │             │    ERROR    │
       │             │             │             │
       │ loading=false│            │ loading=false│
       │ geo=new     │             │ geo=old     │
       └─────────────┘             │ + ShowError │
                                   └─────────────┘
```

---

## Event-State Mapping

### WeatherDataContract Event → State Mapping

| Event | State Before | State After | Side Effects |
|-------|--------------|-------------|--------------|
| **Init** | `loading=true, geo=null, data=null` | `loading=true, geo=set, data=null` | None |
| Init → Success | `loading=true, geo=set, data=null` | `loading=false, geo=set, data=set, colors=set` | None |
| Init → Error | `loading=true, geo=set, data=null` | `loading=false, geo=set, data=null` | ShowError |
| **RefreshData** | `loading=false, data=stale` | `loading=true, data=stale` | None |
| RefreshData → Success | `loading=true, data=stale` | `loading=false, data=new, colors=updated` | None |
| RefreshData → Error | `loading=true, data=stale` | `loading=false, data=stale` | ShowError |

### HomeContract Event → State Mapping

| Event | State Before | State After | Side Effects |
|-------|--------------|-------------|--------------|
| **ChangeUbication** | `loading=false, geo=old` | `loading=true, geo=old` | None |
| ChangeUbication → Success | `loading=true, geo=old` | `loading=false, geo=new` | None |
| ChangeUbication → Error | `loading=true, geo=old` | `loading=false, geo=old` | ShowError |
| **NavigateToNotification** | (any state) | (no change) | NavigateToNotification |
| **NavigateToSettings** | (any state) | (no change) | NavigateToSettings |

---

## Effect Triggering

### Effect Trigger Conditions

| Effect | Contract | Trigger Condition | Handler Action |
|--------|----------|-------------------|----------------|
| **ShowError** | WeatherData | Weather fetch fails | Display error in Snackbar |
| **ShowError** | WeatherData | Network timeout | Display "Connection error" message |
| **ShowError** | Home | City search fails | Display "City not found" message |
| **ShowError** | Home | Geocoding API error | Display generic error message |
| **NavigateToNotification** | Home | User taps notification icon | Navigate to notification screen |
| **NavigateToSettings** | Home | User taps settings icon | Navigate to settings screen |

### Effect Consumption Pattern

Effects MUST be consumed only once:

```kotlin
// In Composable
LaunchedEffect(Unit) {
    screenModel.effect.collect { effect ->
        when (effect) {
            is WeatherDataContract.Effect.ShowError -> {
                // Show error (consumed once)
                snackbarHostState.showSnackbar(
                    message = effect.errorType.message ?: "Unknown error",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
}
```

**Why once?**
- Effects are events, not state
- Navigation should not trigger multiple times
- Snackbars/Toasts should show once per effect

---

## Implementation Guidelines

### Creating a New Contract

```kotlin
interface MyFeatureContract {

    // 1. Define State (immutable data class)
    data class State(
        val isLoading: Boolean = false,
        val data: MyData? = null,
        val error: String? = null
    ) : MVIContract.UiState

    // 2. Define Events (sealed interface)
    sealed interface Event : MVIContract.UiEvent {
        data object LoadData : Event
        data class UpdateFilter(val filter: String) : Event
        data object Retry : Event
    }

    // 3. Define Effects (sealed interface)
    sealed interface Effect : MVIContract.Effect {
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
```

### Implementing ScreenModel

```kotlin
class MyFeatureScreenModel(
    private val useCase: MyUseCase
) : MVIBaseScreenModel<
    MyFeatureContract.State,
    MyFeatureContract.Event,
    MyFeatureContract.Effect
>() {

    // 1. Create initial state
    override fun createInitialState(): MyFeatureContract.State {
        return MyFeatureContract.State()
    }

    // 2. Handle events
    override fun handleEvent(event: MyFeatureContract.Event) {
        when (event) {
            is MyFeatureContract.Event.LoadData -> handleLoadData()
            is MyFeatureContract.Event.UpdateFilter -> handleUpdateFilter(event.filter)
            is MyFeatureContract.Event.Retry -> handleRetry()
        }
    }

    // 3. Implement event handlers
    private fun handleLoadData() {
        screenModelScope.launch {
            updateState { copy(isLoading = true) }

            when (val result = useCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            data = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    sendEffect(MyFeatureContract.Effect.ShowError(result.message))
                }
            }
        }
    }
}
```

### Consuming in View

```kotlin
@Composable
fun MyFeatureScreen(
    screenModel: MyFeatureScreenModel = getScreenModel()
) {
    val state by screenModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects (one-time events)
    LaunchedEffect(Unit) {
        screenModel.effect.collect { effect ->
            when (effect) {
                is MyFeatureContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MyFeatureContract.Effect.NavigateBack -> {
                    // Navigate back
                }
            }
        }
    }

    // Render UI based on state
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(
                error = state.error!!,
                onRetry = { screenModel.onEvent(MyFeatureContract.Event.Retry) }
            )
            state.data != null -> DataDisplay(data = state.data!!)
        }
    }
}
```

---

## Testing Contracts

### State Testing

```kotlin
@Test
fun `initial state has correct defaults`() {
    val state = WeatherDataContract.State()

    assertTrue(state.loadingWeather)
    assertNull(state.currentGeoData)
    assertNull(state.weatherData)
    assertEquals(WeatherColors.NO_DATA, state.colors)
}

@Test
fun `state copy preserves immutability`() {
    val state1 = WeatherDataContract.State(loadingWeather = true)
    val state2 = state1.copy(loadingWeather = false)

    assertTrue(state1.loadingWeather)
    assertFalse(state2.loadingWeather)
}
```

### Event Handling Testing

```kotlin
@Test
fun `Init event updates state correctly`() = runTest {
    val screenModel = WeatherDataScreenModel(mockUseCase)
    val geoData = WeatherGeoDataEntity(/* ... */)

    screenModel.onEvent(WeatherDataContract.Event.Init(geoData))

    val state = screenModel.state.value
    assertEquals(geoData, state.currentGeoData)
    assertTrue(state.loadingWeather)
}

@Test
fun `RefreshData event fetches new data`() = runTest {
    val screenModel = WeatherDataScreenModel(mockUseCase)

    // Setup initial state
    screenModel.onEvent(WeatherDataContract.Event.Init(geoData))

    // Trigger refresh
    screenModel.onEvent(WeatherDataContract.Event.RefreshData)

    // Verify use case was called again
    coVerify(exactly = 2) { mockUseCase(any(), any()) }
}
```

### Effect Testing

```kotlin
@Test
fun `ShowError effect is emitted on failure`() = runTest {
    val error = WeatherConectionException("Network error")
    coEvery { mockUseCase(any(), any()) } returns WeatherDataResult.Error(error)

    val screenModel = WeatherDataScreenModel(mockUseCase)
    val effects = mutableListOf<WeatherDataContract.Effect>()

    // Collect effects
    val job = launch {
        screenModel.effect.collect { effects.add(it) }
    }

    // Trigger event
    screenModel.onEvent(WeatherDataContract.Event.Init(geoData))

    advanceUntilIdle()

    // Verify effect
    assertEquals(1, effects.size)
    assertTrue(effects.first() is WeatherDataContract.Effect.ShowError)
    assertEquals(error, (effects.first() as WeatherDataContract.Effect.ShowError).errorType)

    job.cancel()
}
```

---

## Best Practices

### State Design

**DO** ✅:
- Use immutable data classes for State
- Provide default values for all properties
- Use nullable types only when truly optional
- Keep state flat (avoid nested state objects)
- Include loading and error states

**DON'T** ❌:
- Use mutable collections in state
- Add business logic to state
- Make state classes too large (split if needed)
- Use var properties in state

### Event Design

**DO** ✅:
- Use sealed interfaces for events
- Name events with verbs (LoadData, RefreshData)
- Include all necessary data in event
- Keep events focused (single responsibility)

**DON'T** ❌:
- Add business logic to events
- Make events too generic
- Use events for navigation (use effects)
- Emit events from ScreenModel

### Effect Design

**DO** ✅:
- Use effects for one-time events
- Use effects for navigation
- Use effects for showing toasts/snackbars
- Consume effects only once in View

**DON'T** ❌:
- Use effects for state updates
- Emit multiple effects rapidly
- Store effect history
- Ignore unconsumed effects

---

## Thread Safety Guarantees

### State Updates

State updates are thread-safe through MVIBaseScreenModel:

```kotlin
// Thread-safe state update
updateState { currentState ->
    currentState.copy(loadingWeather = false)
}
```

### Effect Emission

Effects are emitted through a thread-safe channel:

```kotlin
// Thread-safe effect emission
sendEffect(WeatherDataContract.Effect.ShowError(error))
```

### Immutability

All state objects MUST be immutable:

```kotlin
// ✅ GOOD - Immutable
data class State(
    val data: List<Item> = emptyList()  // Immutable list
)

// ❌ BAD - Mutable
data class State(
    val data: MutableList<Item> = mutableListOf()  // Mutable!
)
```

---

## Appendix: Complete Contract Examples

### Complete WeatherDataContract Implementation

See [WeatherDataContract.kt](#complete-contract-definition) above.

### Complete HomeContract Implementation

See [HomeContract.kt](#complete-contract-definition-1) above.

---

**Document Status**: Complete
**Reviewed By**: Development Team
**Next Review**: 2025-11-29
