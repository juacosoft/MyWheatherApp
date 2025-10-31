## Crear constitucion

promt

crea una  #file:constitution.md para spec kit
**Contexto general del proyecto
App existente para android sobre datos del clima
Nivel de api minima: 24
Api de openweathermap.org
Tecnlogias: kotlin, jetpack compose
Librerias: Koin como injector de dependencia , voyager para la navegacion , ktor-client para consumo de api ﻿
Arquitectura: clean arquitecture y patron de presentacion MVI
Para la presentacion se utiliza ScreenModel de voyager en lugar de viewmodel con uso estricto de MvIContract y clase base MVIBaseScreenModel.kt
Test unitarios obligatorios en capa data y domain toda logica de negocio debe ser testeada
Librerias para test: Mockk, junit4 turbine para flows, courtinge-test
commits semantics
**Proyeccion para futuras funcionalidades
Cache local con Room o DataStore
Tema claro y oscuro
escribe una constitucion completa  y bien estructuada que me guie en seguir el desarrollo con pricipios tecnicos y de calidad, actualmente la app solo es para sistema operativo Android

## Crear PRD

Promt:
teniendo en cuenta nuestra constitucion técnica .specify/memory/constitution.md, continuemos con la construcción de la siguientes funcionalidades

***Caracteristica
Clima actual con GPS
Solicitur de permisos de ubicación
Opcion para elegir o no escribir ubicación o detectar automaticamente
Interface minima funcional

**Excluid por ahora
Widgets
Alertas complejas
Historial de ubicación
Configuraciones avanzadas
Genera un PRD conciso de esta nueva caracteristica, enfocandote en una entrega de no mas de 2-3 semanas

(Luego de que se cree el PRD pide generar el spectkit.specify)

## Generar el plan
Ahora generar el plan tecnico usando /spectkit.plan
** Incluir en el plan
Arquitectura detallada como se estructuraran los modulos y capas
Stact tecnologícos y librerías, frameworks y versiones, (en lo posible solo usar librerias oficiales)
Diseño de apis-endpoints, modelos de datos, contratos
estrategia de testing, que probar y como
consideraciones de performance
gestion de dependencias en lo minimo agregar nuevas, y mantener  versiones de las librerías existentes

**Recuerda nuestra constitution
clean arquitecture + MVI
kotlin + jetpack compose
Apilevel minima 24
Test obligatioros en logica de negocio

¿Tienes alguna duda para generar el plan?

(luego se genera el plan y un research)
(se creo archivo data-model.md)

En los checklist hace una re-evaluacion si no la hace tirar el siguiente promt

## Revision del plan 
Antes de aprobar el plan, necesito que confirmes
estan bien todas las dependencias alineadas con el constitution?
El diseño de modulos sigue con clean arquitecture
La estrategia de testing esta correcta?
hay consideraciones para manejo de errores y carga?
el plan es realista para implementar en 2-3 semanas
Responde estas preguntas y ajusta el plan de ser necesario 

(se genera el archivo de task /speckit.tasks)
ejecutar en claude char 


Se itera sobre mejoras en el desarrollo

Actualizar el SPEC existente + fixes sobre lo realizado
problemas identificados

Agregar la nueva pantalla  WeatherScreen al HomeScreen, teniendo en cuenta los paddings y que se usa un scafold en la HomeScreen
WeatherRemoteDataSource y GeocodingRemoteDataSource tiene falla en la inyeccion de dependencia por que necesita especificarse el named named(WEATHER_HTTP_CLIENT) para el httpClient y named(WEATHER_API_KEY) para la api key, esto por que podrías manejar apikey diferentes para mas adelante para producción y develop (no realizar ahora solo usar el named actual
Realizar los cambios minimos posibles para realizar estas mejoras descritas
actualizar el spect actual
crear las nuevas task especificas para estas mejoras
Mantener el PRD y la constitucion

(se itera con cambios manuales)
necesito documentar los cambios manuales realizados en el proyecto
lista de archivos:
app/src/main/java/com/mtzdev/mywheatherapp/data/location/LocationProvider.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/screen/home/HomeScreen.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/screen/hourly/WeatherDataHourlyScreen.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreen.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/WeatherScreenModel.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/navigation/WeatherHourlyTab.kt modified: app/src/main/java/com/mtzdev/mywheatherapp/ui/weather/navigation/WeatherTab.kt
Por favor:
1. Actualiza los specs afectados con estos cambios
2. Marca como completadas las tasks correspondientes  
3. Genera un resumen de lo implementado vs lo planeado
se creo un archivo con los cambios manuales manual-changes-summary.md
