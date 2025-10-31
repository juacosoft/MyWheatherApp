# MyWeatherApp - Project Constitution

## Purpose

MyWeatherApp es una aplicación nativa de Android que proporciona información meteorológica en tiempo real mediante integración con OpenWeatherMap API. El propósito principal es ofrecer a los usuarios datos climáticos precisos, actualizados y presentados de manera clara e intuitiva.

**Objetivos principales:**
- Proporcionar datos meteorológicos actuales y pronósticos por ubicación geográfica
- Mantener una arquitectura limpia, escalable y mantenible
- Garantizar alta cobertura de pruebas en lógica de negocio
- Facilitar futuras extensiones (cache local, tema oscuro, notificaciones)

## Tech Stack

### Platform & Language
- **Platform:** Android (minSdk 24 / Android 7.0 - Nougat)
- **Target SDK:** 36
- **Language:** Kotlin 100% (JVM target 11)
- **UI Framework:** Jetpack Compose

### Core Libraries

#### Dependency Injection
- **Koin:** Framework de inyección de dependencias liviano y pragmático
  - Módulos organizados por capa (AppModule, DataModule, ViewModelModule, ApiModule)
  - Preferir definición explícita sobre reflection cuando sea posible

#### Navigation
- **Voyager:** Navegación moderna para Compose
  - `ScreenModel` en lugar de ViewModel estándar
  - `TabNavigator` para navegación bottom bar
  - Integración con Koin mediante `voyager-koin`

#### Networking
- **Ktor Client:** Cliente HTTP asíncrono y multiplatform
  - `ktor-client-android` como engine
  - `ktor-client-content-negotiation` para serialización JSON
  - `ktor-client-logging` para debugging de requests
  - Configuración centralizada en extensiones (`KtorClientConfigExtensions.kt`)

#### Serialization
- **Kotlinx Serialization:** Manejo de JSON type-safe
  - Anotaciones `@Serializable` en modelos de respuesta/request
  - Convención de naming: `@SerialName` para mapeo explícito

#### Testing
- **JUnit 4:** Framework de testing estándar
- **MockK:** Librería de mocking para Kotlin
- **Coroutine Test:** Testing de código asíncrono
- **Turbine:** Testing de Kotlin Flows
- **Koin Test:** Testing de módulos de inyección de dependencias

## Project Conventions

### Code Style

#### Naming Conventions
- **Packages:** lowercase sin guiones bajos (e.g., `com.mtzdev.mywheatherapp.domain.usecase`)
- **Classes:** PascalCase
  - Interfaces: nombre descriptivo (e.g., `WeatherDataRepository`)
  - Implementaciones: sufijo descriptivo (e.g., `WeatherDataRepositoryData`, `WeatherDataDataSourceRemote`)
  - Data classes: sufijo según capa
    - `*Response` para modelos de API
    - `*Model` para modelos de dominio procesados
    - `*Entity` para entidades de dominio puras
    - `*Params` para parámetros de request
- **Functions:** camelCase, comenzar con verbo (e.g., `getWeatherData`, `handleEvent`)
- **Properties:** camelCase
- **Constants:** UPPER_SNAKE_CASE en object `Constants`
- **Sealed classes/interfaces:** Para resultados y estados (e.g., `WeatherDataResult`, `WeatherGeoResult`)

#### File Organization
- **One class per file:** Excepto para clases pequeñas relacionadas (e.g., sealed interfaces con implementaciones simples)
- **File naming:** Mismo nombre que la clase principal
- **Extension functions:** Sufijo `Extensions.kt` (e.g., `KtorClientConfigExtensions.kt`, `IntExtensions.kt`)

#### Code Formatting
- **Indentation:** 4 espacios (no tabs)
- **Line length:** 120 caracteres máximo
- **Import organization:** Remover imports no utilizados, agrupar por paquete
- **Trailing commas:** Obligatorias en listas de parámetros multi-línea

#### Documentation
- **KDoc:** Obligatorio para:
  - Clases públicas (propósito y responsabilidad)
  - Funciones públicas complejas o con lógica no obvia
  - Interfaces (contrato esperado)
- **Comments:** En español, explicar el "por qué", no el "qué"
- **TODOs:** Formato `TODO(responsable): descripción`

### Architecture Patterns

#### Clean Architecture - Capas Obligatorias

