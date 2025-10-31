# Implementation Tasks: GPS-Based Location Weather Detection

## 1. Project Setup & Configuration

### 1.1 Dependencies & Permissions
- [ ] Agregar versión de Play Services Location en `gradle/libs.versions.toml`
  - Añadir: `playServicesLocation = "21.3.0"` en `[versions]`
  - Añadir: `play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }` en `[libraries]`
- [ ] Agregar dependencia en `app/build.gradle.kts`
  - Añadir: `implementation(libs.play.services.location)` en `dependencies`
- [ ] Agregar permisos en `AndroidManifest.xml`
  - `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`
  - `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`
- [ ] Sync Gradle y verificar que build funciona sin errores

## 2. Domain Layer Implementation

### 2.1 Location Entity
- [ ] Crear `domain/entity/LocationEntity.kt`
  - Data class con: `latitude: Double`, `longitude: Double`, `timestamp: Long`
  - KDoc explicando el propósito de la entity

### 2.2 Location Result
- [ ] Crear `domain/LocationResult.kt` (sealed interface)
  - `data class Success(val location: LocationEntity) : LocationResult`
  - `data class Error(val exception: LocationException) : LocationResult`

### 2.3 Location Exception
- [ ] Crear `domain/LocationException.kt` (sealed class hereda de Exception)
  - `object PermissionDenied : LocationException("Permiso de ubicación denegado")`
  - `object GpsDisabled : LocationException("GPS desactivado en dispositivo")`
  - `object Timeout : LocationException("Timeout al obtener ubicación")`
  - `object PlayServicesUnavailable : LocationException("Google Play Services no disponible")`
  - `data class Unknown(val message: String) : LocationException(message)`

### 2.4 Location Repository Interface
- [ ] Crear `domain/repository/LocationRepository.kt` (interface)
  - `suspend fun getCurrentLocation(): LocationResult`
  - KDoc explicando contrato esperado

### 2.5 Get Current Location UseCase
- [ ] Crear `domain/usecase/GetCurrentLocationUseCase.kt`
  - Constructor: Inyectar `LocationRepository`
  - `operator suspend fun invoke(): LocationResult`
  - Delegar llamada al repository
  - KDoc con ejemplos de uso

## 3. Data Layer Implementation

### 3.1 Location Data Source Interface
- [ ] Crear `data/local/LocationDataSource.kt` (interface)
  - `suspend fun getCurrentLocation(): LocationResult`

### 3.2 Location Data Source Implementation
- [ ] Crear `data/local/LocationDataSourceLocal.kt`
  - Constructor: Inyectar `Context` y `FusedLocationProviderClient`
  - Implementar `getCurrentLocation()`:
    - Verificar permisos usando `ContextCompat.checkSelfPermission`
    - Si no hay permiso, retornar `LocationResult.Error(LocationException.PermissionDenied)`
    - Verificar si GPS está habilitado
    - Si GPS deshabilitado, retornar `LocationResult.Error(LocationException.GpsDisabled)`
    - Usar `fusedLocationProviderClient.lastLocation` primero (más rápido)
    - Si no hay lastLocation, usar `getCurrentLocation()` con timeout de 10 segundos
    - Convertir `android.location.Location` a `LocationEntity`
    - Manejar excepciones: `SecurityException`, timeout, etc.
  - Helper function: `checkLocationSettings()` - verificar si GPS habilitado
  - Helper function: `hasLocationPermission()` - verificar permisos

### 3.3 Location Repository Implementation
- [ ] Crear `data/repository/LocationRepositoryData.kt`
  - Constructor: Inyectar `LocationDataSource`
  - Implementar `getCurrentLocation()` delegando al DataSource

## 4. Common/Utility Layer

### 4.1 Permission Manager
- [ ] Crear `commons/PermissionManager.kt`
  - Constructor: Inyectar `Context` (Application context)
  - `fun hasLocationPermission(): Boolean` - Verificar si permiso está otorgado
  - `fun shouldShowRationale(activity: Activity): Boolean` - Verificar si mostrar rationale
  - `fun isPermissionPermanentlyDenied(activity: Activity): Boolean` - Verificar "never ask again"
  - Helper para generar intents a app settings

### 4.2 Extensions
- [ ] Crear `commons/LocationExtensions.kt` (opcional)
  - Extension: `Context.hasLocationPermission(): Boolean`
  - Extension: `Activity.openAppSettings()` - Abrir configuración de app

## 5. Dependency Injection (Koin Modules)

