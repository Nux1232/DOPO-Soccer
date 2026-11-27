# Pruebas de aceptación de DOPO-Soccer

## Propósito y alcance

Este documento define diez pruebas de aceptación para el alcance implementado: inicio de partidos, configuración de modalidades, catálogo de países y poderes, persistencia tipada, restauración y recompensas. Incluye escenarios positivos y negativos. En los casos negativos, el resultado correcto es que el sistema rechace la operación de forma controlada y preserve el estado anterior.

Las pruebas describen criterios observables para una ejecución manual o automatizada; no afirman que toda interacción visual de JavaFX esté automatizada. El proyecto cuenta con pruebas JUnit para los contratos del dominio, la fachada, la persistencia y los mappers. La suite ejecutada más recientemente reportó 78 pruebas exitosas.

**Requisitos para ejecutar la suite:** JDK 21 o superior y Maven Wrapper:

```powershell
.\mvnw.cmd test
```

## Resumen

| ID | Escenario | Tipo | Resultado de aceptación |
|---|---|---|---|
| PA-01 | Iniciar un partido por goles | Positivo | El partido queda activo con los países y el límite solicitados. |
| PA-02 | Iniciar y actualizar un partido por tiempo | Positivo | El reloj y la configuración del modo se conservan correctamente. |
| PA-03 | Configurar una modalidad registrada desde la presentación | Positivo | Los datos de UI llegan a la fachada y la modalidad se crea con valores normalizados. |
| PA-04 | Enviar una configuración inválida de modalidad | Negativo | La solicitud o el guardado se rechaza; no se persiste un estado inválido. |
| PA-05 | Solicitar un país o tipo de poder inexistente | Negativo | La creación del partido o del catálogo falla explícitamente. |
| PA-06 | Extender el catálogo con un poder no proyectil | Positivo | El poder personalizado se crea y su estado por jugador sobrevive la restauración. |
| PA-07 | Guardar y restaurar el estado completo en versión actual | Positivo | Jugadores, balón, tiro activo y configuración tipada mantienen su estado. |
| PA-08 | Migrar un guardado de versión 5 | Positivo | Se restaura su configuración textual y el archivo queda reescrito en versión 6. |
| PA-09 | Abrir un guardado de versión no soportada | Negativo | La carga se rechaza y el archivo no se acepta como una partida válida. |
| PA-10 | Entregar recompensas al finalizar, incluyendo fallo recuperable de perfil | Mixto | La recompensa se aplica una sola vez; un fallo de persistencia no la consume. |

## Casos detallados

### PA-01 — Inicio de partido por goles

**Tipo:** Positivo  
**Prioridad:** Alta  
**Automatización relacionada:** `DopoSoccerFacadeTest`, `DopoSoccerV2Test`

**Precondiciones**

- La aplicación puede crear una fachada `DopoSoccer`.
- El catálogo predeterminado contiene Estados Unidos y Camerún.

**Datos**

- Modo: jugador contra jugador.
- Modalidad: goles.
- Países: Estados Unidos y Camerún.
- Límite: 5 goles.

**Pasos**

1. Construir una solicitud de partido con los datos anteriores.
2. Enviarla a `DopoSoccer.startMatch`.
3. Consultar el estado y snapshot de la fachada.

**Resultado esperado**

- El estado de la fachada es `ACTIVE`.
- El snapshot muestra los países seleccionados y sus recursos de bandera.
- El partido usa el límite configurado y acepta actualizaciones de entrada.
- No se exponen entidades mutables del dominio a través del snapshot público.

### PA-02 — Inicio y actualización de partido por tiempo

**Tipo:** Positivo  
**Prioridad:** Alta  
**Automatización relacionada:** `GamePersistenceTest`, `TimeGameSuddenDeathTest`

**Precondiciones**

- La modalidad por tiempo está registrada.
- La solicitud se configura con duración válida en minutos.

**Datos**

- Modalidad: tiempo.
- Duración: 2 minutos.
- Países: Corea del Sur y Nigeria.

**Pasos**

1. Crear la solicitud con `durationSeconds = "2"` como valor del formulario en minutos.
2. Iniciar el partido.
3. Avanzar la simulación 5 segundos.
4. Consultar el snapshot y la configuración del juego.

**Resultado esperado**

- El partido se inicia con 120 segundos reglamentarios.
- Después del avance quedan aproximadamente 115 segundos.
- La descripción visible del modo refleja el tiempo restante.
- Si el tiempo termina empatado, se activa muerte súbita; el partido no se marca finalizado hasta que se determine un ganador.

### PA-03 — Configuración desde presentación hacia una modalidad registrada