```
presentation (ui)
    ↓ (depende de)
domain
    ↑ (implementado por)
data
```

**1. Capa de Presentación (ui/)**
- **Responsabilidad:** Renderizado de UI y manejo de interacción del usuario
- **Componentes:**
  - `Screen`: Composables de pantalla completa
  - `ScreenModel`: Gestión de estado MVI (ver sección MVI)
  - `Contract`: Definiciones de UiState, UiEvent, Effect
  - `Components`: Componentes reutilizables de UI
- **Reglas:**
  - NO debe contener lógica de negocio
  - Solo depende de domain (UseCases, Entities, Results)
  - Usa `ScreenModel` de Voyager, NO `ViewModel` de Android

**2. Capa de Dominio (domain/)**
- **Responsabilidad:** Lógica de negocio pura, independiente de frameworks
- **Componentes:**
  - `usecase/`: Casos de uso (e.g., `GetWeatherDataUseCase`)
  - `entity/`: Entidades de dominio puras (e.g., `WeatherDataEntity`)
  - `repository/`: Interfaces de repositorios (contratos)
  - `*Result`: Sealed interfaces para resultados (Success, Error)
- **Reglas:**
  - NO debe depender de data ni presentation
  - NO debe tener dependencias de Android framework
  - Toda función debe ser testeable sin mocks de Android
  - UseCases deben ser clases con `operator fun invoke`

**3. Capa de Data (data/)**
- **Responsabilidad:** Implementación de acceso a datos (API, local, cache)
- **Componentes:**
  - `repository/`: Implementaciones de repositorios (sufijo `*RepositoryData`)
  - `remote/`: DataSources remotos (sufijo `*DataSourceRemote`)
  - `services/`: Interfaces de servicios Ktor
  - `model/`: Modelos de datos
    - `request/`: Parámetros de API (`*Params`)
    - `response/`: Respuestas de API (`*Response`, `*Model`)
- **Reglas:**
  - Implementa interfaces definidas en domain
  - Maneja errores de red y mapea a domain results
  - Transformaciones de modelo: Response → Entity

#### Dependency Rule
- **Absoluta:** Las dependencias siempre apuntan hacia adentro
  - `data → domain` ✓
  - `ui → domain` ✓
  - `domain → data` ✗ (usar interfaces)
  - `domain → ui` ✗ (nunca)

### MVI Pattern (Presentation Layer)

#### Componentes Obligatorios

**1. MVIContract**
- Ubicación: `commons/MVIContract.kt`
- Define interfaces marcadoras:
  ```kotlin
  interface MVIContract {
      interface UiState
      interface UiEvent
      interface Effect
  }
  ```

**2. MVIBaseScreenModel**
- Ubicación: `commons/MVIBaseScreenModel.kt`
- Clase abstracta base para todos los ScreenModels
- **Obligatorio heredar de esta clase**
- Proporciona:
  - Gestión de estado (`StateFlow<UiState>`)
  - Gestión de efectos (`SharedFlow<Effect>`)
  - Función `setEvent(UiEvent)` para entrada unidireccional
  - Función abstracta `handleEvent(UiEvent)` a implementar
  - Función `sendEffect(() -> Effect)` para efectos secundarios

**3. Contract por Screen**
- Ubicación: `ui/screen/[nombre]/[Nombre]Contract.kt`
- Define los 3 componentes MVI para cada pantalla:
  ```kotlin
  sealed interface [Screen]Contract {
      data class UiState(...) : MVIContract.UiState
      sealed interface UiEvent : MVIContract.UiEvent { ... }
      sealed interface Effect : MVIContract.Effect { ... }
  }
  ```

**4. ScreenModel por Screen**
- Ubicación: `ui/screen/[nombre]/[Nombre]ScreenModel.kt`
- Hereda de `MVIBaseScreenModel<UiState, UiEvent, Effect>`
- Constructor: Inyectar UseCases necesarios
- Implementar `handleEvent(event: UiEvent)` con `when` exhaustivo
- Actualizar estado con `mutableState.value = currentState.copy(...)`
- Emitir efectos con `sendEffect { Effect.ShowError(...) }`