### 5.1 Data Module Updates
- [ ] Modificar `di/DataModule.kt`
  - Agregar: `single<FusedLocationProviderClient> { LocationServices.getFusedLocationProviderClient(androidContext()) }`
  - Agregar: `single<LocationDataSource> { LocationDataSourceLocal(get(), get()) }`
  - Agregar: `single<LocationRepository> { LocationRepositoryData(get()) }`

### 5.2 Domain Module Updates
- [ ] Modificar `di/ViewModelModule.kt` (o crear DomainModule si no existe)
  - Agregar: `factory { GetCurrentLocationUseCase(get()) }`

### 5.3 App Module Updates
- [ ] Modificar `di/AppModule.kt`
  - Agregar: `single { PermissionManager(androidContext()) }`

## 6. UI Layer - Components

### 6.1 Location Permission Dialog
- [ ] Crear `ui/components/LocationPermissionDialog.kt`
  - Composable function `LocationPermissionDialog`
  - Parámetros:
    - `onAllowClick: () -> Unit`
    - `onDenyClick: () -> Unit`
    - `onDismiss: () -> Unit`
  - Usar `AlertDialog` de Material3
  - Título: "Acceso a ubicación"
  - Mensaje: "Necesitamos tu ubicación para mostrarte el clima de tu zona actual"
  - Botones: "Permitir" y "Usar búsqueda manual"

### 6.2 Location Search Component
- [ ] Crear `ui/components/LocationSearchComponent.kt`
  - Composable function `LocationSearchInput`
  - Parámetros:
    - `searchQuery: String`
    - `onSearchQueryChange: (String) -> Unit`
    - `onSearchSubmit: () -> Unit`
    - `isLoading: Boolean`
    - `enabled: Boolean`
  - Usar `OutlinedTextField` de Material3
  - Placeholder: "Buscar ciudad..."
  - Trailing icon: Search icon
  - Mostrar CircularProgressIndicator si `isLoading`
  - Keyboard actions: `ImeAction.Search`

### 6.3 GPS Location Button Component
- [ ] Crear `ui/components/GpsLocationButton.kt`
  - Composable function `GpsLocationButton`
  - Parámetros:
    - `onClick: () -> Unit`
    - `isLoading: Boolean`
    - `enabled: Boolean`
  - Usar `Button` o `IconButton` con icono de GPS
  - Mostrar CircularProgressIndicator si `isLoading`
  - Text: "Usar mi ubicación"

## 7. UI Layer - Home Screen Updates

### 7.1 Home Contract Updates
- [ ] Modificar `ui/screen/home/HomeContract.kt`
  - Agregar a `UiState`:
    - `isLoadingGpsLocation: Boolean = false`
    - `isLoadingCitySearch: Boolean = false`
    - `locationError: String? = null`
    - `searchQuery: String = ""`
    - `showPermissionDialog: Boolean = false`
    - `showPermissionDeniedMessage: Boolean = false`
  - Agregar a `UiEvent`:
    - `data object OnRequestGpsLocation : UiEvent`
    - `data class OnSearchCity(val cityName: String) : UiEvent`
    - `data class OnSearchQueryChange(val query: String) : UiEvent`
    - `data object OnPermissionDialogAllow : UiEvent`
    - `data object OnPermissionDialogDeny : UiEvent`
    - `data object OnPermissionDialogDismiss : UiEvent`
    - `data object OnPermissionGranted : UiEvent`
    - `data object OnPermissionDenied : UiEvent`
  - Agregar a `Effect`:
    - `data object RequestLocationPermission : Effect`
    - `data object OpenAppSettings : Effect`
    - `data class ShowError(val message: String) : Effect`

