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

## Extender países y poderes

`CountryCatalog` es la fuente única de países disponibles. Cada `Country` conserva su identidad visual y contiene un `Power`; el país y el poder son objetos independientes, por lo que varios países pueden reutilizar un poder. Para añadir un país, agrega su configuración al catálogo. Para añadir un comportamiento de poder nuevo, implementa `Power`; `ImpactPower` cubre proyectiles con daño, estado, rebote y retroceso configurables. Los efectos de control se ajustan en `PlayerStatusEffect`.