**5. Screen Composable**
- Ubicación: `ui/screen/[nombre]/[Nombre]Screen.kt`
- Implementa `cafe.adriel.voyager.core.screen.Screen`
- Obtener ScreenModel: `val screenModel = getScreenModel<[Nombre]ScreenModel>()`
- Observar estado: `val state by screenModel.state.collectAsState()`
- Observar efectos:
  ```kotlin
  LaunchedEffect(Unit) {
      screenModel.effect.collect { effect ->
          when(effect) { ... }
      }
  }
  ```
- Enviar eventos: `screenModel.setEvent(UiEvent.OnButtonClick)`

#### Flujo de Datos MVI

```
User Interaction → UiEvent → handleEvent()
    → Update State / Send Effect
    → UI Recomposition
```

**Reglas:**
- El estado es la única fuente de verdad
- Los eventos son inmutables e intencionales
- Los efectos son one-shot (navegación, toasts, snackbars)
- NO modificar estado directamente desde UI
- NO manejar lógica de negocio en ScreenModel (delegar a UseCases)

### Testing Strategy

#### Coverage Requirements

**Obligatorio 100% de cobertura:**
- **Capa Domain:** Todos los UseCases deben tener tests unitarios completos
- **Capa Data:** Todos los DataSources y Repositories deben tener tests unitarios

**Opcional pero recomendado:**
- **ScreenModels:** Tests de flujo MVI (estados, eventos, efectos)
- **Integration tests:** Tests de flujo completo (UseCase → Repository → DataSource)

#### Testing Tools y Patterns

**1. Tests Unitarios (JUnit 4)**
- Ubicación: `src/test/java/com/mtzdev/mywheatherapp/`
- Naming: `[ClaseATestear]Test.kt`
- Estructura de función:
  ```kotlin
  @Test
  fun `should [comportamiento esperado] when [condición]`() {
      // Given (Arrange)
      val input = ...

      // When (Act)
      val result = ...

      // Then (Assert)
      assertEquals(expected, result)
  }
  ```

**2. Mocking con MockK**
- Crear mocks: `mockk<Interface>()`
- Relaxed mocks: `mockk<Interface>(relaxed = true)` para stubs simples
- Stubbing: `every { mock.function() } returns value`
- Corutinas: `coEvery { mock.suspendFunction() } returns value`
- Verificación: `verify { mock.function() }`, `coVerify { mock.suspendFunction() }`

**3. Testing de Coroutines**
- Usar `MainCoroutineRule` para dispatcher de tests:
  ```kotlin
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()
  ```
- Ejecutar tests síncronos: `runTest { ... }`
- Avanzar tiempo: `advanceUntilIdle()`

**4. Testing de Flows con Turbine**
- Verificar emisiones:
  ```kotlin
  flow.test {
      assertEquals(expected1, awaitItem())
      assertEquals(expected2, awaitItem())
      awaitComplete()
  }
  ```
- Uso en ScreenModels:
  ```kotlin
  screenModel.state.test {
      assertEquals(initialState, awaitItem())
      screenModel.setEvent(event)
      assertEquals(expectedState, awaitItem())
  }
  ```

**5. Testing de Koin Modules**
- Usar `KoinTest` interface
- Setup:
  ```kotlin
  @Before
  fun setup() {
      startKoin { modules(moduleToTest) }
  }

  @After
  fun teardown() {
      stopKoin()
  }
  ```
- Verificar inyección: `val instance = get<Interface>()`

#### Test Data
- Ubicación centralizada: `src/test/java/.../Fakedata.kt`
- Crear builders de datos fake reutilizables
- Naming: `fake[Entity]` (e.g., `fakeWeatherDataEntity()`)

#### Assertions
- Preferir `assertEquals(expected, actual)` sobre `assertTrue(actual == expected)`
- Para colecciones: `assertContentEquals`, `assertContains`
- Para excepciones: `assertFailsWith<ExceptionType> { ... }`
- Para null: `assertNull`, `assertNotNull`

### Git Workflow

#### Branching Strategy
- **main/master:** Rama principal, siempre estable y deployable
- **Feature branches:** `feature/[descripcion-breve]` o nombre descriptivo
  - Ejemplo: `add-dark-theme`, `refactor-weather-service`
- **Bugfix branches:** `bugfix/[descripcion]`
- **Lifecycle:** Branch → Development → PR → Review → Merge → Delete branch

#### Commit Message Convention (Semantic Commits)