**Tipo:** Positivo  
**Prioridad:** Alta  
**Automatización relacionada:** `DopoSoccerFacadeTest`, prueba `presentationMatchSetupStartsTheConfiguredTimeMatchThroughTheFacade`

**Precondiciones**

- Hay una configuración `MatchSetup` válida.
- La fachada tiene registrada la modalidad por tiempo.

**Datos**

- Modo: `PLAYER_VS_PLAYER`.
- Modalidad: `time`.
- Parámetro `durationSeconds`: `"2"`.

**Pasos**

1. Crear y configurar un `MatchSetup`.
2. Convertirlo mediante `MatchRequestMapper.toDomain`.
3. Iniciar el partido con la solicitud resultante.
4. Consultar el estado de la fachada y la descripción del modo.

**Resultado esperado**

- El mapper conserva modo, modalidad, países, perfil de IA y parámetros.
- La fachada inicia un partido activo.
- El snapshot refleja dos minutos de duración (`02:00`).
- La conversión se puede probar sin iniciar el toolkit gráfico de JavaFX.

### PA-04 — Rechazo de parámetros o configuración inválidos

**Tipo:** Negativo  
**Prioridad:** Alta  
**Automatización relacionada:** `ExtensibleArchitectureTest`, pruebas de validación de parámetros y persistencia

**Precondiciones**

- Se registra una modalidad que requiere un parámetro numérico, por ejemplo `rounds` entre 1 y 9.
- La modalidad tiene un repositorio de guardado de prueba que permite comprobar si recibió datos.

**Datos de prueba**

- Solicitud con `rounds = "99"`; y, en una segunda variante, juego cuyo `MatchConfiguration` omite el parámetro requerido o lo guarda con un tipo incompatible.

**Pasos**

1. Intentar crear la modalidad con el valor fuera de rango.
2. Confirmar que no se obtiene un partido válido.
3. Intentar guardar la configuración incompleta o con un tipo incorrecto.
4. Inspeccionar el repositorio de prueba.

**Resultado esperado**

- La creación o el guardado lanza un error de dominio comprensible.
- El repositorio no recibe una instantánea inválida.
- No se sustituye un guardado válido anterior por datos inválidos.

### PA-05 — Rechazo de país o tipo de poder desconocido

**Tipo:** Negativo  
**Prioridad:** Alta  
**Automatización relacionada:** pruebas de `CountryCatalog` y `PowerFactoryRegistry` en `ExtensibleArchitectureTest`

**Precondiciones**

- Se utiliza el catálogo de países y un registro de fábricas de poderes.

**Datos de prueba**

- Solicitud con identificador de país que no existe en el catálogo.
- Configuración de catálogo cuyo `power.type` no tiene fábrica registrada.

**Pasos**

1. Intentar iniciar un partido con el país desconocido.
2. Cargar un catálogo de prueba con un tipo de poder no registrado.

**Resultado esperado**

- La búsqueda del país falla con un error explícito; no se sustituye silenciosamente por otro país.
- La carga del catálogo falla indicando que no hay fábrica para el tipo de poder.
- No se crea un partido parcial ni un catálogo incompleto.

### PA-06 — Poder personalizado no proyectil y persistencia de su estado

**Tipo:** Positivo  
**Prioridad:** Media  
**Automatización relacionada:** `ExtensibleArchitectureTest`, prueba `customCountryCatalogAndNonProjectilePowerStateAreInjectedAndRestored`

**Precondiciones**

- Se crea un `PowerFactoryRegistry` independiente que incluye fábricas predeterminadas y una fábrica personalizada.
- Se carga un `CountryCatalog` independiente con un país que usa el poder nuevo.
- La fábrica personalizada produce un `Power` no proyectil.

**Pasos**

1. Crear un partido usando el catálogo inyectado.
2. Guardar un dato propio del poder en el estado de su jugador.
3. Guardar y restaurar la partida mediante `MatchPersistenceService` usando la misma fábrica/restaurador configurado.
4. Consultar el poder del país y el estado de extensión del jugador restaurado.

**Resultado esperado**

- El país conserva el ID y el poder personalizado.
- El poder no necesita heredar de `ProjectilePower` para registrarse.
- El estado serializable del poder se restaura para el jugador correcto.

### PA-07 — Guardado y restauración completos en versión actual

**Tipo:** Positivo  
**Prioridad:** Alta  
**Automatización relacionada:** `GamePersistenceTest`, prueba `serializesTypedMatchStateAndRebuildsThePhysicsEngine`

**Precondiciones**

- Se utiliza el almacén de partidas con una ruta temporal.
- Hay una partida por goles con un tiro especial activo.

