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

`CountryCatalog` carga países y configuración de sus poderes desde `src/resources/countries.properties`. Cada `Country` conserva su identidad visual y contiene un `Power`; el país y el poder son objetos independientes. Para añadir un país con un poder de impacto, agrega su ID a `countries` y define sus propiedades `country.<id>` (incluidos alias, apariencia y parámetros del poder); los recursos de bandera y efecto deben estar en sus carpetas respectivas. Para comportamientos de poder nuevos, implementa `Power` o `ProjectilePower` en Java y conecta su creación al catálogo. Los efectos de control se ajustan en `PlayerStatusEffect`.

## Extender modalidades y persistencia

`DopoSoccer` acepta una `MatchFactory` y un `PlayerProfileService` por constructor. Para incorporar una modalidad, crea un identificador con `MatchType.of("mi-modalidad")`, implementa su `Game` y la configuración correspondiente, y registra su `MatchCreator` en la fábrica. Define sus campos con `MatchFactory.MatchParameter` (ID, etiqueta, valor predeterminado, rango y multiplicador); la selección construye el formulario desde esos metadatos y envía los valores en `MatchParameters.parameters()`. Incluye los valores en `MatchConfiguration.parameters()` para conservarlos al guardar y restaurar el partido. `DopoSoccer.availableMatchTypes()` publica el catálogo y `MatchRequest` acepta IDs y parámetros personalizados. Comparte la misma fábrica con `MatchPersistenceService` para reconstruir modalidades guardadas. Para usar la fábrica personalizada desde JavaFX, extiende `DopoSoccerGUI` y sobrescribe `createDomain()`.

El snapshot de partidas está en la versión 3, que persiste los parámetros configurables de modalidades. Los guardados anteriores se rechazan, sin migración automática.

`Game` construye jugadores con un `PlayerSide`, que concentra el estado del lado y la orientación inicial. `LPlayer` y `RPlayer` permanecen como adaptadores obsoletos para compatibilidad con código anterior; el código nuevo debe crear `Player` con su lado explícito.

La finalización y entrega idempotente de recompensas se coordina en `MatchCompletionService`; `DopoSoccer` conserva la fachada pública y delega ese flujo en el servicio.

Los servicios de persistencia dependen de `MatchSaveRepository` y `PlayerProfileRepository`, no de sus almacenes de archivo concretos. `MatchSaveStore` sigue siendo el almacenamiento predeterminado de partidas: guarda `MatchSaveData` con `ObjectOutputStream` en un `.ser` versionado y lo valida al cargar. La versión 3 del formato incorpora identificadores extensibles y parámetros configurables de modalidad; los archivos de versiones anteriores se rechazan y no se migran automáticamente. Los perfiles conservan el formato `.properties` actual; se pueden sustituir sus almacenes sin modificar la lógica del servicio.