**Formato obligatorio:**
```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Types permitidos:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `refactor`: Refactorización sin cambio de funcionalidad
- `test`: Agregar o modificar tests
- `docs`: Documentación
- `style`: Formato, espacios, punto y coma faltante (no cambio de lógica)
- `perf`: Mejora de performance
- `chore`: Tareas de mantenimiento (actualización de dependencias, configs)
- `build`: Cambios en sistema de build o dependencias externas
- `ci`: Cambios en configuración de CI/CD

**Scope (opcional pero recomendado):**
- Capa: `domain`, `data`, `ui`
- Feature: `weather`, `geo`, `navigation`, `theme`
- Componente: `repository`, `usecase`, `screenmodel`

**Ejemplos:**
```
feat(ui): add dark theme toggle in settings screen

fix(data): handle null response in WeatherDataDataSourceRemote

refactor(domain): extract common logic into BaseUseCase

test(data): add unit tests for WeatherGeoRepository

docs: update README with API key setup instructions

chore(deps): update Koin to version 3.5.0
```

**Reglas:**
- Subject en minúsculas, sin punto final
- Subject en imperativo ("add" no "added" ni "adds")
- Subject máximo 72 caracteres
- Body opcional para explicar el "por qué" y contexto
- Footer opcional para referencias (issues, breaking changes)

#### Pull Request Guidelines
- **Título:** Mismo formato que commit message (si es un solo commit)
- **Descripción mínima:**
  - Qué se cambió y por qué
  - Cómo probar
  - Screenshots (si aplica para UI)
- **Checklist pre-PR:**
  - [ ] Builds sin errores
  - [ ] Tests pasan (unit tests obligatorios)
  - [ ] Código formateado
  - [ ] Sin TODOs no resueltos (o documentados en issue)
  - [ ] Documentación actualizada si es necesario

#### Git Hygiene
- **Commits frecuentes:** Commits pequeños y atómicos
- **No commitear:** Archivos generados, API keys, `local.properties`
- **Gitignore:** Mantener actualizado
- **Squash:** Opcional antes de merge para limpiar historial

## Domain Context

### Weather Data Domain

**Conceptos principales:**
- **WeatherData:** Datos meteorológicos actuales y pronósticos
  - Temperatura (actual, sensación térmica, min/max)
  - Condiciones (descripción, icono)
  - Humedad, presión atmosférica, visibilidad
  - Viento (velocidad, dirección)
  - Timestamps (zona horaria UTC)

- **GeoData:** Información geográfica para localización
  - Nombre de ciudad
  - Coordenadas (latitud, longitud)
  - País, estado (si aplica)

- **Weather Results:** Tipos de resultado sealed para operaciones
  - `Success<T>`: Operación exitosa con datos
  - `Error`: Operación fallida con excepción o mensaje

### External API - OpenWeatherMap

**Endpoints utilizados:**
- **Current Weather:** `/data/2.5/weather`
- **Geocoding:** `/geo/1.0/direct`

**Autenticación:**
- API Key: Almacenada en `local.properties` como `weather_api`
- Inyectada en build como `BuildConfig.WEATHER_API_KEY`
- Enviada como query param `appid`

**Rate limits y consideraciones:**
- Free tier: 60 llamadas/minuto, 1,000,000 llamadas/mes
- Cachear respuestas cuando sea posible (futuro: Room/DataStore)
- Manejar errores de red gracefully

**Convenciones de mapeo:**
- Response de API → Model intermedio (con conversiones)
- Model → Entity de dominio (sin lógica de API)
- Ejemplo: Temperaturas de Kelvin → Celsius en capa data

## Important Constraints

### Technical Constraints

**1. Android Version Support**
- **minSdk 24:** Android 7.0 Nougat (Noviembre 2016)
- **Implicación:** ~97% de dispositivos activos cubiertos
- **Restricciones:** No usar APIs de Android posteriores a API 24 sin checks de versión

**2. Single Platform**
- **Solo Android:** No iOS, ni Web, ni Desktop
- **Implicación:** Usar librerías Android-específicas cuando sea óptimo
- **Futuro:** Arquitectura permite migración a multiplatform (Ktor, Kotlin)

**3. Network Only**
- **Actual:** Todos los datos desde API, sin cache persistente
- **Implicación:** Requiere conexión activa para funcionar
- **Futuro:** Migrar a cache local con Room o DataStore

**4. Build Configuration**
- **Java Target:** JVM 11 (no usar features de Java 17+)
- **Proguard:** Deshabilitado (considerar habilitar en release)

### Architectural Constraints

**1. Clean Architecture Enforcement**
- **Estricto:** Capa domain NUNCA importa de data o ui
- **Violación:** Build debe fallar (considerar usar linters)

**2. MVI Pattern Enforcement**
- **Obligatorio:** Todos los ScreenModels heredan de `MVIBaseScreenModel`
- **Obligatorio:** Todos los ScreenModels tienen Contract separado
- **No permitido:** Usar `ViewModel` de Android directamente

**3. Voyager ScreenModel**
- **No usar:** `androidx.lifecycle.ViewModel`
- **Usar:** `cafe.adriel.voyager.core.model.StateScreenModel` vía `MVIBaseScreenModel`
- **Razón:** Integración con navegación de Voyager y ciclo de vida

**4. Testing Enforcement**
- **Obligatorio:** Test unitario para todo UseCase y Repository nuevo
- **No permitido:** Mergear PR sin tests en domain y data
- **Recomendado:** Tests de ScreenModel antes de PR

### Security Constraints

**1. API Key Management**
- **No commitear:** API keys en código fuente o repositorio
- **Storage:** `local.properties` (en `.gitignore`)
- **Build time:** Inyectado como `BuildConfig` field
- **Distribución:** Documentar proceso para nuevos developers

**2. Network Security**
- **HTTPS only:** Todas las llamadas a OpenWeatherMap vía HTTPS
- **Certificate pinning:** Considerar para producción (futuro)

**3. Data Privacy**
- **Location:** Solicitar permisos de ubicación con justificación clara
- **Data storage:** No almacenar datos sensibles sin encriptar (futuro)

### Performance Constraints

**1. Memory**
- **Target:** Funcionamiento fluido en dispositivos con 2GB RAM
- **Implicación:** Evitar carga de imágenes grandes sin compresión
- **Implicación:** Limitar cache en memoria

**2. Network**
- **Latency:** Asumir conexiones 3G/4G (promedio 300-800ms)
- **Implicación:** Mostrar loading states, evitar bloqueos de UI
- **Implicación:** Retry logic con exponential backoff

**3. Battery**
- **Eficiencia:** Minimizar polling frecuente
- **Futuro:** Usar WorkManager para updates periódicos en background

## External Dependencies

### Core Dependencies (Obligatorias)

**1. OpenWeatherMap API**
- **URL Base:** `https://api.openweathermap.org`
- **Versión:** 2.5 (Current Weather), 1.0 (Geocoding)
- **SLA:** No garantizado (free tier)
- **Documentación:** https://openweathermap.org/api
- **Fallback:** Mostrar mensaje de error amigable, no crashear

