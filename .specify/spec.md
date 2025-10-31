# Product Requirements Document: Clima Actual con GPS

**Version**: 1.0.0
**Fecha**: 2025-10-29
**Estimación de Entrega**: 2-3 semanas
**Prioridad**: Alta

---

## 1. Resumen Ejecutivo

### 1.1 Objetivo
Implementar funcionalidad de clima actual basada en la ubicación del usuario mediante GPS, permitiendo la detección automática o búsqueda manual de ubicaciones, con una interfaz mínima funcional.

### 1.2 Alcance de la Entrega
- Solicitud y manejo de permisos de ubicación (Android)
- Detección automática de ubicación mediante GPS
- Búsqueda manual de ubicación por nombre de ciudad
- Visualización de clima actual para la ubicación seleccionada
- Manejo de estados (carga, error, éxito)
- Interfaz funcional minimalista

### 1.3 Fuera de Alcance (Excluido)
- Widgets de pantalla principal
- Sistema de alertas climáticas complejas
- Historial de ubicaciones consultadas
- Configuraciones avanzadas de usuario
- Temas claro/oscuro (fase futura)
- Cache local con Room (fase futura)

---

## 2. Requisitos Funcionales

### 2.1 Gestión de Permisos de Ubicación

#### RF-001: Solicitud de Permisos
**Prioridad**: Alta
**Descripción**: La aplicación DEBE solicitar permisos de ubicación al usuario.

**Criterios de Aceptación**:
- Solicitar `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`
- Mostrar diálogo de permisos nativo de Android
- Manejar respuesta del usuario (concedido/denegado)
- Explicar claramente por qué se necesita el permiso

**API de OpenWeatherMap**:
- Endpoint: Current Weather Data API
- Parámetros: `lat`, `lon`, `appid`

#### RF-002: Manejo de Estado de Permisos
**Prioridad**: Alta
**Descripción**: La aplicación DEBE manejar todos los estados de permisos.

**Criterios de Aceptación**:
- Detectar si los permisos ya están concedidos
- Permitir reintento si fueron denegados
- Mostrar mensaje informativo si permisos están permanentemente denegados
- Redirigir a configuración del sistema si es necesario
- Funcionar sin GPS si usuario deniega permisos (modo búsqueda manual)

### 2.2 Detección de Ubicación

#### RF-003: Obtención Automática de Ubicación
**Prioridad**: Alta
**Descripción**: La aplicación DEBE obtener la ubicación actual del usuario mediante GPS.

**Criterios de Aceptación**:
- Usar `FusedLocationProviderClient` (Google Play Services)
- Obtener coordenadas (latitud, longitud)
- Timeout de 10 segundos para obtención de ubicación
- Mostrar indicador de carga durante la obtención
- Manejar caso de GPS desactivado
- Manejar caso de ubicación no disponible

**Casos de Error**:
- GPS desactivado → Mensaje: "Activa el GPS para detectar tu ubicación"
- Timeout → Mensaje: "No se pudo obtener ubicación. Intenta con búsqueda manual"
- Permiso denegado → Mensaje: "Necesitamos permisos de ubicación. Busca manualmente"

#### RF-004: Búsqueda Manual de Ubicación
**Prioridad**: Alta
**Descripción**: El usuario DEBE poder buscar ubicaciones por nombre de ciudad.

**Criterios de Aceptación**:
- Campo de texto para ingresar nombre de ciudad
- Botón de búsqueda claramente visible
- Validar que el campo no esté vacío
- Buscar ubicación usando OpenWeatherMap Geocoding API
- Mostrar resultado de la búsqueda (ciudad encontrada)
- Manejar caso de ciudad no encontrada

**API de OpenWeatherMap**:
- Endpoint: Geocoding API
- Parámetros: `q` (nombre ciudad), `appid`, `limit=1`
- Respuesta: `lat`, `lon`, `name`, `country`

**Casos de Error**:
- Ciudad no encontrada → Mensaje: "No encontramos esa ubicación. Verifica el nombre"
- Error de red → Mensaje: "Error de conexión. Verifica tu internet"

### 2.3 Visualización de Clima Actual

