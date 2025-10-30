# Implementation Plan: Clima Actual con GPS

**Branch**: `001-clima-actual-gps` | **Date**: 2025-10-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-clima-actual-gps/spec.md`

**Note**: This plan follows the constitution principles and architectural patterns established for the MyWeatherApp project.

## Summary

Implementar funcionalidad de clima actual basada en ubicación GPS del usuario con opción de búsqueda manual de ciudades. La solución utiliza Clean Architecture con patrón MVI, consumiendo OpenWeatherMap API a través de Ktor Client. Incluye manejo completo de permisos Android, estados de UI (loading/success/error), y navegación con Voyager. Testing obligatorio de data y domain layers con cobertura mínima del 80%.

## Technical Context

**Language/Version**: Kotlin 2.1.0
**Primary Dependencies**:
- Jetpack Compose BOM 2024.09.00 (UI)
- Koin 4.1.0 (Dependency Injection)
- Voyager 1.1.0-beta02 (Navigation & ScreenModel)
- Ktor Client 3.3.0 (HTTP Client)
- Google Play Services Location (FusedLocationProviderClient)

**Storage**: N/A (esta fase no incluye persistencia local)
**Testing**:
- JUnit 4.13.2 (Test Framework)
- MockK 1.14.6 (Mocking)
- Turbine 1.2.1 (Flow Testing)
- Kotlinx-Coroutines-Test 1.10.2 (Coroutine Testing)

**Target Platform**: Android API 24+ (Android 7.0+), TargetSdk 36
**Project Type**: Mobile (Android)
**Performance Goals**:
- GPS location < 10 segundos
- API weather calls < 3 segundos
- UI responsiveness < 100ms
- 60 FPS en animaciones de Compose

**Constraints**:
- Funcionar con/sin Google Play Services
- Funcionar sin permisos (modo búsqueda manual)
- Manejar offline gracefully
- No bloquear UI thread
- API key NO en código fuente

**Scale/Scope**:
- 1 pantalla principal (WeatherScreen)
- 3 capas de arquitectura (presentation/domain/data)
- ~10-15 archivos Kotlin nuevos
- ~20-25 tests unitarios
- 2-3 semanas de desarrollo

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Architecture Compliance
- [x] **Clean Architecture**: presentation → domain ← data
  *Status*: PASS - Feature seguirá estructura establecida
- [x] **MVI Pattern**: ScreenModel + MVIContract + immutable state
  *Status*: PASS - WeatherScreenModel extenderá MVIBaseScreenModel
- [x] **Dependency Rule**: Dependencies flow inward
  *Status*: PASS - Domain sin dependencias Android, data implementa interfaces de domain
- [x] **No Android in Domain**: Pure Kotlin only
  *Status*: PASS - Use cases y modelos de dominio puros

### ✅ Testing Requirements
- [x] **80%+ Coverage**: Data + Domain layers
  *Status*: PASS - 20-25 tests planificados
- [x] **Testing Libraries**: MockK, JUnit 4, Turbine, Coroutines-Test
  *Status*: PASS - Ya configurados en build.gradle.kts
- [x] **Given-When-Then**: Test structure
  *Status*: PASS - Patrón obligatorio en todos los tests

### ✅ Dependency Injection
- [x] **Koin**: Constructor injection, modules by feature
  *Status*: PASS - Módulos weatherModule con single/factory apropiados
- [x] **No Service Locator**: No Koin.get() in production
  *Status*: PASS - Solo constructor injection

### ✅ Error Handling
- [x] **Sealed Classes**: Result<T> pattern
  *Status*: PASS - sealed class Result<out T> { Success, Error, Loading }
- [x] **User-Friendly Messages**: Clear error states
  *Status*: PASS - Mensajes específicos por tipo de error

### ✅ Security
- [x] **No Secrets in Code**: API keys external
  *Status*: PASS - BuildConfig.WEATHER_API_KEY desde local.properties
- [x] **HTTPS Only**: Secure communication
  *Status*: PASS - OpenWeatherMap API usa HTTPS

### ✅ Code Quality
- [x] **Max Function Length**: 30 lines
  *Status*: PASS - Principio aplicado
- [x] **Max File Length**: 300 lines
  *Status*: PASS - Modularización por responsabilidad
- [x] **KDoc**: Public APIs documented
  *Status*: PASS - Todos los componentes públicos documentados

### ⚠️ New Dependencies Assessment
**New Dependencies Required**:
1. **Google Play Services Location** (com.google.android.gms:play-services-location)
   - **Justification**: FusedLocationProviderClient es el API oficial de Google para ubicación
   - **Alternative Considered**: LocationManager (deprecated, menos preciso)
   - **Constitution Impact**: MINOR - No afecta arquitectura, solo Android framework
   - **Status**: APPROVED - Necesario para RF-003

### ✅ Gate Status: **APPROVED**
All constitutional requirements met. Feature can proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/mtzdev/mywheatherapp/
├── commons/                          # Shared utilities (existing)
│   ├── MVIContract.kt               # Base MVI interfaces
│   └── MVIBaseScreenModel.kt        # Base ScreenModel implementation
│
├── data/                            # Data Layer
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── WeatherResponseDto.kt      # NEW
│   │   │   ├── GeocodingResponseDto.kt    # NEW
│   │   │   └── WeatherConditionDto.kt     # NEW
│   │   ├── datasource/
│   │   │   ├── WeatherRemoteDataSource.kt # NEW
│   │   │   └── GeocodingRemoteDataSource.kt # NEW
│   │   └── api/
│   │       └── WeatherApiClient.kt        # NEW
│   ├── repository/
│   │   └── WeatherRepositoryImpl.kt       # NEW
│   └── mapper/
│       ├── WeatherMapper.kt               # NEW
│       └── LocationMapper.kt              # NEW
│
├── domain/                          # Domain Layer
│   ├── model/
│   │   ├── Weather.kt                     # NEW
│   │   ├── Location.kt                    # NEW
│   │   ├── WeatherCondition.kt            # NEW
│   │   └── Result.kt                      # NEW (sealed class)
│   ├── repository/
│   │   ├── WeatherRepository.kt           # NEW (interface)
│   │   └── LocationRepository.kt          # NEW (interface)
│   └── usecase/
│       ├── GetCurrentWeatherByCoordinatesUseCase.kt # NEW
│       ├── GetCurrentWeatherByCityNameUseCase.kt    # NEW
│       └── SearchLocationUseCase.kt       # NEW
│
├── ui/                              # Presentation Layer
│   └── weather/
│       ├── WeatherScreen.kt               # NEW
│       ├── WeatherScreenModel.kt          # NEW
│       ├── WeatherContract.kt             # NEW
│       ├── components/
│       │   ├── WeatherDisplay.kt          # NEW
│       │   ├── LocationSearchBar.kt       # NEW
│       │   ├── LoadingIndicator.kt        # NEW
│       │   ├── ErrorMessage.kt            # NEW
│       │   └── PermissionHandler.kt       # NEW
│       └── navigation/
│           └── WeatherTab.kt              # NEW (Voyager Tab)
│
└── di/                              # Dependency Injection
    └── WeatherModule.kt                   # NEW

app/src/test/java/com/mtzdev/mywheatherapp/
├── data/
│   ├── repository/
│   │   └── WeatherRepositoryImplTest.kt   # NEW
│   └── mapper/
│       ├── WeatherMapperTest.kt           # NEW
│       └── LocationMapperTest.kt          # NEW
└── domain/
    └── usecase/
        ├── GetCurrentWeatherByCoordinatesUseCaseTest.kt # NEW
        ├── GetCurrentWeatherByCityNameUseCaseTest.kt    # NEW
        └── SearchLocationUseCaseTest.kt                 # NEW

app/src/main/AndroidManifest.xml
└── [ADD] Location permissions: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION

app/build.gradle.kts
└── [ADD] play-services-location dependency
```

