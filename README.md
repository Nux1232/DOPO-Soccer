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

`CountryCatalog` es la fuente única de países disponibles. Cada `Country` conserva su identidad visual y contiene un `Power`; el país y el poder son objetos independientes, por lo que varios países pueden reutilizar un poder. Para añadir un país, agrega su configuración al catálogo. Para añadir un comportamiento de poder nuevo, implementa `Power`; `ImpactPower` cubre proyectiles con daño, estado, rebote y retroceso configurables. Los efectos de control se ajustan en `PlayerStatusEffect`.

## Extender modalidades y persistencia

`DopoSoccer` acepta una `MatchFactory` y un `PlayerProfileService` por constructor. La fábrica permite registrar un `MatchCreator` para cada `MatchType`; al incorporar otra modalidad también hay que agregar su tipo al contrato público y a `MatchType`, implementar su `Game`/`MatchConfiguration` y registrar cómo construirla. La misma fábrica debe compartirse con `MatchPersistenceService` para que una partida guardada pueda reconstruir esa modalidad.

Los servicios de persistencia dependen de `MatchSaveRepository` y `PlayerProfileRepository`, no de sus almacenes de archivo concretos. `MatchSaveStore` sigue siendo el almacenamiento predeterminado de partidas: guarda `MatchSaveData` con `ObjectOutputStream` en un `.ser` versionado y lo valida al cargar. Los perfiles conservan el formato `.properties` actual; se pueden sustituir sus almacenes sin modificar la lógica del servicio.