#### RF-005: Mostrar Datos Climáticos
**Prioridad**: Alta
**Descripción**: La aplicación DEBE mostrar información del clima actual.

**Criterios de Aceptación**:
- Mostrar temperatura actual (en °C)
- Mostrar descripción del clima (ej: "Soleado", "Nublado")
- Mostrar icono del clima (proporcionado por API)
- Mostrar nombre de la ubicación
- Mostrar humedad relativa (%)
- Mostrar velocidad del viento (km/h)
- Actualizar automáticamente al cambiar ubicación

**Datos Mínimos a Mostrar**:
```
📍 Ciudad, País
🌡️ Temperatura: XX°C
☁️ Condición: Descripción
💧 Humedad: XX%
💨 Viento: XX km/h
```

**API de OpenWeatherMap**:
- Endpoint: `/data/2.5/weather`
- Parámetros: `lat`, `lon`, `appid`, `units=metric`, `lang=es`

#### RF-006: Selector de Modo de Ubicación
**Prioridad**: Alta
**Descripción**: El usuario DEBE poder elegir entre detección automática o búsqueda manual.

**Criterios de Aceptación**:
- Botón/Toggle para "Detectar ubicación automáticamente"
- Botón/Toggle para "Buscar ubicación manualmente"
- Cambio de modo debe ser claro e intuitivo
- Estado actual debe ser visible
- Transición suave entre modos

**Comportamiento**:
- Modo GPS: Mostrar botón "Usar mi ubicación" + campo de búsqueda deshabilitado
- Modo Manual: Mostrar campo de búsqueda + botón de búsqueda
- Permitir cambio entre modos en cualquier momento

### 2.4 Manejo de Estados

#### RF-007: Estados de la Interfaz
**Prioridad**: Alta
**Descripción**: La aplicación DEBE mostrar estados apropiados en todas las operaciones.

**Estados Requeridos**:
1. **Idle**: Estado inicial, esperando acción del usuario
2. **Loading**:
   - Obteniendo ubicación GPS
   - Buscando ciudad
   - Cargando datos del clima
3. **Success**: Datos del clima mostrados correctamente
4. **Error**:
   - Error de permisos
   - Error de ubicación
   - Error de red
   - Error de API
   - Ciudad no encontrada

**Criterios de Aceptación**:
- Mostrar indicador de carga (CircularProgressIndicator) durante operaciones asíncronas
- Mostrar mensaje de error descriptivo con opción de reintento
- Permitir volver al estado inicial desde cualquier error
- No bloquear UI durante operaciones

---

## 3. Requisitos No Funcionales

### 3.1 Rendimiento
- **NFR-001**: Obtención de ubicación GPS en < 10 segundos
- **NFR-002**: Llamadas a API de clima en < 3 segundos
- **NFR-003**: UI debe responder inmediatamente (< 100ms)
- **NFR-004**: No bloquear thread principal durante operaciones de red

### 3.2 Usabilidad
- **NFR-005**: Interfaz minimalista y fácil de entender
- **NFR-006**: Botones y controles suficientemente grandes (mínimo 48dp)
- **NFR-007**: Mensajes de error claros y accionables
- **NFR-008**: Feedback visual inmediato para todas las acciones

### 3.3 Compatibilidad
- **NFR-009**: Funcionar en API Level 24+ (Android 7.0+)
- **NFR-010**: Funcionar en diferentes tamaños de pantalla
- **NFR-011**: Funcionar con/sin Google Play Services

### 3.4 Seguridad
- **NFR-012**: API key de OpenWeatherMap NO en código fuente
- **NFR-013**: Usar HTTPS para todas las llamadas de API
- **NFR-014**: Validar y sanitizar inputs del usuario

---

## 4. Arquitectura Técnica

### 4.1 Capas y Responsabilidades

#### 4.1.1 Data Layer
**Componentes**:
- `WeatherRemoteDataSource`: Llamadas a OpenWeatherMap API
- `GeocodingRemoteDataSource`: Búsqueda de ubicaciones
- `WeatherRepositoryImpl`: Implementación del repositorio
- `WeatherApiClient`: Cliente Ktor configurado
- DTOs: `WeatherResponseDto`, `GeocodingResponseDto`
- Mappers: `WeatherMapper`, `LocationMapper`

