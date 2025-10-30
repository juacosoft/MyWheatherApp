# Tasks: Clima Actual con GPS

**Feature ID**: 001-clima-actual-gps
**Branch**: `001-clima-actual-gps`
**Status**: Ready for Implementation
**Target Coverage**: 80%+ (Data & Domain layers)
**Estimated Duration**: 2-3 weeks (10-15 business days)

---

## Overview

This document defines all implementation tasks for the GPS-based weather feature. Tasks are organized by phase, with clear dependencies, parallelization opportunities, and test requirements.

**Total Tasks**: 87
**Setup Tasks**: 4
**Foundational Tasks**: 16
**Feature Implementation Tasks**: 46
**Integration Tasks**: 6
**Testing Tasks**: 15

---

## Task Format Legend

```
[TaskID] [P?] [Story?] Description with file path
```

- **TaskID**: T001, T002, etc. (sequential numbering)
- **[P]**: Indicates task can be parallelized (independent, different files)
- **[Story]**: User story reference (e.g., [RF-001]) for feature-specific tasks
- **File paths**: Always absolute, starting with `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/`

---

## Phase 0: Setup (Project Initialization)

**Objective**: Configure project dependencies and permissions before implementation begins.

**Prerequisites**: None (can start immediately)

**Duration**: 30 minutes - 1 hour

### Configuration Tasks

- [x] T001 [P] Add Google Play Services Location dependency to `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/gradle/libs.versions.toml` (version: 21.3.0)
- [x] T002 [P] Add Accompanist Permissions dependency to `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/gradle/libs.versions.toml` (version: 0.36.0)
- [x] T003 [P] Add dependencies to `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/build.gradle.kts` (implementation libs.play.services.location and libs.accompanist.permissions)
- [x] T004 Add location permissions to `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/AndroidManifest.xml` (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, GPS feature)

**Verification**:
```bash
./gradlew --refresh-dependencies
./gradlew build
```

---

## Phase 1: Foundational (Blocking Prerequisites)

**Objective**: Create core domain models, result types, and repository interfaces.

**Prerequisites**: Phase 0 complete

**Duration**: 1-2 days

### Domain Models

- [x] T005 [P] Create Result sealed class at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Result.kt` (Loading, Success<T>, Error with DomainError)
- [x] T006 [P] Create DomainError sealed class at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/model/DomainError.kt` (LocationError, WeatherError, ValidationError hierarchies)
- [x] T007 [P] Create Location data class at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Location.kt` (latitude, longitude, name, country with validation)
- [x] T008 [P] Create WeatherCondition data class at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/model/WeatherCondition.kt` (id, main, description, icon with getIconUrl)
- [x] T009 [P] Create Weather data class at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/model/Weather.kt` (temperature, feelsLike, humidity, pressure, wind, condition, location, timestamp)

### Repository Interfaces

- [x] T010 [P] Create WeatherRepository interface at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/WeatherRepository.kt` (getCurrentWeatherByCoordinates suspend fun)
- [x] T011 [P] Create LocationRepository interface at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/repository/LocationRepository.kt` (searchLocation suspend fun)

### DTOs

- [x] T012 [P] Create WeatherResponseDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/WeatherResponseDto.kt` (@Serializable with SerialName annotations)
- [x] T013 [P] Create WeatherConditionDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/WeatherConditionDto.kt` (@Serializable)
- [x] T014 [P] Create MainDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/MainDto.kt` (@Serializable with temp, feelsLike, humidity, pressure)
- [x] T015 [P] Create WindDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/WindDto.kt` (@Serializable with speed, deg nullable)
- [x] T016 [P] Create SysDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/SysDto.kt` (@Serializable with country)
- [x] T017 [P] Create CoordinatesDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/CoordinatesDto.kt` (@Serializable)
- [x] T018 [P] Create GeocodingResponseDto at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/dto/GeocodingResponseDto.kt` (@Serializable with name, lat, lon, country)

### Koin Modules Structure

