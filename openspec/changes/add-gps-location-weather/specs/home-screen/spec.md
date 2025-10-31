# Home Screen Capability

## ADDED Requirements

### Requirement: Location Selection UI
El Home Screen SHALL proporcionar UI para que el usuario seleccione cómo obtener su ubicación.

#### Scenario: Display location input options
- **GIVEN** el usuario abre la aplicación
- **WHEN** la pantalla Home se carga
- **THEN** el sistema MUST mostrar dos opciones de entrada de ubicación:
  - Botón "Usar mi ubicación" con icono de GPS
  - Input de búsqueda con placeholder "Buscar ciudad..."
- **AND** ambas opciones MUST ser claramente visibles y accesibles

#### Scenario: GPS button interaction
- **GIVEN** el usuario ve las opciones de ubicación
- **WHEN** el usuario presiona "Usar mi ubicación"
- **THEN** el sistema MUST disparar evento `OnRequestGPSLocation`
- **AND** el sistema MUST iniciar flujo de permisos si es necesario
- **AND** el sistema MUST mostrar loading state en el botón

#### Scenario: Manual search interaction
- **GIVEN** el usuario ve las opciones de ubicación
- **WHEN** el usuario toca el input de búsqueda
- **THEN** el sistema MUST mostrar teclado
- **AND** el sistema MUST permitir ingresar texto
- **WHEN** el usuario presiona Enter o botón de búsqueda
- **THEN** el sistema MUST disparar evento `OnSearchCity` con el texto ingresado

### Requirement: Location Permission Dialog
El Home Screen SHALL mostrar diálogos educativos para solicitud de permisos de ubicación.

#### Scenario: Show rationale before permission request
- **GIVEN** el usuario presionó "Usar mi ubicación"
- **AND** permisos NO han sido solicitados anteriormente
- **WHEN** el sistema necesita solicitar permiso
- **THEN** el sistema MUST mostrar diálogo con:
  - Título: "Acceso a ubicación"
  - Mensaje: "Necesitamos tu ubicación para mostrarte el clima de tu zona actual"
  - Botón "Permitir"
  - Botón "Cancelar" o "Usar búsqueda manual"

#### Scenario: Permission rationale accepted
- **GIVEN** el diálogo de rationale está visible
- **WHEN** el usuario presiona "Permitir"
- **THEN** el sistema MUST mostrar diálogo nativo de Android para permisos
- **AND** el diálogo personalizado MUST cerrarse

#### Scenario: Permission rationale rejected
- **GIVEN** el diálogo de rationale está visible
- **WHEN** el usuario presiona "Cancelar" o "Usar búsqueda manual"
- **THEN** el sistema MUST cerrar el diálogo
- **AND** el sistema MUST mantener focus en el input de búsqueda manual
- **AND** NO debe solicitar permiso nativo de Android

### Requirement: Location Loading States in Home Screen
El Home Screen SHALL mostrar estados de carga apropiados para operaciones de ubicación.

#### Scenario: Loading GPS location
- **GIVEN** el usuario solicitó ubicación GPS
- **WHEN** el sistema está obteniendo coordenadas
- **THEN** el botón "Usar mi ubicación" MUST mostrar spinner de carga
- **AND** el botón MUST estar deshabilitado
- **AND** el input de búsqueda SHOULD estar deshabilitado
- **AND** SHOULD mostrar mensaje temporal "Obteniendo ubicación..."

#### Scenario: Loading city search results
- **GIVEN** el usuario buscó una ciudad
- **WHEN** el sistema está consultando geocoding API
- **THEN** el input de búsqueda MUST mostrar spinner inline
- **AND** el input MUST estar deshabilitado
- **AND** el botón GPS SHOULD estar deshabilitado

### Requirement: Location Error Display in Home Screen
El Home Screen SHALL mostrar errores de ubicación de manera clara y actionable.

#### Scenario: GPS location error
- **GIVEN** el usuario solicitó ubicación GPS
- **WHEN** ocurre un error (permiso denegado, GPS desactivado, timeout, etc.)
- **THEN** el sistema MUST mostrar Snackbar o Toast con mensaje de error específico
- **AND** el sistema MUST restaurar estado normal de botón GPS
- **AND** el sistema SHOULD poner focus en input de búsqueda manual