**Responsabilidades**:
- Consumir OpenWeatherMap API (clima y geocoding)
- Mapear DTOs a modelos de dominio
- Manejo de errores de red
- Logging de requests/responses

#### 4.1.2 Domain Layer
**Componentes**:
- Models: `Weather`, `Location`, `WeatherCondition`
- Repository Interface: `WeatherRepository`, `LocationRepository`
- Use Cases:
  - `GetCurrentWeatherByCoordinatesUseCase`
  - `GetCurrentWeatherByCityNameUseCase`
  - `SearchLocationUseCase`
- Result: `sealed class Result<T>` (Success, Error, Loading)

**Responsabilidades**:
- Definir modelos de dominio puros
- Encapsular lógica de negocio
- Coordinar flujos de datos
- Independiente de frameworks Android

#### 4.1.3 Presentation Layer
**Componentes**:
- `WeatherScreen`: Composable principal
- `WeatherScreenModel`: Extiende `MVIBaseScreenModel`
- `WeatherContract`: Implementa `MVIContract` (UiState, UiEvent, Effect)
- Components:
  - `LocationPermissionHandler`
  - `WeatherDisplay`
  - `LocationSearchBar`
  - `LoadingIndicator`
  - `ErrorMessage`

**Responsabilidades**:
- Renderizar UI con Jetpack Compose
- Manejar interacciones del usuario
- Gestionar estado de UI (MVI)
- Solicitar permisos de ubicación

### 4.2 Flujos de Datos

#### 4.2.1 Flujo: Detección Automática con GPS
```
User taps "Usar mi ubicación"
    → WeatherScreenModel.onEvent(WeatherContract.Event.RequestAutoDetection)
    → Check permissions
    → Request GPS location (FusedLocationProviderClient)
    → GetCurrentWeatherByCoordinatesUseCase(lat, lon)
    → WeatherRepository.getCurrentWeatherByCoordinates()
    → WeatherRemoteDataSource.fetchWeather(lat, lon)
    → OpenWeatherMap API
    → Map DTO to Domain Model
    → Emit Result.Success(weather)
    → Update UiState with weather data
```

#### 4.2.2 Flujo: Búsqueda Manual
```
User enters city name + taps search
    → WeatherScreenModel.onEvent(WeatherContract.Event.SearchCity(query))
    → Validate input (not empty)
    → SearchLocationUseCase(cityName)
    → LocationRepository.searchLocation()
    → GeocodingRemoteDataSource.searchCity(query)
    → OpenWeatherMap Geocoding API
    → Map to Location(lat, lon, name, country)
    → GetCurrentWeatherByCoordinatesUseCase(lat, lon)
    → [Same flow as GPS from here]
```

### 4.3 Dependencias Koin

```kotlin
// Data Module
val dataModule = module {
    single { provideKtorClient() }
    single { WeatherApiClient(get()) }
    single<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }
    single { WeatherRemoteDataSource(get()) }
    single { GeocodingRemoteDataSource(get()) }
}

// Domain Module
val domainModule = module {
    factory { GetCurrentWeatherByCoordinatesUseCase(get()) }
    factory { GetCurrentWeatherByCityNameUseCase(get(), get()) }
    factory { SearchLocationUseCase(get()) }
}

// Presentation Module
val presentationModule = module {
    factory { WeatherScreenModel(get(), get(), get()) }
}
```

---

## 5. Contrato MVI

### 5.1 WeatherContract

El contrato DEBE implementar `MVIContract` definido en [commons/MVIContract.kt](app/src/main/java/com/mtzdev/mywheatherapp/commons/MVIContract.kt):