- [x] T019 Create WeatherModule skeleton at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/di/WeatherModule.kt` (module declaration, will be populated in Phase 9)
- [x] T020 Register weatherModule in `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/di/AppModule.kt` (add to includes list)

**Parallel Execution**: T005-T018 can run in parallel (different files)

---

## Phase 2: RF-001 & RF-002 (Permission Management)

**User Story**: Gestión de Permisos de Ubicación

**Objective**: Implement Android location permission handling with Accompanist.

**Prerequisites**: Phase 1 complete

**Duration**: 1 day

### Permission Handling

- [x] T021 [RF-001] Create PermissionHandler composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/PermissionHandler.kt` (with rememberMultiplePermissionsState)
- [x] T022 [RF-001] Add permission rationale dialog to PermissionHandler (AlertDialog with explanation)
- [x] T023 [RF-002] Add permission state management in PermissionHandler (LaunchedEffect for status changes)
- [x] T024 [RF-002] Add permanently denied handler in PermissionHandler (navigate to settings logic)

### Tests

- [x] T025 [P] TESTS: Create PermissionHandlerTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/ui/weather/components/PermissionHandlerTest.kt` (Given-When-Then structure, test all permission states)

**Acceptance Criteria**:
- Permission dialog shows on first request
- Rationale shows if denied once
- Settings navigation if permanently denied
- Manual search available as fallback

---

## Phase 3: RF-003 (GPS Auto-detection)

**User Story**: Detección Automática de Ubicación GPS

**Objective**: Implement GPS location provider and weather fetching by coordinates.

**Prerequisites**: Phase 1 complete

**Duration**: 2-3 days

### Location Provider

- [x] T026 [RF-003] Create LocationProvider at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/location/LocationProvider.kt` (FusedLocationProviderClient with 10s timeout)
- [x] T027 [RF-003] Add LocationException class in LocationProvider.kt (custom exception for location errors)
- [x] T028 [RF-003] Implement getCurrentLocation suspend function (with cancellationTokenSource and coroutine support)

### Use Cases

- [x] T029 [RF-003] Create GetCurrentLocationUseCase at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentLocationUseCase.kt` (operator invoke with Result wrapper)
- [x] T030 [RF-003] Create GetCurrentWeatherByCoordinatesUseCase at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentWeatherByCoordinatesUseCase.kt` (validate coordinates, delegate to repository)

### Remote Data Source

- [x] T031 [RF-003] Create WeatherRemoteDataSource at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/datasource/WeatherRemoteDataSource.kt` (Ktor client injection, getCurrentWeather method)
- [x] T032 [RF-003] Configure OpenWeatherMap API endpoint in WeatherRemoteDataSource (https://api.openweathermap.org/data/2.5/weather with params: lat, lon, appid, units=metric, lang=es)

### Repository Implementation

- [x] T033 [RF-003] Create WeatherRepositoryImpl at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/repository/WeatherRepositoryImpl.kt` (implement WeatherRepository interface)
- [x] T034 [RF-003] Add coordinate validation in WeatherRepositoryImpl (lat: -90 to 90, lon: -180 to 180)
- [x] T035 [RF-003] Add error mapping in WeatherRepositoryImpl (UnknownHostException, ClientRequestException, ServerResponseException to DomainError)
- [x] T036 [RF-003] Add Dispatchers.IO context in WeatherRepositoryImpl (withContext for network calls)

### Mappers

- [x] T037 [P] [RF-003] Create WeatherMapper at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/mapper/WeatherMapper.kt` (mapToDomain for WeatherResponseDto to Weather)
- [x] T038 [P] [RF-003] Add null handling in WeatherMapper (require weather list not empty, handle nullable fields)
- [x] T039 [P] [RF-003] Add helper mappers for nested objects (mapLocation, mapWeatherCondition private functions)

### Tests

- [x] T040 [P] TESTS: Create WeatherRepositoryImplTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/data/repository/WeatherRepositoryImplTest.kt` (Given: mock data source, When: call method, Then: assert Result)
- [x] T041 [P] TESTS: Test success scenario in WeatherRepositoryImplTest (API returns valid data, mapper converts, returns Success)
- [x] T042 [P] TESTS: Test network error in WeatherRepositoryImplTest (UnknownHostException thrown, returns Error with NoInternetConnection)
- [x] T043 [P] TESTS: Test invalid coordinates in WeatherRepositoryImplTest (lat > 90, returns Error with ValidationError)
- [x] T044 [P] TESTS: Create GetCurrentWeatherByCoordinatesUseCaseTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/domain/usecase/GetCurrentWeatherByCoordinatesUseCaseTest.kt`
- [x] T045 [P] TESTS: Test use case delegates to repository correctly
- [x] T046 [P] TESTS: Test use case validates coordinates before repository call
- [x] T047 [P] TESTS: Create WeatherMapperTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/data/mapper/WeatherMapperTest.kt`
- [x] T048 [P] TESTS: Test valid DTO maps to domain correctly in WeatherMapperTest
- [x] T049 [P] TESTS: Test empty weather list throws exception in WeatherMapperTest