**2. Koin**
- **Versión:** BOM (Bill of Materials) - definida en `libs.versions.toml`
- **Módulos utilizados:**
  - `koin-core`: Core DI
  - `koin-android`: Android extensions
  - `koin-test`, `koin-junit`: Testing
- **Actualización:** Seguir versiones estables, probar con tests de integración

**3. Voyager**
- **Versión:** Definida en `libs.versions.toml`
- **Módulos utilizados:**
  - `voyager-navigator`: Core navigation
  - `voyager-koin`: DI integration
  - `voyager-screenModel`: State management
  - `voyager-tab-navigator`: Bottom navigation
  - `voyager-transitions`: Screen transitions
- **Compatibilidad:** Verificar compatibilidad con versiones de Compose

**4. Ktor Client**
- **Versión:** Definida en `libs.versions.toml`
- **Engine:** Android-specific
- **Plugins obligatorios:**
  - Content Negotiation (JSON)
  - Logging (solo debug builds)
- **Actualización:** Probar exhaustivamente, verificar breaking changes en serialización

### Testing Dependencies

**1. JUnit 4**
- **Razón:** Estándar de facto en Android
- **Migración a JUnit 5:** Evaluar pero no prioritario

**2. MockK**
- **Razón:** DSL idiomático de Kotlin, mejor que Mockito para Kotlin
- **Configuración:** Relaxed mocks por defecto para tests simples

**3. Turbine**
- **Razón:** Mejor herramienta para testing de Flows
- **Uso:** Obligatorio para tests de StateFlow/SharedFlow

**4. Coroutines Test**
- **Razón:** Testing de código suspend y dispatchers
- **Uso:** `runTest`, `TestDispatcher`