```kotlin
object WeatherContract {

    // STATE - Implementa MVIContract.UiState
    data class State(
        val weather: Weather? = null,
        val location: Location? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val locationMode: LocationMode = LocationMode.AUTO,
        val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN
    ) : MVIContract.UiState

    enum class LocationMode {
        AUTO,    // GPS detection
        MANUAL   // Search by city
    }

    enum class PermissionStatus {
        UNKNOWN,
        GRANTED,
        DENIED,
        PERMANENTLY_DENIED
    }

    // EVENTS - Implementa MVIContract.UiEvent
    sealed interface Event : MVIContract.UiEvent {
        data object RequestAutoDetection : Event
        data class SearchCity(val cityName: String) : Event
        data class SwitchLocationMode(val mode: LocationMode) : Event
        data object RequestPermissions : Event
        data class OnPermissionResult(val granted: Boolean) : Event
        data object RetryLastAction : Event
        data object ClearError : Event
    }

    // EFFECTS - Implementa MVIContract.Effect
    sealed interface Effect : MVIContract.Effect {
        data object NavigateToSettings : Effect
        data class ShowSnackbar(val message: String) : Effect
        data object RequestLocationPermission : Effect
    }
}
```

### 5.2 WeatherScreenModel

```kotlin
class WeatherScreenModel(
    private val getCurrentWeatherByCoordinatesUseCase: GetCurrentWeatherByCoordinatesUseCase,
    private val searchLocationUseCase: SearchLocationUseCase,
    // Otros use cases...
) : MVIBaseScreenModel<WeatherContract.State, WeatherContract.Event, WeatherContract.Effect>() {

    override fun createInitialState(): WeatherContract.State {
        return WeatherContract.State()
    }

    override fun handleEvent(event: WeatherContract.Event) {
        when (event) {
            is WeatherContract.Event.RequestAutoDetection -> handleAutoDetection()
            is WeatherContract.Event.SearchCity -> handleSearchCity(event.cityName)
            is WeatherContract.Event.SwitchLocationMode -> handleSwitchMode(event.mode)
            // ... otros eventos
        }
    }

    private fun handleAutoDetection() {
        // Implementación
    }

    private fun handleSearchCity(cityName: String) {
        // Implementación
    }
}
```

---

## 6. Testing

### 6.1 Tests Obligatorios

#### Data Layer (80%+ coverage)
- `WeatherRepositoryImplTest`:
  - ✅ `getCurrentWeatherByCoordinates_whenApiReturnsSuccess_emitsWeatherData`
  - ✅ `getCurrentWeatherByCoordinates_whenApiReturnsError_emitsErrorResult`
  - ✅ `searchLocation_whenCityFound_emitsLocationData`
  - ✅ `searchLocation_whenCityNotFound_emitsErrorResult`

- `WeatherMapperTest`:
  - ✅ `mapDtoToDomain_whenValidDto_returnsDomainModel`
  - ✅ `mapDtoToDomain_whenInvalidDto_throwsException`

#### Domain Layer (80%+ coverage)
- `GetCurrentWeatherByCoordinatesUseCaseTest`:
  - ✅ `invoke_whenRepositoryReturnsSuccess_emitsWeatherData`
  - ✅ `invoke_whenRepositoryReturnsError_emitsError`
  - ✅ `invoke_whenInvalidCoordinates_emitsError`

- `SearchLocationUseCaseTest`:
  - ✅ `invoke_whenValidCityName_emitsLocation`
  - ✅ `invoke_whenEmptyCityName_emitsError`
  - ✅ `invoke_whenCityNotFound_emitsError`

### 6.2 Tests Manuales
- Otorgar permisos → Ver clima actual con GPS
- Denegar permisos → Ver campo de búsqueda manual
- Buscar ciudad válida → Ver clima de la ciudad
- Buscar ciudad inválida → Ver mensaje de error
- Sin conexión → Ver mensaje de error de red
- GPS desactivado → Ver mensaje apropiado

---

## 7. APIs y Endpoints

### 7.1 OpenWeatherMap Current Weather API

**Endpoint**: `https://api.openweathermap.org/data/2.5/weather`

**Parámetros**:
- `lat` (required): Latitud
- `lon` (required): Longitud
- `appid` (required): API key
- `units` (optional): `metric` (para Celsius)
- `lang` (optional): `es` (español)

**Respuesta**:
```json
{
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
    "humidity": 60
  },
  "wind": {
    "speed": 3.5
  },
  "name": "Madrid"
}
```

### 7.2 OpenWeatherMap Geocoding API