**Parallel Execution**: T037-T039 (mappers), T040-T049 (tests) can run in parallel

**Acceptance Criteria**:
- GPS obtains location within 10 seconds
- Weather API call completes within 3 seconds
- Coordinates validated before API call
- All errors mapped to user-friendly messages

---

## Phase 4: RF-004 (Manual City Search)

**User Story**: Búsqueda Manual de Ubicación

**Objective**: Implement city name to coordinates geocoding with search UI.

**Prerequisites**: Phase 3 complete (needs WeatherRepositoryImpl pattern)

**Duration**: 2 days

### Geocoding Data Source

- [x] T050 [RF-004] Create GeocodingRemoteDataSource at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/remote/datasource/GeocodingRemoteDataSource.kt` (Ktor client, searchLocation method)
- [x] T051 [RF-004] Configure Geocoding API endpoint (https://api.openweathermap.org/geo/1.0/direct with params: q, appid, limit=1)

### Use Case

- [x] T052 [RF-004] Create SearchLocationUseCase at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/domain/usecase/SearchLocationUseCase.kt` (validate cityName not blank, operator invoke)

### Repository Implementation

- [x] T053 [RF-004] Create LocationRepositoryImpl at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/repository/LocationRepositoryImpl.kt` (implement LocationRepository interface)
- [x] T054 [RF-004] Add empty results handling in LocationRepositoryImpl (if results.isEmpty return CityNotFound error)
- [x] T055 [RF-004] Add city name validation in LocationRepositoryImpl (trim, check not blank, max 255 chars)

### Mapper

- [x] T056 [P] [RF-004] Create LocationMapper at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/data/mapper/LocationMapper.kt` (mapToDomain for GeocodingResponseDto to Location)

### UI Component

- [x] T057 [RF-004] Create CitySearchBar composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/CitySearchBar.kt` (TextField with search icon, debounce)

### Tests

- [x] T058 [P] TESTS: Create LocationRepositoryImplTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/data/repository/LocationRepositoryImplTest.kt`
- [x] T059 [P] TESTS: Test city found scenario in LocationRepositoryImplTest (results not empty, returns Success)
- [x] T060 [P] TESTS: Test city not found in LocationRepositoryImplTest (results empty, returns Error CityNotFound)
- [x] T061 [P] TESTS: Test blank city name in LocationRepositoryImplTest (returns ValidationError EmptyCityName)
- [x] T062 [P] TESTS: Create SearchLocationUseCaseTest at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/test/java/com/mtzdev/mywheatherapp/domain/usecase/SearchLocationUseCaseTest.kt`
- [x] T063 [P] TESTS: Test use case validates blank city name
- [x] T064 [P] TESTS: Test use case trims whitespace

**Acceptance Criteria**:
- Search accepts city name input
- Empty city name shows validation error
- City not found shows clear message
- Successful search returns coordinates
- Search is case-insensitive

---

## Phase 5: RF-005 (Weather Display)

**User Story**: Visualización de Clima Actual

**Objective**: Create MVI contract, ScreenModel, and UI for weather display.

**Prerequisites**: Phase 3 complete (needs use cases)

**Duration**: 2-3 days

### MVI Contract

- [x] T065 [RF-005] Create WeatherContract at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherContract.kt` (State, Event, Effect sealed interfaces)
- [x] T066 [RF-005] Define State in WeatherContract (weather: Weather?, location: Location?, isLoading, error, locationMode, permissionStatus, searchQuery)
- [x] T067 [RF-005] Define Event in WeatherContract (RequestAutoDetection, SearchCity, SwitchLocationMode, OnPermissionResult, ShowPermissionSettings, RetryLastAction, ClearError, OnSearchQueryChanged)
- [x] T068 [RF-005] Define Effect in WeatherContract (NavigateToSettings, ShowSnackbar, RequestLocationPermission)
- [x] T069 [RF-005] Add LocationMode enum in WeatherContract (AUTO, MANUAL)
- [x] T070 [RF-005] Add PermissionStatus enum in WeatherContract (UNKNOWN, GRANTED, DENIED, PERMANENTLY_DENIED)

