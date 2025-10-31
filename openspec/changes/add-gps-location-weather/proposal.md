# Proposal: Add GPS-Based Location Weather Detection

## Why

Actualmente, la aplicación solo muestra el clima de "Bogotá" hardcodeado. Los usuarios no tienen forma de:
- Obtener clima de su ubicación actual automáticamente
- Cambiar la ubicación manualmente ingresando una ciudad
- Elegir entre detección automática (GPS) o entrada manual

Esta limitación reduce significativamente la utilidad de la app, ya que los usuarios fuera de Bogotá no pueden acceder a información meteorológica relevante para su ubicación.

## What Changes

Esta propuesta introduce **detección de ubicación basada en GPS con opción de entrada manual**, proporcionando una interfaz mínima funcional para obtener clima actual de cualquier ubicación.

### Nuevas Funcionalidades

1. **Detección automática de ubicación vía GPS**
   - Solicitud de permisos de ubicación (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION)
   - Uso de FusedLocationProviderClient para obtener coordenadas actuales
   - Integración con flujo existente de Weather API

2. **Entrada manual de ubicación**
   - Input de texto para búsqueda de ciudad
   - Opción para cambiar entre modo automático (GPS) y manual
   - Persistencia de preferencia de usuario

3. **UI mínima funcional**
   - Botón "Usar mi ubicación" en Home Screen
   - Input de búsqueda de ciudad
   - Diálogo de solicitud de permisos con explicación
   - Estados de loading durante detección GPS
   - Manejo de errores (GPS desactivado, permiso denegado, sin ubicación)

4. **Gestión de permisos**
   - Request runtime de permisos Android
   - Manejo de grant/deny/never ask again
   - Educación al usuario sobre por qué se necesita el permiso

### Exclusiones (Fuera de Scope)

- ❌ Widgets de home screen
- ❌ Alertas complejas o notificaciones
- ❌ Historial de ubicaciones visitadas
- ❌ Configuraciones avanzadas (unidades, refresh intervals, etc.)
- ❌ Múltiples ubicaciones favoritas
- ❌ Cache local de ubicaciones (se implementará en futuro)

### Componentes Afectados

**Nuevos:**
- `domain/usecase/GetCurrentLocationUseCase` - Obtener ubicación del dispositivo
- `domain/repository/LocationRepository` - Interface para acceso a ubicación
- `data/local/LocationDataSource` - DataSource de ubicación GPS
- `data/repository/LocationRepositoryData` - Implementación de repositorio
- `commons/PermissionManager` - Gestión de permisos de ubicación
- `ui/components/LocationSearchComponent` - Input de búsqueda
- `ui/components/LocationPermissionDialog` - Diálogo de permiso

**Modificados:**
- `ui/screen/home/HomeScreen.kt` - Agregar UI de selección de ubicación
- `ui/screen/home/HomeScreenModel.kt` - Manejar eventos de GPS/búsqueda
- `ui/screen/home/HomeContract.kt` - Nuevos eventos y estados para ubicación
- `AndroidManifest.xml` - Agregar permisos de ubicación
- `gradle/libs.versions.toml` - Agregar versión de Play Services Location
- `app/build.gradle.kts` - Agregar dependencia de Play Services Location
- `di/DataModule.kt` - Inyectar LocationRepository y DataSource
- `di/AppModule.kt` - Inyectar PermissionManager

## Impact

### Affected Specs
- **NEW:** `specs/location-detection/spec.md` - Nueva capacidad de detección de ubicación
- **MODIFIED:** `specs/home-screen/spec.md` - Home screen ahora tiene selección de ubicación
- **MODIFIED:** `specs/weather-display/spec.md` - Weather display ahora muestra ubicación dinámica

### Affected Code

**Domain Layer:**
- `domain/usecase/` - 1 nuevo UseCase
- `domain/repository/` - 1 nueva interface
- `domain/entity/` - Potencialmente 1 nueva entity (LocationEntity)

**Data Layer:**
- `data/local/` - Nueva carpeta con LocationDataSource
- `data/repository/` - 1 nueva implementación
- `di/` - Modificaciones a módulos Koin

**UI Layer:**
- `ui/screen/home/` - 3 archivos modificados (Screen, ScreenModel, Contract)
- `ui/components/` - 2 nuevos componentes
- `commons/` - 1 nuevo PermissionManager

**Archivos de configuración:**
- `AndroidManifest.xml` - Permisos
- `gradle/libs.versions.toml` - Versión de Play Services Location
- `app/build.gradle.kts` - Dependencias

**Testing:**
- `test/.../domain/usecase/GetCurrentLocationUseCaseTest.kt` - Nuevo
- `test/.../data/local/LocationDataSourceTest.kt` - Nuevo
- `test/.../ui/screen/home/HomeScreenModelTest.kt` - Modificar existente