**Endpoint**: `http://api.openweathermap.org/geo/1.0/direct`

**Parámetros**:
- `q` (required): Nombre de ciudad
- `appid` (required): API key
- `limit` (optional): Número de resultados (usar 1)

**Respuesta**:
```json
[
  {
    "name": "Madrid",
    "lat": 40.4165,
    "lon": -3.7026,
    "country": "ES"
  }
]
```

---

## 8. UI/UX Mockup

### 8.1 Pantalla Principal - Estado Inicial

```
┌─────────────────────────────────┐
│  Mi Clima                    ⚙️ │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐ │
│  │ 🔘 Detectar automáticamente│ │
│  │ ⚪ Buscar manualmente      │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │   📍 Usar mi ubicación    │ │
│  └───────────────────────────┘ │
│                                 │
│          O bien                 │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 🔍 Buscar ciudad...       │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

### 8.2 Pantalla - Mostrando Clima

```
┌─────────────────────────────────┐
│  Mi Clima                    ⚙️ │
├─────────────────────────────────┤
│                                 │
│       📍 Madrid, España         │
│                                 │
│          ☀️                     │
│          25°C                   │
│       Cielo claro               │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 💧 Humedad: 60%         │   │
│  │ 💨 Viento: 3.5 km/h     │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🔄 Actualizar           │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

### 8.3 Pantalla - Error