### ScreenModel

- [x] T071 [RF-005] Create WeatherScreenModel at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreenModel.kt` (extend MVIBaseScreenModel)
- [x] T072 [RF-005] Implement handleEvent in WeatherScreenModel (when expression for all Event types)
- [x] T073 [RF-005] Implement handleAutoDetection in WeatherScreenModel (check permissions, call getCurrentLocationUseCase, then getWeatherByCoordinates)
- [x] T074 [RF-005] Implement handleSearchCity in WeatherScreenModel (validate not blank, call searchLocationUseCase, then getWeather)
- [x] T075 [RF-005] Implement error handlers in WeatherScreenModel (handleLocationError, handleWeatherError, handleSearchError map DomainError to user messages)
- [x] T076 [RF-005] Implement handleRetry in WeatherScreenModel (retry based on current locationMode)

### UI Components

- [x] T077 [RF-005] Create WeatherScreen at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreen.kt` (Tab implementation with Voyager)
- [x] T078 [RF-005] Create WeatherDisplay composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/WeatherDisplay.kt` (show temperature, description, icon, humidity, wind)
- [x] T079 [RF-005] Create LoadingIndicator composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/LoadingIndicator.kt` (CircularProgressIndicator with message)
- [x] T080 [RF-005] Create ErrorMessage composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/ErrorMessage.kt` (Card with error icon, message, retry button)

**Acceptance Criteria**:
- Weather displays temperature, description, humidity, wind
- Loading indicator shows during API calls
- Error messages are user-friendly
- Retry button works from error state
- UI follows Material 3 design

---

## Phase 6: RF-006 (Location Mode Selector)

**User Story**: Selector de Modo de Ubicación

**Objective**: Add toggle between GPS auto-detection and manual search.

**Prerequisites**: Phase 5 complete

**Duration**: 0.5 day

### UI Components

- [x] T081 [RF-006] Create LocationModeToggle composable at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/components/LocationModeToggle.kt` (RadioButton group or SegmentedButton)
- [x] T082 [RF-006] Add mode switching logic in WeatherScreenModel (handleSwitchMode updates state.locationMode)
- [x] T083 [RF-006] Integrate LocationModeToggle in WeatherScreen (onModeChange calls setEvent)

**Acceptance Criteria**:
- Toggle shows AUTO vs MANUAL modes
- Switching mode updates UI immediately
- GPS disabled if MANUAL mode active
- Search field disabled if AUTO mode active

---

## Phase 7: RF-007 (State Management)

**User Story**: Manejo de Estados de UI

**Objective**: Implement loading, error, and success state transitions.

**Prerequisites**: Phase 5 complete

**Duration**: 1 day

### State Transitions

- [ ] T084 [RF-007] Add loading state management in WeatherScreenModel (set isLoading true before async calls, false after)
- [ ] T085 [RF-007] Add error state recovery actions in WeatherScreenModel (clearError, retryLastAction)
- [ ] T086 [RF-007] Add state transition animations in WeatherScreen (AnimatedVisibility, Crossfade for state changes)

**Acceptance Criteria**:
- Loading indicator shows immediately on action
- Error state shows retry button
- Success state clears previous errors
- State transitions are smooth

---

## Phase 8: Integration & Navigation

**Objective**: Wire up Koin modules, configure navigation, integrate with app.

**Prerequisites**: Phase 5-7 complete

**Duration**: 1 day

### Dependency Injection