### 7.2 Home ScreenModel Updates
- [ ] Modificar `ui/screen/home/HomeScreenModel.kt`
  - Constructor: Inyectar:
    - `GetCurrentLocationUseCase`
    - `GetWeatherGeoDataUseCase` (ya existe)
    - `PermissionManager`
  - Remover LaunchedEffect de "Bogotá" hardcoded
  - Implementar `handleEvent` para nuevos eventos:
    - `OnRequestGpsLocation`:
      - Verificar permisos con `PermissionManager`
      - Si tiene permiso, llamar `GetCurrentLocationUseCase`
      - Si no tiene permiso y shouldShow rationale, mostrar dialog
      - Si no tiene permiso y no shouldShow, enviar Effect.RequestLocationPermission
      - Actualizar estado `isLoadingGpsLocation = true`
    - `OnSearchCity`:
      - Validar que `cityName` no esté vacío
      - Actualizar estado `isLoadingCitySearch = true`
      - Llamar `GetWeatherGeoDataUseCase` con city name
      - Manejar resultado (Success/Error)
    - `OnSearchQueryChange`: Actualizar `searchQuery` en estado
    - `OnPermissionDialogAllow`: Enviar Effect.RequestLocationPermission
    - `OnPermissionDialogDeny`: Cerrar dialog, mantener búsqueda manual
    - `OnPermissionGranted`: Llamar `GetCurrentLocationUseCase`
    - `OnPermissionDenied`: Mostrar mensaje, ofrecer manual search
  - Helper function: `handleGpsLocation(location: LocationEntity)`
    - Llamar `GetWeatherDataUseCase` con coordenadas
  - Helper function: `handleLocationError(error: LocationException)`
    - Mapear exception a mensaje user-friendly
    - Actualizar estado con error
    - Si es PermissionDenied permanente, enviar Effect.OpenAppSettings

### 7.3 Home Screen UI Updates
- [ ] Modificar `ui/screen/home/HomeScreen.kt`
  - Remover LaunchedEffect de "Bogotá"
  - Agregar permission launcher: `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())`
  - Agregar effect collector:
    - `Effect.RequestLocationPermission`: Launch permission request
    - `Effect.OpenAppSettings`: Abrir settings con Intent
    - `Effect.ShowError`: Mostrar Snackbar/Toast
  - Agregar UI de selección de ubicación:
    - `LocationSearchInput` component
    - `GpsLocationButton` component
    - Disposición: Column con ambos componentes, spacing adecuado
  - Conectar eventos:
    - GPS button onClick → `setEvent(OnRequestGpsLocation)`
    - Search input onSubmit → `setEvent(OnSearchCity(query))`
  - Mostrar `LocationPermissionDialog` cuando `state.showPermissionDialog == true`
  - Mostrar loading states usando `state.isLoadingGpsLocation` y `state.isLoadingCitySearch`
  - Mostrar mensaje placeholder si no hay ubicación seleccionada

## 8. Testing - Domain Layer

### 8.1 GetCurrentLocationUseCase Tests
- [ ] Crear `test/.../domain/usecase/GetCurrentLocationUseCaseTest.kt`
  - Setup: Mock `LocationRepository`
  - Test: `should return success when repository returns location`
  - Test: `should return error when repository returns permission denied`
  - Test: `should return error when repository returns gps disabled`
  - Test: `should return error when repository returns timeout`
  - Verificar que UseCase delega correctamente al repository

## 9. Testing - Data Layer

### 9.1 LocationDataSourceLocal Tests
- [ ] Crear `test/.../data/local/LocationDataSourceLocalTest.kt`
  - Setup: Mock `FusedLocationProviderClient`, Mock `Context`
  - Test: `should return success when location is retrieved successfully`
  - Test: `should return permission denied error when permission not granted`
  - Test: `should return gps disabled error when location settings off`
  - Test: `should return timeout error when location takes too long`
  - Test: `should convert Android Location to LocationEntity correctly`
  - Test: `should use lastLocation first if available`
  - Test: `should fallback to getCurrentLocation if lastLocation is null`

### 9.2 LocationRepositoryData Tests
- [ ] Crear `test/.../data/repository/LocationRepositoryDataTest.kt`
  - Setup: Mock `LocationDataSource`
  - Test: `should delegate to data source`
  - Test: `should return success from data source`
  - Test: `should return error from data source`

## 10. Testing - UI Layer

### 10.1 HomeScreenModel Tests Updates
- [ ] Modificar `test/.../ui/screen/home/HomeScreenModelTest.kt`
  - Setup: Mock `GetCurrentLocationUseCase`, Mock `PermissionManager`
  - Test: `should show permission dialog when gps requested without permission`
  - Test: `should request location when gps requested with permission`
  - Test: `should update loading state when requesting gps location`
  - Test: `should handle location success and fetch weather data`
  - Test: `should handle location error and show error message`
  - Test: `should search city when search submitted with valid input`
  - Test: `should not search when query is empty`
  - Test: `should update search query on query change`
  - Usar `Turbine` para verificar emisiones de state y effect

## 11. Manual Testing & QA