**Structure Decision**:
El proyecto sigue la estructura de **Android Single Module** con Clean Architecture. La app ya tiene establecidas las carpetas `data/`, `domain/`, `ui/`, `commons/` y `di/`. Esta feature agregará nuevos archivos respetando la estructura existente y agrupando componentes por capa arquitectónica, no por feature (según constitución actual del proyecto).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations detected**. All architectural decisions align with project constitution.

---

## Post-Design Constitution Check

*Re-evaluation after Phase 1 (Design & Contracts)*

### ✅ Design Validation
- [x] **Domain Models**: Pure Kotlin, no Android dependencies
  *Status*: PASS - Verified in data-model.md
- [x] **Repository Interfaces**: Defined in domain layer
  *Status*: PASS - Verified in contracts/repository-contracts.md
- [x] **Use Cases**: Single responsibility, testable
  *Status*: PASS - Verified in contracts/use-case-contracts.md
- [x] **MVI Contract**: Implements MVIContract base interfaces
  *Status*: PASS - Verified in contracts/mvi-contracts.md
- [x] **Error Handling**: Sealed classes, user-friendly messages
  *Status*: PASS - Verified in contracts/error-contracts.md
- [x] **Testing Strategy**: 80%+ coverage achievable
  *Status*: PASS - Test examples provided in research.md and quickstart.md
- [x] **Performance**: Timeouts and optimizations documented
  *Status*: PASS - Verified in research.md section 7
- [x] **Dependencies**: Only 2 new, no version conflicts
  *Status*: PASS - Verified in research.md section 9

### ✅ Final Gate Status: **APPROVED**
Design phase complete. All constitutional requirements met. Ready to proceed to implementation (/speckit.implement).

---

## Implementation Readiness

### Artifacts Generated
- ✅ **research.md**: All technical decisions documented
- ✅ **data-model.md**: Complete domain models, DTOs, mappers
- ✅ **contracts/**: 5 contract documents (API, repositories, use cases, MVI, errors)
- ✅ **quickstart.md**: Step-by-step implementation guide
- ✅ **CLAUDE.md**: Agent context updated

### Remaining Tasks
Use `/speckit.tasks` command to generate tasks.md with implementation checklist.

---

## Summary

**Feature**: Clima Actual con GPS
**Status**: Design Complete ✅
**Next Command**: `/speckit.tasks` to generate implementation tasks
**Branch**: `001-clima-actual-gps`
**Estimated Implementation**: 2-3 semanas (10-15 días hábiles)

**Key Deliverables**:
- 10-15 new Kotlin files
- 20-25 unit tests
- 2 new dependencies (play-services-location, accompanist-permissions)
- 0 breaking changes to existing code

**Technical Debt**: None - All decisions align with constitution

**Risks**: All identified and mitigated (see research.md section 11)