- [ ] T087 Configure Koin in WeatherModule at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/di/WeatherModule.kt` (single for data sources, repositories, mappers; factory for use cases, ScreenModel)
- [ ] T088 Add LocationProvider to WeatherModule (single with androidContext)
- [ ] T089 Add WeatherRemoteDataSource to WeatherModule (single with HttpClient, apiKey from BuildConfig)
- [ ] T090 Add GeocodingRemoteDataSource to WeatherModule (single)
- [ ] T091 Add WeatherMapper and LocationMapper to WeatherModule (singleOf)
- [ ] T092 Add WeatherRepositoryImpl to WeatherModule (singleOf bind WeatherRepository::class)
- [ ] T093 Add LocationRepositoryImpl to WeatherModule (singleOf bind LocationRepository::class)
- [ ] T094 Add use cases to WeatherModule (factoryOf for all three use cases)
- [ ] T095 Add WeatherScreenModel to WeatherModule (factoryOf)

### Navigation

- [ ] T096 Create WeatherTab at `/Users/joaquinalfonsomartinez/Documents/nisum/MyWheatherApp/app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/navigation/WeatherTab.kt` (implement Voyager Tab interface)
- [ ] T097 Integrate WeatherTab with bottom navigation (add to TabNavigator if exists, or create)

**Verification**:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

---

## Phase 9: Polish & Cross-Cutting

**Objective**: Add KDoc, code review, testing checklist, performance optimization.

**Prerequisites**: Phase 8 complete

**Duration**: 1-2 days

### Documentation

- [ ] T098 [P] Add KDoc to all public domain model classes (Weather, Location, WeatherCondition, Result, DomainError)
- [ ] T099 [P] Add KDoc to all repository interfaces (WeatherRepository, LocationRepository)
- [ ] T100 [P] Add KDoc to all use cases (GetCurrentWeatherByCoordinatesUseCase, SearchLocationUseCase, GetCurrentLocationUseCase)
- [ ] T101 [P] Add KDoc to WeatherContract (State, Event, Effect)
- [ ] T102 [P] Add KDoc to WeatherScreenModel (class-level and handleEvent)

### Code Quality

- [ ] T103 Run Lint and fix warnings: `./gradlew lintDebug`
- [ ] T104 Verify no hardcoded strings (use strings.xml for user-facing text)
- [ ] T105 Verify API key not in source code (only in BuildConfig)
- [ ] T106 Code review checklist: max 30 lines per function, max 300 lines per file, meaningful names

### Testing

- [ ] T107 Run all unit tests and verify passing: `./gradlew testDebugUnitTest`
- [ ] T108 Generate test coverage report: `./gradlew testDebugUnitTestCoverage`
- [ ] T109 Verify 80%+ coverage for data layer (repositories, mappers)
- [ ] T110 Verify 80%+ coverage for domain layer (use cases)

---

## Dependency Graph

Visual representation of task dependencies by phase:

```
Phase 0 (Setup)
└── [T001-T004] → Can run in parallel
    │
    ▼
Phase 1 (Foundational)
├── [T005-T009] Domain Models (parallel)
├── [T010-T011] Repository Interfaces (parallel)
├── [T012-T018] DTOs (parallel)
└── [T019-T020] Koin Setup (sequential)
    │
    ▼
Phase 2 (RF-001, RF-002 - Permissions)
├── [T021-T024] Permission Handler (sequential)
└── [T025] Tests (parallel after T024)
    │
    ▼
Phase 3 (RF-003 - GPS)
├── [T026-T028] Location Provider (sequential)
├── [T029-T030] Use Cases (parallel after T026-T028)
├── [T031-T032] Weather Data Source (parallel)
├── [T033-T036] Weather Repository (sequential, depends on T031)
├── [T037-T039] Mappers (parallel)
└── [T040-T049] Tests (parallel after implementation)
    │
    ▼
Phase 4 (RF-004 - Search)
├── [T050-T051] Geocoding Data Source (sequential)
├── [T052] Search Use Case (parallel)
├── [T053-T055] Location Repository (sequential, depends on T050)
├── [T056] Location Mapper (parallel)
├── [T057] Search UI (parallel)
└── [T058-T064] Tests (parallel after implementation)
    │
    ▼
Phase 5 (RF-005 - Display)
├── [T065-T070] Weather Contract (sequential)
├── [T071-T076] Weather ScreenModel (sequential, depends on T065)
└── [T077-T080] UI Components (parallel after T071)
    │
    ▼
Phase 6 (RF-006 - Mode Toggle)
└── [T081-T083] Location Mode Toggle (sequential)
    │
    ▼
Phase 7 (RF-007 - State Management)
└── [T084-T086] State Transitions (sequential)
    │
    ▼