### 11.1 Permission Scenarios
- [ ] Test en dispositivo físico: Primera vez, otorgar permiso
- [ ] Test en dispositivo físico: Primera vez, denegar permiso
- [ ] Test en dispositivo físico: Denegar con "No volver a preguntar"
- [ ] Test en dispositivo físico: Abrir settings desde app y otorgar permiso manualmente
- [ ] Test en emulador: Simular GPS habilitado/deshabilitado

### 11.2 GPS Scenarios
- [ ] Test: GPS obtiene ubicación exitosamente en <5 segundos
- [ ] Test: GPS timeout después de 10 segundos
- [ ] Test: GPS deshabilitado en configuración (modo avión)
- [ ] Test: Sin Play Services (emulador sin Google APIs)
- [ ] Test: Precisión de ubicación (comparar con Google Maps)

### 11.3 Manual Search Scenarios
- [ ] Test: Buscar ciudad válida (ej: "London", "Tokyo", "Bogotá")
- [ ] Test: Buscar ciudad con tildes/caracteres especiales
- [ ] Test: Buscar ciudad inexistente o typo
- [ ] Test: Buscar con query vacío
- [ ] Test: Buscar sin conexión a internet

### 11.4 UI/UX Testing
- [ ] Test: Transición smooth entre loading states
- [ ] Test: Mensajes de error son claros y útiles
- [ ] Test: Diálogos de permiso son comprensibles
- [ ] Test: Botones habilitados/deshabilitados apropiadamente
- [ ] Test: Keyboard interactions funcionan correctamente

### 11.5 Integration Testing
- [ ] Test: Flujo completo GPS → Weather Data display
- [ ] Test: Flujo completo Manual Search → Weather Data display
- [ ] Test: Cambiar de GPS a Manual y viceversa
- [ ] Test: App funciona correctamente después de reinicio
- [ ] Test: App maneja cambios de configuración (rotación, dark mode)

### 11.6 Device Compatibility
- [ ] Test en Android 7.0 (API 24) - Dispositivo más antiguo soportado
- [ ] Test en Android 10 (API 29) - Cambios en permisos de ubicación
- [ ] Test en Android 12+ (API 31+) - Permisos de ubicación aproximada/precisa
- [ ] Test en al menos 3 fabricantes diferentes (Samsung, Xiaomi, Pixel)

## 12. Documentation & Code Review

### 12.1 Code Documentation
- [ ] Agregar/actualizar KDoc en todas las clases públicas nuevas
- [ ] Agregar comentarios explicativos en lógica compleja
- [ ] Documentar decisiones técnicas no obvias

### 12.2 README Updates
- [ ] Actualizar README con instrucciones de permisos de ubicación
- [ ] Documentar proceso de testing en múltiples dispositivos
- [ ] Añadir troubleshooting para problemas comunes de GPS

### 12.3 Code Review Checklist
- [ ] Verificar que respeta Clean Architecture (no violaciones)
- [ ] Verificar que sigue MVI pattern correctamente
- [ ] Verificar que tests unitarios existen y pasan (domain/data)
- [ ] Verificar naming conventions
- [ ] Verificar que no hay secrets o hardcoded values
- [ ] Verificar manejo de errores robusto
- [ ] Verificar accesibilidad (content descriptions, TalkBack)

## 13. Deployment Preparation

### 13.1 Build Verification
- [ ] Ejecutar `./gradlew clean build` exitosamente
- [ ] Ejecutar `./gradlew test` - todos los tests pasan
- [ ] Ejecutar `./gradlew assembleDebug` - APK se genera correctamente
- [ ] Verificar que no hay warnings de compilación

### 13.2 Performance Verification
- [ ] Verificar que app no consume batería excesiva
- [ ] Verificar que requests GPS no son demasiado frecuentes
- [ ] Verificar que no hay memory leaks (usar Android Profiler)

### 13.3 Pre-Merge Checklist
- [ ] Todos los tasks anteriores completados
- [ ] Code review aprobado por al menos 1 reviewer
- [ ] Tests unitarios al 100% en domain/data
- [ ] Manual testing completado en al menos 3 dispositivos
- [ ] Documentación actualizada
- [ ] No hay TODOs sin resolver o están documentados en issues

## Notes

- Prioridad alta: Tasks 1-7 (Setup, Domain, Data, UI)
- Prioridad media: Tasks 8-10 (Testing)
- Prioridad baja pero obligatoria: Tasks 11-13 (QA, Docs, Deployment)
- Estimar 1-2 días por sección principal
- Todos los tests unitarios son OBLIGATORIOS antes de merge
- Manual testing en múltiples dispositivos es CRÍTICO para permisos/GPS
