# Location Detection Capability

## ADDED Requirements

### Requirement: GPS Location Detection
El sistema SHALL obtener la ubicación geográfica actual del dispositivo usando servicios de localización GPS/Network.

#### Scenario: Successful GPS location retrieval
- **GIVEN** el usuario ha otorgado permisos de ubicación
- **AND** el GPS está activado en el dispositivo
- **WHEN** el sistema solicita la ubicación actual
- **THEN** el sistema MUST retornar coordenadas (latitud, longitud) con precisión de al menos 100 metros
- **AND** la operación MUST completarse en menos de 10 segundos

#### Scenario: GPS disabled on device
- **GIVEN** el usuario ha otorgado permisos de ubicación
- **AND** el GPS está desactivado en el dispositivo
- **WHEN** el sistema solicita la ubicación actual
- **THEN** el sistema MUST retornar un error indicando "GPS desactivado"
- **AND** el sistema SHOULD proporcionar una acción para abrir configuración de ubicación

#### Scenario: Location permission not granted
- **GIVEN** el usuario NO ha otorgado permisos de ubicación
- **WHEN** el sistema solicita la ubicación actual
- **THEN** el sistema MUST retornar un error indicando "Permiso denegado"
- **AND** NO debe intentar acceder a servicios de ubicación

#### Scenario: Location timeout
- **GIVEN** el usuario ha otorgado permisos de ubicación
- **AND** el GPS está activado
- **WHEN** el sistema no puede obtener ubicación después de 10 segundos
- **THEN** el sistema MUST retornar un error de timeout
- **AND** el sistema SHOULD sugerir usar entrada manual de ubicación

### Requirement: Location Permission Management
El sistema SHALL solicitar y gestionar permisos de ubicación de acuerdo a políticas de Android.

#### Scenario: First time permission request
- **GIVEN** la app se ejecuta por primera vez
- **AND** el usuario intenta usar "Detectar mi ubicación"
- **WHEN** el sistema solicita permiso de ubicación
- **THEN** el sistema MUST mostrar un diálogo explicativo ANTES del diálogo nativo de Android
- **AND** el diálogo MUST explicar claramente por qué se necesita el permiso
- **AND** el diálogo MUST permitir al usuario cancelar sin solicitar permiso

#### Scenario: Permission granted
- **GIVEN** el sistema solicitó permiso de ubicación
- **WHEN** el usuario otorga el permiso
- **THEN** el sistema MUST proceder a obtener ubicación GPS
- **AND** el sistema MUST almacenar el estado del permiso

#### Scenario: Permission denied
- **GIVEN** el sistema solicitó permiso de ubicación
- **WHEN** el usuario deniega el permiso
- **THEN** el sistema MUST mostrar mensaje indicando que puede usar búsqueda manual
- **AND** el sistema MUST permitir continuar usando la app con entrada manual
- **AND** NO debe volver a solicitar permiso en esta sesión

#### Scenario: Permission permanently denied (Never ask again)
- **GIVEN** el usuario seleccionó "No volver a preguntar" en diálogo de permisos
- **WHEN** el usuario intenta usar "Detectar mi ubicación"
- **THEN** el sistema MUST mostrar diálogo explicativo con opción de ir a Configuración
- **AND** el sistema MUST proporcionar botón para abrir Configuración de la app

### Requirement: Manual Location Input
El sistema SHALL permitir al usuario ingresar manualmente el nombre de una ciudad para obtener clima.

#### Scenario: Valid city name entered
- **GIVEN** el usuario está en la pantalla de búsqueda de ubicación
- **WHEN** el usuario ingresa un nombre de ciudad válido (ej: "London", "Bogotá", "New York")
- **AND** presiona buscar
- **THEN** el sistema MUST llamar a la API de geocoding con el nombre ingresado
- **AND** el sistema MUST obtener coordenadas de la ciudad
- **AND** el sistema MUST mostrar el clima de esa ubicación

#### Scenario: Invalid or unknown city name
- **GIVEN** el usuario está en la pantalla de búsqueda de ubicación
- **WHEN** el usuario ingresa un nombre de ciudad inválido o desconocido
- **AND** presiona buscar
- **THEN** el sistema MUST mostrar mensaje de error "Ciudad no encontrada"
- **AND** el sistema SHOULD mantener el texto ingresado para corrección
- **AND** el sistema SHOULD mostrar sugerencias si es posible

#### Scenario: Empty input
- **GIVEN** el usuario está en la pantalla de búsqueda de ubicación
- **WHEN** el usuario NO ingresa ningún texto
- **AND** presiona buscar
- **THEN** el sistema MUST mostrar mensaje de validación "Por favor ingrese un nombre de ciudad"
- **AND** NO debe realizar llamada a la API

### Requirement: Location Mode Selection
El sistema SHALL permitir al usuario elegir entre detección automática (GPS) o entrada manual de ubicación.

#### Scenario: User selects GPS mode
- **GIVEN** el usuario está en la pantalla principal
- **WHEN** el usuario selecciona "Usar mi ubicación" o botón de GPS
- **THEN** el sistema MUST verificar permisos de ubicación
- **AND** si permisos están otorgados, proceder a obtener ubicación GPS
- **AND** si permisos NO están otorgados, solicitar permisos primero