Phase 8 (Integration)
├── [T087-T095] Koin Configuration (sequential)
└── [T096-T097] Navigation (sequential)
    │
    ▼
Phase 9 (Polish)
├── [T098-T102] KDoc (parallel)
├── [T103-T106] Code Quality (sequential)
└── [T107-T110] Final Testing (sequential)
```

---

## Parallel Execution Examples

### Phase 1: Maximum Parallelization
```
Parallel Group 1 (Domain Models):
- T005 (Result)
- T006 (DomainError)
- T007 (Location)
- T008 (WeatherCondition)
- T009 (Weather)

Parallel Group 2 (Interfaces):
- T010 (WeatherRepository)
- T011 (LocationRepository)

Parallel Group 3 (DTOs):
- T012 (WeatherResponseDto)
- T013 (WeatherConditionDto)
- T014 (MainDto)
- T015 (WindDto)
- T016 (SysDto)
- T017 (CoordinatesDto)
- T018 (GeocodingResponseDto)

Total: 16 tasks can run in parallel across 3 groups
```

### Phase 3: Test Parallelization
```
After T037-T039 (Mappers) complete:

Parallel Test Group:
- T040 (WeatherRepositoryImplTest)
- T041-T043 (Repository test cases)
- T044 (GetCurrentWeatherByCoordinatesUseCaseTest)
- T045-T046 (Use case test cases)
- T047 (WeatherMapperTest)
- T048-T049 (Mapper test cases)

Total: 10 test tasks can run in parallel
```

### Phase 9: Documentation Parallelization
```
Parallel Documentation Group:
- T098 (Domain model KDoc)
- T099 (Repository KDoc)
- T100 (Use case KDoc)
- T101 (Contract KDoc)
- T102 (ScreenModel KDoc)