```
┌─────────────────────────────────┐
│  Mi Clima                    ⚙️ │
├─────────────────────────────────┤
│                                 │
│          ⚠️                     │
│                                 │
│   No se pudo obtener            │
│   la ubicación                  │
│                                 │
│   Verifica que el GPS           │
│   esté activado                 │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🔄 Reintentar           │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🔍 Buscar manualmente   │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

---

## 9. Cronograma Estimado

### Semana 1: Setup & Data Layer
**Días 1-2**: Configuración inicial
- Configurar permisos en AndroidManifest.xml
- Configurar Ktor client para OpenWeatherMap API
- Crear DTOs y modelos de datos
- Crear estructura de módulos Koin

**Días 3-5**: Implementación Data Layer
- Implementar `WeatherRemoteDataSource`
- Implementar `GeocodingRemoteDataSource`
- Implementar `WeatherRepositoryImpl`
- Implementar Mappers
- **Tests**: Data layer (80%+ coverage)

### Semana 2: Domain & Presentation Layer
**Días 1-2**: Domain Layer
- Crear modelos de dominio
- Implementar Use Cases
- Implementar Result wrapper
- **Tests**: Domain layer (80%+ coverage)

**Días 3-5**: Presentation Layer
- Crear `WeatherContract` implementando `MVIContract`
- Implementar `WeatherScreenModel` extendiendo `MVIBaseScreenModel`
- Implementar handler de permisos
- Configurar navegación con Voyager

### Semana 3: UI & Polish
**Días 1-3**: UI Components
- Crear `WeatherScreen` composable
- Crear componentes reutilizables
- Implementar estados de Loading/Error/Success
- Implementar búsqueda manual

**Días 4-5**: Testing & Refinamiento
- Tests manuales en dispositivos reales
- Fix de bugs encontrados
- Optimización de UX
- Code review
- Documentación

---

## 10. Definition of Done

Una feature se considera completada cuando:

- [ ] Código implementa todos los criterios de aceptación
- [ ] Clean Architecture aplicada (presentation → domain ← data)
- [ ] MVI pattern implementado con `MVIBaseScreenModel`
- [ ] `WeatherContract` implementa correctamente `MVIContract.UiState`, `MVIContract.UiEvent`, `MVIContract.Effect`
- [ ] Tests unitarios en data layer (80%+ coverage)
- [ ] Tests unitarios en domain layer (80%+ coverage)
- [ ] Manejo de errores implementado con mensajes claros
- [ ] Dependency injection con Koin funcionando
- [ ] No hay API keys en código fuente
- [ ] Build exitoso sin warnings
- [ ] Tests manuales completados en API 24+
- [ ] Tests manuales en diferentes tamaños de pantalla
- [ ] Code review aprobado
- [ ] Commits siguen convención semántica
- [ ] Documentación actualizada

---

## 11. Riesgos y Mitigaciones

### 11.1 Riesgos Identificados

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Usuario deniega permisos de ubicación | Alta | Medio | Implementar búsqueda manual como alternativa |
| GPS no disponible/desactivado | Media | Medio | Mostrar mensaje claro + búsqueda manual |
| Límite de llamadas API alcanzado | Baja | Alto | Implementar manejo de rate limiting |
| Error de red durante obtención de datos | Media | Bajo | Mensajes de error claros + opción de reintento |
| Respuesta API inválida/inesperada | Baja | Medio | Validación robusta + logging de errores |

---

## 12. Métricas de Éxito

- ✅ Feature implementada en <= 3 semanas
- ✅ 80%+ cobertura de tests en data y domain
- ✅ 0 crashes relacionados con permisos
- ✅ < 3 segundos para obtener clima después de ubicación
- ✅ Mensajes de error claros en todos los casos
- ✅ Code review aprobado sin blocking comments
- ✅ Build time < 2 minutos

---

## 13. Referencias

### 13.1 Documentación Técnica
- [OpenWeatherMap Current Weather API](https://openweathermap.org/current)
- [OpenWeatherMap Geocoding API](https://openweathermap.org/api/geocoding-api)
- [Android Location Permissions](https://developer.android.com/training/location/permissions)
- [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)

### 13.2 Repositorio
- Constitución Técnica: [.specify/memory/constitution.md](.specify/memory/constitution.md)
- Base Contract: [commons/MVIContract.kt](app/src/main/java/com/mtzdev/mywheatherapp/commons/MVIContract.kt)
- Base ScreenModel: [commons/MVIBaseScreenModel.kt](app/src/main/java/com/mtzdev/mywheatherapp/commons/MVIBaseScreenModel.kt)

---

**Aprobaciones Requeridas**:
- [ ] Product Owner
- [ ] Tech Lead
- [ ] QA Lead

**Estado**: Draft → Pendiente de Aprobación

---

## 14. Actualizaciones Post-Implementación

### 14.1 Fixes de Integración (2025-10-30)

#### Fix 1: Inyección de Dependencias con Named Qualifiers
**Problema Identificado**:
- `WeatherRemoteDataSource` y `GeocodingRemoteDataSource` usaban `BuildConfig.WEATHER_API_KEY` directamente
- No permitía soporte multi-ambiente (producción vs desarrollo)

**Solución Implementada**:
- Actualizado [WeatherModule.kt](../app/src/main/java/com/mtzdev/mywheatherapp/di/WeatherModule.kt:40-55)
- Usa `named(WEATHER_HTTP_CLIENT)` para HttpClient
- Usa `named(WEATHER_API_KEY)` para API key
- Permite configuración flexible por ambiente

```kotlin
// T089: WeatherRemoteDataSource con named qualifiers
single {
    WeatherRemoteDataSource(
        client = get(named(WEATHER_HTTP_CLIENT)),
        apiKey = get(named(WEATHER_API_KEY))
    )
}