#### Scenario: User selects manual mode
- **GIVEN** el usuario está en la pantalla principal
- **WHEN** el usuario selecciona "Buscar ciudad" o input de búsqueda
- **THEN** el sistema MUST mostrar teclado con input de texto
- **AND** el sistema MUST permitir escribir nombre de ciudad
- **AND** NO debe solicitar permisos de ubicación

#### Scenario: Switch between modes
- **GIVEN** el usuario está usando un modo de ubicación
- **WHEN** el usuario cambia a otro modo
- **THEN** el sistema MUST cancelar cualquier operación en progreso del modo anterior
- **AND** el sistema MUST actualizar UI para reflejar el modo activo

### Requirement: Location Loading States
El sistema SHALL mostrar estados de carga apropiados durante operaciones de ubicación.

#### Scenario: Loading GPS location
- **GIVEN** el usuario solicitó ubicación GPS
- **WHEN** el sistema está obteniendo coordenadas
- **THEN** el sistema MUST mostrar indicador de carga con mensaje "Obteniendo ubicación..."
- **AND** el sistema MUST deshabilitar botón de "Usar mi ubicación" para evitar requests duplicados
- **AND** el indicador MUST ser visible durante máximo 10 segundos

#### Scenario: Loading weather for manual location
- **GIVEN** el usuario ingresó nombre de ciudad
- **WHEN** el sistema está obteniendo datos de geocoding/weather
- **THEN** el sistema MUST mostrar indicador de carga con mensaje "Buscando ciudad..."
- **AND** el sistema MUST deshabilitar input y botón de búsqueda

### Requirement: Location Error Handling
El sistema SHALL manejar errores de ubicación de manera clara y ofrecer alternativas al usuario.

#### Scenario: No network connection
- **GIVEN** el dispositivo no tiene conexión a internet
- **WHEN** el usuario intenta obtener ubicación (GPS o manual)
- **THEN** el sistema MUST mostrar mensaje "Sin conexión a internet"
- **AND** el sistema SHOULD ofrecer reintentar cuando conexión esté disponible

#### Scenario: Play Services unavailable
- **GIVEN** Google Play Services no está disponible en el dispositivo
- **WHEN** el usuario intenta usar ubicación GPS
- **THEN** el sistema MUST mostrar mensaje explicativo
- **AND** el sistema MUST redirigir automáticamente a modo de búsqueda manual

#### Scenario: Generic location error
- **GIVEN** ocurre un error inesperado durante obtención de ubicación
- **WHEN** el error no está categorizado específicamente
- **THEN** el sistema MUST mostrar mensaje genérico "Error al obtener ubicación"
- **AND** el sistema MUST ofrecer usar búsqueda manual como fallback
- **AND** el sistema SHOULD loggear el error para debugging

### Requirement: Clean Architecture Compliance
La implementación de detección de ubicación SHALL seguir Clean Architecture establecida en el proyecto.

#### Scenario: Domain layer independence
- **GIVEN** la implementación de location detection
- **WHEN** se revisa la capa domain
- **THEN** `GetCurrentLocationUseCase` NO debe tener dependencias de frameworks Android
- **AND** `LocationRepository` interface MUST estar en domain layer
- **AND** NO debe haber imports de `android.*` o `com.google.android.gms.*` en domain

#### Scenario: Data layer implementation
- **GIVEN** la implementación de location detection
- **WHEN** se revisa la capa data
- **THEN** `LocationDataSource` MUST implementar interface definida en domain
- **AND** toda interacción con FusedLocationProviderClient MUST estar en data layer
- **AND** conversiones de tipos Android a entities domain MUST ocurrir en data layer

#### Scenario: Dependency injection
- **GIVEN** la implementación de location detection
- **WHEN** se configuran módulos Koin
- **THEN** `LocationRepository` MUST ser inyectado en `GetCurrentLocationUseCase`
- **AND** `LocationDataSource` MUST ser inyectado en `LocationRepositoryData`
- **AND** todas las dependencias MUST ser testables mediante mocking

### Requirement: Unit Test Coverage
Toda la lógica de ubicación en domain y data layers SHALL tener 100% de cobertura de tests unitarios.

#### Scenario: GetCurrentLocationUseCase tests
- **GIVEN** el UseCase de ubicación
- **WHEN** se ejecutan tests unitarios
- **THEN** MUST existir test para caso de éxito (retorna coordenadas)
- **AND** MUST existir test para caso de error (permiso denegado)
- **AND** MUST existir test para caso de error (GPS desactivado)
- **AND** MUST existir test para caso de timeout
- **AND** todos los tests MUST usar mocks del repository

#### Scenario: LocationDataSource tests
- **GIVEN** el DataSource de ubicación
- **WHEN** se ejecutan tests unitarios
- **THEN** MUST existir test para llamada exitosa a FusedLocationProviderClient
- **AND** MUST existir test para manejo de exception de seguridad (permiso denegado)
- **AND** MUST existir test para manejo de timeout
- **AND** MUST existir test para conversión de Android Location a LocationEntity
- **AND** todos los tests MUST usar mocks de FusedLocationProviderClient