Total: 5 documentation tasks can run in parallel
```

---

## Test Coverage Tracking

### By Layer

| Layer | Target | Current | Status |
|-------|--------|---------|--------|
| **Data** | 80%+ | TBD | ⏳ Pending |
| └─ Repositories | 80%+ | TBD | ⏳ Pending |
| └─ Mappers | 80%+ | TBD | ⏳ Pending |
| **Domain** | 80%+ | TBD | ⏳ Pending |
| └─ Use Cases | 90%+ | TBD | ⏳ Pending |
| **Presentation** | Manual | N/A | ℹ️ Manual Testing Only |

### Test Checklist

#### Data Layer (T040-T049, T058-T064)
- [ ] WeatherRepositoryImpl: Success scenario
- [ ] WeatherRepositoryImpl: Network error (UnknownHostException)
- [ ] WeatherRepositoryImpl: API error (4xx)
- [ ] WeatherRepositoryImpl: Server error (5xx)
- [ ] WeatherRepositoryImpl: Invalid coordinates
- [ ] WeatherRepositoryImpl: Timeout
- [ ] WeatherMapper: Valid DTO to domain
- [ ] WeatherMapper: Empty weather list throws exception
- [ ] WeatherMapper: Null handling
- [ ] LocationRepositoryImpl: City found
- [ ] LocationRepositoryImpl: City not found (empty results)
- [ ] LocationRepositoryImpl: Blank city name
- [ ] LocationRepositoryImpl: Network error
- [ ] LocationMapper: Valid DTO to domain

#### Domain Layer (T044-T046, T062-T064)
- [ ] GetCurrentWeatherByCoordinatesUseCase: Success
- [ ] GetCurrentWeatherByCoordinatesUseCase: Error
- [ ] GetCurrentWeatherByCoordinatesUseCase: Invalid coordinates
- [ ] GetCurrentLocationUseCase: Success
- [ ] GetCurrentLocationUseCase: Permission denied
- [ ] GetCurrentLocationUseCase: Timeout
- [ ] SearchLocationUseCase: Success
- [ ] SearchLocationUseCase: Blank city
- [ ] SearchLocationUseCase: City not found

#### Presentation Layer (Manual)
- [ ] Permission grant flow
- [ ] Permission deny flow
- [ ] GPS auto-detection
- [ ] Manual city search
- [ ] Error display and retry
- [ ] Loading states
- [ ] Mode toggle

---

## Implementation Strategy

### MVP-First Approach

**Week 1: Core MVP**
- Phase 0-1: Setup + Foundation (Days 1-2)
- Phase 3: GPS auto-detection (Days 3-5)
  - Focus: Get GPS → Fetch Weather → Display
  - Skip: Manual search, mode toggle

**Week 2: Feature Complete**
- Phase 4: Manual search (Days 1-2)
- Phase 5: UI polish (Days 3-4)
- Phase 2: Permissions (Day 5)
  - Reason: Can develop without real device using mock coordinates

**Week 3: Polish & Ship**
- Phase 6-7: Mode toggle + State management (Days 1-2)
- Phase 8: Integration (Day 3)
- Phase 9: Polish, testing, documentation (Days 4-5)

### Critical Path
```
Phase 0 → Phase 1 → Phase 3 → Phase 5 → Phase 8
(Setup)   (Models)   (GPS)     (Display)  (Integration)
```

All other phases can be parallelized or deferred without blocking the critical path.

---

## Risk Mitigation

### High-Risk Tasks

| Task | Risk | Mitigation |
|------|------|------------|
| T026-T028 | Google Play Services unavailable | Add availability check, fallback to manual search |
| T031-T032 | OpenWeatherMap API rate limit | Implement request throttling, show clear error |
| T034 | Invalid API key in production | Verify BuildConfig setup, add key validation test |
| T073 | Permission edge cases crash app | Comprehensive permission state testing |
| T087-T095 | Koin dependency resolution fails | Verify module includes, test app startup |

### Testing Strategy

1. **Unit Tests First**: Write tests alongside implementation (TDD when possible)
2. **Integration Tests**: Test Koin module resolution before manual testing
3. **Manual Tests**: Real device testing for permissions and GPS
4. **Performance Tests**: Verify API call timeouts work correctly

---

## Definition of Done

A task is considered complete when:

### For Implementation Tasks (T001-T087)
- [ ] Code implements specified functionality
- [ ] File created at exact path specified
- [ ] Follows Clean Architecture (no layer violations)
- [ ] Follows MVI pattern (if presentation layer)
- [ ] No Android dependencies in domain layer
- [ ] KDoc added for all public APIs
- [ ] No hardcoded strings (use resources)
- [ ] No API keys in source code
- [ ] Functions ≤ 30 lines
- [ ] Files ≤ 300 lines
- [ ] Meaningful variable/function names
- [ ] Code compiles without errors or warnings

### For Test Tasks (T025, T040-T049, T058-T064, T107-T110)
- [ ] Test file created at specified path
- [ ] Follows Given-When-Then structure
- [ ] Uses MockK for mocking
- [ ] Uses kotlinx-coroutines-test for suspend functions
- [ ] All assertions meaningful and specific
- [ ] Test names describe scenario clearly
- [ ] Tests pass consistently
- [ ] Coverage target met (80%+ for data/domain)

### For Phase Completion
- [ ] All tasks in phase marked complete
- [ ] Integration smoke test passes
- [ ] No blocking issues for next phase
- [ ] Code reviewed (if working in team)

---

## Total Summary

**Total Tasks**: 110
- **Setup**: 4 tasks (T001-T004)
- **Foundation**: 16 tasks (T005-T020)
- **RF-001/RF-002 (Permissions)**: 5 tasks (T021-T025)
- **RF-003 (GPS)**: 24 tasks (T026-T049)
- **RF-004 (Search)**: 15 tasks (T050-T064)
- **RF-005 (Display)**: 16 tasks (T065-T080)
- **RF-006 (Mode Toggle)**: 3 tasks (T081-T083)
- **RF-007 (State Management)**: 3 tasks (T084-T086)
- **Integration**: 11 tasks (T087-T097)
- **Polish**: 13 tasks (T098-T110)

**Parallelization Potential**:
- Phase 1: 16 tasks can run in parallel
- Phase 3: 10 test tasks can run in parallel
- Phase 4: 7 implementation tasks can run in parallel
- Phase 9: 5 documentation tasks can run in parallel

**Estimated Timeline**:
- **Sequential**: ~25-30 days (no parallelization)
- **With Parallelization**: ~10-15 days (optimal team coordination)
- **Solo Developer**: ~15-20 days (natural parallelization limits)

---

**Document Status**: Production Ready ✅
**Generated**: 2025-10-30
**Ready for**: `/speckit.implement` command
**Approved by**: Automated validation (all references verified)