**Datos**

- Estado de jugador con goles, puntos y efecto temporal.
- Balón con posición y velocidad distintas de los valores iniciales.
- Configuración de modalidad con `MatchValue.IntegerNumber`.
- Tiro activo con propietario y efecto aplicado.

**Pasos**

1. Crear y modificar la partida.
2. Guardar el snapshot.
3. Cargarlo mediante `MatchPersistenceService`.
4. Comparar el estado restaurado y la configuración almacenada.

**Resultado esperado**

- El archivo se guarda en versión actual (6).
- Se restauran estadísticas, posiciones, velocidades, efecto y propietario del tiro.
- Los parámetros de modalidad conservan su tipo `MatchValue`.
- Se reconstruye el motor físico y la partida puede continuar actualizándose.

### PA-08 — Migración automática de guardado de versión 5

**Tipo:** Positivo  
**Prioridad:** Alta  
**Automatización relacionada:** `GamePersistenceTest`, prueba `migratesVersionFiveStringConfigurationAndRewritesTheSave`

**Precondiciones**

- Existe un archivo de versión 5 con configuración textual válida.
- El restaurador/fábrica dispone del esquema de la modalidad.

**Datos**

- Modalidad por tiempo.
- Parámetro textual de duración y estado textual de tiempo restante y muerte súbita.

**Pasos**

1. Preparar un archivo de prueba compatible con la estructura antigua y marcado como versión 5.
2. Cargarlo usando el servicio de persistencia.
3. Volver a leer el archivo guardado.

**Resultado esperado**

- La duración textual se valida con el esquema y se convierte al `MatchValue` correspondiente.
- El tiempo restante y el indicador de muerte súbita conservan sus valores.
- La partida se restaura correctamente.
- Solo después de una restauración exitosa, el archivo queda reescrito como versión 6.

### PA-09 — Rechazo de una versión de guardado no soportada

**Tipo:** Negativo  
**Prioridad:** Alta  
**Automatización relacionada:** el almacén valida versiones; añadir una prueba específica de regresión para versión 4 al conjunto automatizado

**Precondiciones**

- Se dispone de un archivo serializado válido en estructura, pero con versión 4 o inferior.
- El archivo no debe modificarse durante la preparación del caso.

**Pasos**

1. Intentar cargar el archivo mediante `MatchSaveStore` o `MatchPersistenceService`.
2. Comprobar el resultado y verificar que no se abrió una partida.

**Resultado esperado**

- La carga se rechaza como incompatible.
- No se intenta restaurar parcialmente el juego ni reescribir el archivo como versión 6.
- El error se propaga al límite de aplicación para que la UI pueda informar al usuario.

**Nota de cobertura:** la suite cubre la migración de versión 5 y el formato actual; una fixture real de versión anterior a 5 debe añadirse para automatizar este caso completamente.

### PA-10 — Finalización, recompensas idempotentes y recuperación de fallo

**Tipo:** Mixto (éxito y fallo recuperable)  
**Prioridad:** Alta  
**Automatización relacionada:** `ExtensibleArchitectureTest`, pruebas de finalización, idempotencia y fallo de persistencia del perfil

**Precondiciones**

- Hay una partida finalizada.
- El servicio de recompensas y el repositorio de perfiles están inyectados.

**Pasos**

1. Completar el partido dos veces usando el servicio de finalización.
2. Verificar los puntos del partido y los perfiles persistidos.
3. Repetir con un repositorio que falle en el primer guardado.
4. Comprobar puntos tras el error, corregir el repositorio y reintentar la finalización.

**Resultado esperado**

- En el flujo exitoso, las recompensas se aplican y persisten una sola vez aunque se invoque la finalización repetidamente.
- Si falla el guardado del perfil, el error no se oculta y los puntos no se consumen como si la operación hubiera terminado.
- Tras corregir el repositorio y reintentar, las recompensas se entregan una sola vez.

## Trazabilidad y límites

- Los casos PA-01 a PA-04, PA-06 a PA-08 y PA-10 se corresponden con pruebas existentes de fachada, modalidad, persistencia y arquitectura extensible.
- PA-05 y PA-09 documentan rechazos esperados que conviene cubrir con pruebas de regresión específicas: identificadores desconocidos y una fixture serializada de versión anterior a 5.
- Estas pruebas no sustituyen una prueba de aceptación visual manual de JavaFX. En particular, el selector de archivos, los diálogos de error, la navegación y el renderizado deben comprobarse en una sesión gráfica.
- Un caso negativo no significa que el producto esté fallando: la aceptación consiste en que el error esperado se detecte, se comunique y no deje estado inconsistente.
