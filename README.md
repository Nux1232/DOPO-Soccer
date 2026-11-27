# Head Soccer

Juego 2D local para dos jugadores, implementado con JavaFX.

## Ejecutar

Requiere JDK 21 o superior con `JAVA_HOME` configurado. Maven se descarga automáticamente mediante el wrapper incluido:

```powershell
.\mvnw.cmd javafx:run
.\mvnw.cmd test
```

La simulación física usa [Dyn4j](https://dyn4j.org/), que Maven descarga automáticamente al abrir o compilar el proyecto. El mundo trabaja en metros con pasos fijos de 1/60 s: los jugadores usan cuerpos tipo cápsula y se mueven con fuerzas e impulsos; el balón rebota mediante materiales físicos, y los goles se detectan con sensores de contacto.

En IntelliJ IDEA, abre `pom.xml` como proyecto Maven y usa **Reload All Maven Projects** para sincronizar las dependencias de JavaFX.


## Controles

- Jugador 1: `A` / `D` mover, `W` saltar, `F` patear, `G` activar poder.
- Jugador 2: `←` / `→` mover, `↑` saltar, `Enter` patear, `Shift` activar poder.

El dominio del partido está en `src/domain`; la aplicación JavaFX y el renderizado están en `src/presentation`.

## Guardado de partidas

Las partidas se guardan como snapshots versionados y serializables (`.ser`), no como objetos del motor Dyn4j. El motor físico se reconstruye al cargar. Desde la pantalla de juego puedes guardar y elegir la ubicación del archivo; los perfiles permanentes de los jugadores se guardan por separado en `~/.dopo-soccer/profiles.properties`.

## Poderes y estado del partido

Los valores base del Anexo 1 son: Corea del Sur (40 de daño, 1,5 s), Camerún (60, 3 s), Nigeria (40, 3 s) y Estados Unidos (30, 2,5 s). El daño final del tiro se multiplica por el nivel de patada del atacante. Camerún reduce al 50 % la velocidad y bloquea el salto; el empuje de Corea del Sur apunta al arco propio del rival. La confusión de Estados Unidos es únicamente visual: muestra ocho balones idénticos sin alterar los controles.

El aura dispara el poder al tocar el balón, incluso sin pulsar la tecla de patada. Los GIF de estado son opcionales: `GameRenderer` dibuja indicadores vectoriales de respaldo y el HUD muestra puntos, estadísticas, aura y estado. El botón **Terminar partido** cierra el encuentro con el resultado del marcador y aplica las recompensas; en modo contra la máquina solo se carga y guarda el perfil del jugador humano.

## Extender países y poderes

`CountryCatalog` carga países y configuración de sus poderes desde `src/resources/countries.properties`. Cada `Country` conserva su identidad visual y contiene un `Power`; el país y el poder son objetos independientes. Para añadir un país con un poder de impacto, agrega su ID a `countries` y define sus propiedades `country.<id>` (incluidos alias, apariencia, `power.type` y parámetros); los recursos de bandera y efecto deben estar en sus carpetas respectivas.

Los tipos de poder se conectan mediante `PowerFactoryRegistry`. Para incorporar un comportamiento nuevo, implementa `Power` o `ProjectilePower`, crea un registro con `PowerFactoryRegistry.withDefaults()`, registra la fábrica y carga un catálogo independiente con `CountryCatalog.loadDefault(registry)`. Inyecta ese catálogo en `new MatchFactory(catalog, physicsProvider)` y comparte la fábrica con `DopoSoccer` y `MatchPersistenceService`. Así el registro personalizado no depende del orden de inicialización del catálogo global. La fábrica recibe un mapa inmutable de las propiedades `country.<id>.power.*` (excepto `type`).

Un poder que quiera asociar un tiro al balón puede implementar `BallShot` y sobrescribir `Power.activateInMatch(...)` para invocar el lanzador proporcionado. El contrato del tiro define velocidades, duración, restitución e impacto; el motor aplica la física común sin comprobar si el poder es `ProjectilePower`. Los poderes heredados pueden seguir implementando `ProjectilePower`, que adapta su activación a este contrato.

Los poderes no proyectiles pueden guardar estado por jugador mediante `Player.setPowerExtensionState` y personalizar `Power.saveState` / `Power.restoreState` cuando requieran otro formato. El formato del snapshot es la versión 6 e incluye esos datos; los guardados anteriores se rechazan sin migración automática.

La presentación de efectos temporales se amplía con `PlayerEffectCatalog`: se pueden registrar rutas y geometría para nuevos identificadores de estado, y una subclase de `DopoSoccerGUI` puede suministrar su catálogo sobrescribiendo `createPlayerEffectCatalog()`. La conversión de snapshots y controles entre dominio y presentación está centralizada en `GameSnapshotMapper`.

La selección de países está encapsulada en `PlayerCountryCard`, mientras `CharacterSelectionPane` coordina la pantalla. `GameRenderer` delega el marcador y las barras de estado en `MatchHudRenderer`, separando la cancha de la interfaz de partido.

Para cambiar la representación del balón de un poder, registra una estrategia en `PowerShotRendererRegistry` y sobrescribe `DopoSoccerGUI.createPowerShotRendererRegistry()`. El renderizador predeterminado sigue funcionando como respaldo para poderes sin estrategia propia.

## Extender modalidades y persistencia

`DopoSoccer` acepta una `MatchFactory` y un `PlayerProfileService` por constructor. Para incorporar una modalidad, crea un identificador con `MatchType.of("mi-modalidad")`, implementa su `Game` y su `MatchConfiguration`, y registra su `MatchCreator` en la fábrica. La fábrica delega el catálogo y la normalización de parámetros a `MatchTypeRegistry`; los tipos de control se describen con `MatchFactory.MatchParameter`: `NUMBER` (rango y multiplicador), `TEXT` (longitud mínima y máxima), `BOOLEAN` y `SELECT` (opciones explícitas). Cada campo declara ID, etiqueta y valor predeterminado. La selección construye los controles desde esos metadatos y envía los valores en `MatchParameters.parameters()`. Cada modalidad conserva sus opciones en `MatchConfiguration.parameters()` y su progreso de ejecución en `MatchConfiguration.state()`, usando valores cerrados y serializables de `MatchValue`: `Text`, `IntegerNumber`, `DecimalNumber` y `Flag`. Persistencia guarda ese contrato sin campos específicos de goles o tiempo. El esquema tipado se valida al crear el partido, al restaurarlo y antes de escribir el guardado; los campos requeridos o valores inválidos no dejan guardados que fallen solo al volver a cargarlos. Para desacoplar la restauración, `MatchPersistenceService` recibe el puerto `MatchRestorer`; `MatchFactory` lo implementa por defecto, pero una aplicación puede inyectar otra estrategia para reconstruir modalidades sin que persistencia conozca la fábrica concreta.

Los parámetros numéricos se normalizan a su unidad configurada antes de entregar la solicitud al creador. Las modalidades personalizadas solo requieren definir y validar su propio esquema; no hay que modificar una estructura común al agregar campos de configuración. La persistencia delega además en `Power.restoreActiveShot(...)` la reconstrucción de un tiro; cada tipo de poder decide si puede restaurar ese estado, sin que el servicio dependa directamente de `ProjectilePower`.

El snapshot de partidas está en la versión 6, que persiste el contrato `MatchConfiguration` de cada modalidad con valores tipados y el estado extensible de poderes. Los guardados anteriores se rechazan, sin migración automática.

Los snapshots de fachada y presentación agrupan la posición y geometría del balón junto con los datos del tiro activo en `BallSnapshot`. Así, añadir propiedades visuales del balón o del tiro no requiere ampliar cada vez el contrato raíz de partida; los accesores planos existentes se conservan para compatibilidad.

`Game` construye jugadores con un `PlayerSide`, que concentra el estado del lado y la orientación inicial. `LPlayer` y `RPlayer` permanecen como adaptadores obsoletos para compatibilidad con código anterior; el código nuevo debe crear `Player` con su lado explícito.

La finalización y entrega idempotente de recompensas se coordina en `MatchCompletionService`; `DopoSoccer` conserva la fachada pública y delega ese flujo en el servicio.

Los servicios de persistencia dependen de `MatchSaveRepository` y `PlayerProfileRepository`, no de sus almacenes de archivo concretos. `MatchSaveStore` sigue siendo el almacenamiento predeterminado de partidas: guarda `MatchSaveData` con `ObjectOutputStream` en un `.ser` versionado y lo valida al cargar. La versión 6 incorpora identificadores extensibles, configuración específica tipada por modalidad y estado adicional de poderes; los archivos de versiones anteriores se rechazan y no se migran automáticamente. Los perfiles conservan el formato `.properties` actual; se pueden sustituir sus almacenes sin modificar la lógica del servicio.