#### Scenario: City not found error
- **GIVEN** el usuario buscó una ciudad
- **WHEN** la ciudad no se encuentra en geocoding API
- **THEN** el sistema MUST mostrar mensaje "Ciudad no encontrada. Intenta con otro nombre"
- **AND** el sistema MUST mantener el texto en el input para que usuario pueda corregir
- **AND** el sistema MUST restaurar estado normal del input

#### Scenario: Network error during location
- **GIVEN** el usuario solicitó ubicación (GPS o manual)
- **WHEN** no hay conexión a internet
- **THEN** el sistema MUST mostrar mensaje "Sin conexión a internet"
- **AND** el sistema SHOULD ofrecer botón "Reintentar"

## MODIFIED Requirements

### Requirement: Home Screen Initial State
**Modified from:** "El Home Screen SHALL mostrar clima de Bogotá por defecto al iniciar"

El Home Screen SHALL permitir al usuario elegir su ubicación al iniciar, sin hardcodear ninguna ciudad por defecto.

#### Scenario: First app launch
- **GIVEN** el usuario abre la app por primera vez
- **AND** NO hay ubicación guardada previamente
- **WHEN** el Home Screen se carga
- **THEN** el sistema MUST mostrar opciones de selección de ubicación (GPS o búsqueda)
- **AND** el sistema MUST mostrar mensaje placeholder "Selecciona tu ubicación para ver el clima"
- **AND** NO debe mostrar datos de clima hasta que usuario seleccione ubicación

#### Scenario: Subsequent app launches with saved location
- **GIVEN** el usuario abre la app
- **AND** existe una ubicación guardada de sesión anterior
- **WHEN** el Home Screen se carga
- **THEN** el sistema MUST cargar clima de la última ubicación seleccionada
- **AND** el sistema SHOULD mostrar nombre de la ubicación actual en top bar
- **AND** el usuario MUST poder cambiar ubicación usando opciones GPS o búsqueda

#### Scenario: Subsequent app launches without saved location
- **GIVEN** el usuario abre la app
- **AND** NO existe ubicación guardada
- **WHEN** el Home Screen se carga
- **THEN** el sistema MUST comportarse como en primer lanzamiento
- **AND** el sistema MUST mostrar opciones de selección de ubicación

### Requirement: Home Screen Navigation
El Home Screen SHALL navegar a pantallas de clima con contexto de ubicación seleccionada por el usuario.

**Modified from:** Original requirement assumed static location context

#### Scenario: Navigate to Weather Data Screen with location
- **GIVEN** el usuario ha seleccionado una ubicación (GPS o manual)
- **AND** el sistema obtuvo coordenadas exitosamente
- **WHEN** el usuario navega a Weather Data tab
- **THEN** el sistema MUST pasar las coordenadas (lat, lon) y nombre de ubicación
- **AND** Weather Data Screen MUST mostrar clima de esa ubicación
- **AND** el nombre de ubicación MUST ser visible en la UI

#### Scenario: Navigate to Hourly Weather with location
- **GIVEN** el usuario ha seleccionado una ubicación
- **AND** el sistema obtuvo coordenadas exitosamente
- **WHEN** el usuario navega a Hourly tab
- **THEN** el sistema MUST pasar las coordenadas para pronóstico horario
- **AND** Hourly Screen MUST mostrar pronóstico de esa ubicación

## REMOVED Requirements

### Requirement: Hardcoded Default City
**Reason:** Hardcoding "Bogotá" como ciudad por defecto limita utilidad de la app y no permite personalización.

**Migration:** Reemplazar LaunchedEffect que enviaba `ChangeUbication("Bogotá")` con lógica de:
1. Verificar si existe ubicación guardada en preferences
2. Si existe, cargar esa ubicación
3. Si NO existe, mostrar UI de selección de ubicación sin datos de clima

**Breaking Change:** NO - El comportamiento cambia pero mejora la UX. Usuarios existentes verán opciones de ubicación en lugar de "Bogotá".