### Estimated Effort
- **Complexity:** Media
- **Time estimate:** 2-3 semanas (10-15 días hábiles)
- **Team size:** 1 developer
- **Risk level:** Bajo-Medio
  - Risk: Diferentes comportamientos de GPS en fabricantes Android
  - Mitigation: Testing extensivo en múltiples dispositivos

### Breaking Changes
- **NO** breaking changes en API pública
- **NO** breaking changes en arquitectura existente
- Cambio interno: HomeScreen ya no hardcodea "Bogotá", pero es transparente para el usuario

## Success Criteria

1. ✅ Usuario puede ver clima de su ubicación actual presionando botón "Usar mi ubicación"
2. ✅ Usuario puede buscar clima de cualquier ciudad ingresando nombre manualmente
3. ✅ Sistema solicita permisos de ubicación correctamente con explicación clara
4. ✅ App maneja gracefully permisos denegados (muestra mensaje, permite entrada manual)
5. ✅ App maneja GPS desactivado (muestra mensaje instructivo)
6. ✅ App muestra loading state mientras obtiene ubicación GPS
7. ✅ Tests unitarios pasan al 100% en domain/data
8. ✅ App funciona en dispositivos Android 7.0+ (API 24+)

## Timeline (Estimation)

### Week 1 (Days 1-5): Foundation & Domain Layer
- Day 1-2: Setup (permissions, dependencies, entities)
- Day 3-4: Domain layer (UseCase, Repository interface)
- Day 5: Data layer (LocationDataSource implementation)

### Week 2 (Days 6-10): UI & Integration
- Day 6-7: Permission handling & PermissionManager
- Day 8-9: UI components (LocationSearchComponent, LocationPermissionDialog)
- Day 10: HomeScreen modifications (integrate location selection)

### Week 3 (Days 11-15): Testing & Polish
- Day 11-12: Unit tests (domain/data)
- Day 13: Integration testing & bug fixes
- Day 14: Manual testing on multiple devices
- Day 15: Code review & documentation

## Dependencies

### External Dependencies
- `com.google.android.gms:play-services-location` (añadir versión en `gradle/libs.versions.toml`)
  - Versión recomendada: 21.3.0 o última estable

### Internal Dependencies
- Requiere que existan `GetWeatherGeoDataUseCase` y `GetWeatherDataUseCase` ✅ (ya existen)
- Requiere HomeScreen funcional ✅ (ya existe)
- Requiere MVI pattern establecido ✅ (ya existe)

### Platform Requirements
- Android API 24+ (ya cumplido)
- Google Play Services disponibles en dispositivo (verificar en runtime)

## Alternatives Considered

### Alternative 1: Solo entrada manual (sin GPS)
- **Pros:** Más simple, no requiere permisos
- **Cons:** Peor UX, usuarios deben escribir ciudad cada vez
- **Decision:** Rechazado - GPS es valor agregado importante

### Alternative 2: Solo GPS (sin entrada manual)
- **Pros:** Más simple de implementar
- **Cons:** No funciona si usuario deniega permiso o no tiene GPS
- **Decision:** Rechazado - debe haber fallback manual

### Alternative 3: Usar Accompanist Permissions
- **Pros:** Librería robusta de Google para permisos en Compose
- **Cons:** Dependencia adicional
- **Decision:** Evaluaremos en implementación, pero probablemente usaremos API nativa por simplicidad

## Open Questions

1. **¿Persistir última ubicación seleccionada?**
   - Consideración: ¿Guardar última ciudad buscada o coordenadas GPS?
   - Respuesta tentativa: SÍ, guardar última ubicación seleccionada en SharedPreferences/DataStore (scope extendido opcional)

2. **¿Soportar background location?**
   - Consideración: ¿Actualizar ubicación cuando app está en background?
   - Respuesta: NO por ahora, solo foreground location (menor fricción de permisos)

3. **¿Qué hacer si Play Services no está disponible?**
   - Consideración: Algunos dispositivos custom (China) no tienen Play Services
   - Respuesta: Fallback a entrada manual, mostrar mensaje educativo

4. **¿Incluir botón de refrescar ubicación en WeatherDataScreen?**
   - Consideración: Permitir re-obtener ubicación desde pantalla de clima
   - Respuesta tentativa: NO en MVP, mantener cambio de ubicación solo en Home

## Notes

- Esta feature es foundational para futuras mejoras (notificaciones, múltiples ubicaciones, widgets)
- Seguir estrictamente Clean Architecture: toda lógica GPS en data layer, UseCases en domain
- Todos los tests unitarios obligatorios en domain/data antes de merge
- Documentar proceso de testing en múltiples dispositivos para futuros developers