// T090: GeocodingRemoteDataSource con named qualifiers
single {
    GeocodingRemoteDataSource(
        httpClient = get(named(WEATHER_HTTP_CLIENT)),
        apiKey = get(named(WEATHER_API_KEY))
    )
}
```

**Beneficios**:
- Soporte multi-ambiente preparado para el futuro
- Mayor flexibilidad en testing (puede inyectar mocks con named)
- Alineado con arquitectura de la app existente

#### Fix 2: Integración de WeatherScreen en HomeScreen (Reemplazo de WeatherDataScreen)
**Problema Identificado**:
- `WeatherScreen` (nueva pantalla GPS) no estaba integrada en `HomeScreen`
- La pantalla antigua `WeatherDataScreen` debía ser reemplazada por la nueva implementación
- No era accesible desde la navegación principal

**Solución Implementada**:
- Removido import de `WeatherDataScreen` en [HomeScreen.kt](../app/src/main/java/com/mtzdev/mywheatherapp/ui/screen/home/HomeScreen.kt:31)
- Reemplazado `WeatherDataScreen` por `WeatherTab.create()` como tab inicial en TabNavigator (línea 45)
- Actualizado `NavigationBar` para mostrar solo 2 tabs: `WeatherTab` (clima GPS) y `WeatherDataHourlyScreen` (líneas 62-64)
- Respeta paddings del Scaffold existente

**Antes**:
```kotlin
TabNavigator(WeatherDataScreen(state.geoData, paddingState)){
    ...
    bottomBar = {
        NavigationBar {
            TabNavigationItem(WeatherDataScreen(state.geoData, paddingState))
            TabNavigationItem(WeatherDataHourlyScreen(paddingState))
            TabNavigationItem(WeatherTab.create())
        }
    }
}
```

**Después**:
```kotlin
TabNavigator(WeatherTab.create()){
    ...
    bottomBar = {
        NavigationBar {
            TabNavigationItem(WeatherTab.create())
            TabNavigationItem(WeatherDataHourlyScreen(paddingState))
        }
    }
}
```

**Beneficios**:
- Navegación simplificada con 2 tabs en lugar de 3
- `WeatherScreen` reemplaza completamente a `WeatherDataScreen` con funcionalidad GPS mejorada
- Mantiene consistencia con tabs existentes
- Usa sistema de paddings existente del Scaffold
- Tab principal ahora es la pantalla GPS con búsqueda manual integrada

#### Tareas Nuevas Generadas
- **T111**: Actualizar DI de WeatherRemoteDataSource con named qualifiers
- **T112**: Actualizar DI de GeocodingRemoteDataSource con named qualifiers
- **T113**: Reemplazar WeatherDataScreen por WeatherScreen en HomeScreen
- **T114**: Documentar fixes en spec.md

#### Fix 3: Refinamientos Manuales Post-Implementación
**Cambios Identificados**: Mejoras en la integración de tabs y pasaje de paddings

**Archivos Modificados**:

1. **HomeScreen.kt** - Ajuste de paddings en tabs
   - Línea 33: Agregado import `WeatherHourlyTab`
   - Línea 48: `WeatherTab.create(Modifier.padding(paddingState))` con padding explícito
   - Líneas 65-66: Ambas tabs reciben `Modifier.padding(paddingState)` para paddings consistentes

2. **WeatherTab.kt** - Soporte para modifier con padding
   - Línea 19: Actualizado `fun create(modifier: Modifier)` para aceptar modifier
   - Permite control externo de paddings desde HomeScreen

3. **WeatherHourlyTab.kt** - Nueva abstracción creada
   - Creado objeto factory para `WeatherDataHourlyScreen`
   - Línea 8: `fun create(modifier: Modifier) = WeatherDataHourlyScreen(modifier)`
   - Mantiene consistencia con patrón de `WeatherTab`

4. **WeatherDataHourlyScreen.kt** - Actualización para Tab
   - Línea 14: Constructor actualizado `private val modifier: Modifier = Modifier`
   - Líneas 16-28: Implementación de `TabOptions` con index=1u
   - Ahora implementa correctamente interfaz `Tab` de Voyager

5. **WeatherScreen.kt** - Ajustes menores
   - Línea 57: Constructor acepta `modifier: Modifier` para paddings externos
   - Línea 112: `Scaffold(modifier = modifier)` aplica padding recibido

6. **WeatherScreenModel.kt** - Sin cambios funcionales detectados
   - Código permanece igual a la implementación original

7. **LocationProvider.kt** - Sin cambios detectados
   - Implementación estable desde Phase 2

**Beneficios de los Refinamientos**:
- ✅ Paddings consistentes entre todas las tabs
- ✅ Patrón factory unificado (`WeatherTab.create()`, `WeatherHourlyTab.create()`)
- ✅ Mejor encapsulación con modifiers externos
- ✅ Arquitectura más limpia y mantenible

**Build Status**: ✅ Compilación exitosa (./gradlew build)
**Tests**: ✅ Tests unitarios pasando
**Cambios Mínimos**: ✅ Solo modificaciones necesarias
**PRD Mantenido**: ✅ Sin cambios en requisitos originales
**Constitución Respetada**: ✅ Sigue estándares técnicos