### Dependency Management Strategy

**1. Version Catalogs**
- **Ubicación:** `gradle/libs.versions.toml`
- **Convención:** Centralizar todas las versiones
- **Actualización:** Revisar mensualmente, actualizar trimestralmente

**2. Dependency Updates**
- **Automatización:** Considerar Dependabot o Renovate (futuro)
- **Testing:** Siempre correr suite completa de tests antes de merge

**3. Security Vulnerabilities**
- **Monitoreo:** Revisar avisos de seguridad de dependencias
- **Patch rápido:** Actualizar dependencias con CVEs críticos inmediatamente

## Future Roadmap

### Planned Features (Próximas Iteraciones)

**1. Local Cache (Alta Prioridad)**
- **Tecnología:** Room Database o DataStore
- **Propósito:**
  - Funcionamiento offline con datos cached
  - Reducir consumo de API calls
  - Mejorar rendimiento (no esperar a network)
- **Implementación:**
  - Crear capa `data/local/` paralela a `data/remote/`
  - Implementar estrategia de cache (Time-To-Live, Cache-Then-Network)
  - Añadir sincronización en background con WorkManager

**2. Dark Theme (Alta Prioridad)**
- **Tecnología:** Material 3 Dynamic Theming
- **Propósito:**
  - Mejor experiencia en entornos de baja luz
  - Ahorro de batería en pantallas OLED
  - Preferencias de usuario modernas
- **Implementación:**
  - Extender `Theme.kt` con `darkColorScheme`
  - Añadir toggle en Settings Screen
  - Persistir preferencia en DataStore
  - Seguir system theme por defecto

**3. Notifications (Media Prioridad)**
- **Tecnología:** WorkManager + NotificationManager
- **Propósito:**
  - Alertas de clima severo
  - Updates periódicos de condiciones
- **Implementación:**
  - Crear NotificationService
  - Configurar WorkManager periodic tasks
  - Añadir preferencias de notificaciones en Settings

**4. Multi-Location Support (Media Prioridad)**
- **Propósito:** Guardar múltiples ubicaciones favoritas
- **Implementación:**
  - Base de datos local con Room
  - UI de lista de ubicaciones
  - Pantalla de detalle por ubicación

### Technical Improvements

**1. Migration to Kotlin Multiplatform (Largo Plazo)**
- **Preparación actual:**
  - Arquitectura limpia ya es compatible
  - Ktor es multiplatform-ready
  - Voyager soporta multiplatform
- **Beneficio:** Código compartido entre Android/iOS/Web

**2. CI/CD Pipeline**
- **Herramientas:** GitHub Actions o GitLab CI
- **Pipeline:**
  - Build automation
  - Test execution (unit + integration)
  - Lint checks (ktlint, detekt)
  - Code coverage reports
  - Automated releases

**3. Proguard/R8 Optimization**
- **Actual:** Deshabilitado
- **Futuro:** Habilitar en release builds
- **Beneficio:** APK más pequeño, ofuscación de código

**4. Modularization**
- **Actual:** Single-module app
- **Futuro:** Multi-module (`:app`, `:core`, `:feature-weather`, `:data`)
- **Beneficio:** Build times más rápidos, mejor separación

### Quality Improvements

**1. Linting y Static Analysis**
- **Herramientas:** ktlint, detekt
- **Configuración:** Reglas customizadas
- **Enforcement:** Pre-commit hooks, CI checks

**2. UI Testing**
- **Framework:** Espresso + Compose Test
- **Cobertura:** Tests de flujos críticos (happy paths)

**3. Accessibility**
- **Content descriptions:** Obligatorio en todas las imágenes
- **TalkBack support:** Verificar navegación con screen reader
- **Text scaling:** Soportar tamaños de texto grandes

**4. Internationalization (i18n)**
- **Actual:** Strings hardcoded en español/inglés
- **Futuro:** Strings resources para múltiples idiomas
- **Prioridad:** Español, Inglés, Portugués

## Development Workflow

### Setup para Nuevos Developers

1. **Clone repository**
2. **Crear `local.properties` en root:**
   ```properties
   weather_api=YOUR_API_KEY_HERE
   ```
3. **Obtener API Key:** https://openweathermap.org/api → Sign up → Copy key
4. **Sync Gradle** y **Build project**
5. **Run tests:** `./gradlew test` para verificar setup
6. **Leer esta documentación completa**

### Daily Development Checklist

- [ ] Pull últimos cambios de main antes de crear branch
- [ ] Crear feature branch con nombre descriptivo
- [ ] Implementar feature/fix siguiendo arquitectura
- [ ] Escribir tests unitarios (domain y data obligatorios)
- [ ] Correr todos los tests: `./gradlew test`
- [ ] Verificar que builda: `./gradlew assembleDebug`
- [ ] Commit con mensaje semantic
- [ ] Crear PR con descripción clara
- [ ] Esperar review y aprobación
- [ ] Merge y delete branch

### Code Review Guidelines

**Qué revisar (Reviewer):**
- [ ] Respeta Clean Architecture (no violaciones de dependency rule)
- [ ] Sigue MVI pattern correctamente (Contract, ScreenModel, handleEvent)
- [ ] Tests unitarios presentes en domain/data
- [ ] Naming conventions correctas
- [ ] Commits semánticos
- [ ] No hay hardcoded strings (considerar strings.xml)
- [ ] No hay API keys o secrets committeados
- [ ] Lógica de negocio está en UseCases, no en ScreenModels
- [ ] Manejo de errores apropiado

**Autor de PR:**
- Responder a comentarios de manera constructiva
- Explicar decisiones técnicas no obvias
- No tomar feedback personal
- Iterar hasta aprobación

## Principles and Values

### Technical Principles

**1. Simplicity First**
- Preferir solución simple sobre solución "elegante"
- No sobre-ingenierizar
- "You Aren't Gonna Need It" (YAGNI)

**2. Testability**
- Diseñar código testeable desde el principio
- Si es difícil de testear, refactorizar
- Tests son documentación ejecutable

**3. Readability > Cleverness**
- Código se lee 10 veces más de lo que se escribe
- Preferir claridad sobre brevedad extrema
- Nombres descriptivos, funciones pequeñas

**4. Fail Fast**
- Detectar errores lo antes posible
- No esconder excepciones
- Logs informativos en modo debug

**5. Progressive Enhancement**
- Funcionalidad básica primero, optimizaciones después
- Medir antes de optimizar
- No optimizar prematuramente

### Quality Standards

**Definition of Done:**
- [ ] Código implementado y funcional
- [ ] Tests unitarios escritos y passing (domain/data obligatorio)
- [ ] Builds sin warnings
- [ ] Code review aprobado
- [ ] Documentación actualizada si es necesario
- [ ] Merged to main

**Non-Negotiables:**
- Tests unitarios en domain y data (no negociable)
- Clean Architecture (no violaciones de dependency rule)
- MVI pattern (no usar ViewModel directamente)
- Semantic commits
- No commitear secrets

**Best Effort:**
- Tests de ScreenModel (hacer esfuerzo, pero no blocker)
- UI tests (ideal pero no obligatorio por ahora)
- 100% cobertura (aspiracional, no requerido)

### Team Values

**1. Collaboration**
- Code reviews constructivos y respetuosos
- Compartir conocimiento
- Preguntar cuando hay dudas

**2. Continuous Learning**
- Experimentar con nuevas librerías en branches
- Leer release notes de dependencias
- Mantenerse actualizado con Kotlin/Compose/Android

**3. Ownership**
- Responsabilidad del código que escribes
- Fixes de bugs propios prioritarios
- Dejar el código mejor de como lo encontraste

**4. Pragmatism**
- Balance entre perfección y shipping
- Technical debt consciente y documentado
- Refactorizar cuando duele, no por diversión

---

## Document Maintenance

**Última actualización:** 2025-10-31
**Versión:** 1.0.0
**Próxima revisión:** 2025-11-30 o cuando haya cambios arquitecturales significativos

**Cambios requerirán actualización de este documento:**
- Cambio de tecnologías core (Koin, Voyager, Ktor)
- Cambio de arquitectura (Clean, MVI)
- Nuevas convenciones de código o testing
- Cambio de estrategia de branching o commits
- Nuevas constraints o dependencias externas

**Responsable de mantener este documento:** Tech lead del proyecto

---

*Este documento es la constitución técnica del proyecto. Todas las decisiones de desarrollo deben alinearse con los principios y convenciones aquí definidos. Para proponer cambios, crear un proposal en `openspec/changes/` siguiendo el proceso OpenSpec.*
