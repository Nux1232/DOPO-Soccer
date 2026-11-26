# Head Soccer

Juego 2D local para dos jugadores, implementado con JavaFX.

## Ejecutar

Requiere JDK 21 o superior y Maven:

La simulación física usa [Dyn4j](https://dyn4j.org/), que Maven descarga automáticamente al abrir o compilar el proyecto. El mundo trabaja en metros con pasos fijos de 1/60 s: los jugadores usan cuerpos tipo cápsula y se mueven con fuerzas e impulsos; el balón rebota mediante materiales físicos, y los goles se detectan con sensores de contacto.

En IntelliJ IDEA, abre `pom.xml` como proyecto Maven y usa **Reload All Maven Projects** para sincronizar las dependencias de JavaFX.


## Controles

- Jugador 1: `A` / `D` mover, `W` saltar, `F` patear, `G` activar poder.
- Jugador 2: `←` / `→` mover, `↑` saltar, `Enter` patear, `Shift` activar poder.

El dominio del partido está en `src/domain`; la aplicación JavaFX y el renderizado están en `src/presentation`.

## Guardado de partidas

Las partidas se guardan como snapshots versionados y serializables (`.ser`), no como objetos del motor Dyn4j. El motor físico se reconstruye al cargar. Desde la pantalla de juego puedes guardar y elegir la ubicación del archivo; los perfiles permanentes de los jugadores se guardan por separado en `~/.dopo-soccer/profiles.properties`.

## Extender poderes

Cada país configura e implementa su poder en su propia clase (`src/domain/model/entity`). El contrato común está en `Power`; el daño escala con `POWER_DAMAGE_MULTIPLIER` en `Country`, y cada `applyImpact` define la reacción física. Los estados de control se ajustan en `PlayerStatusEffect` y `PlayerInputHandler`.

## Balance de poderes

Cada país configura su poder en su constructor (`src/domain/model/entity`). El daño escala con `POWER_DAMAGE_MULTIPLIER` en `Country`; los demás valores indican duración en segundos, daño base, velocidades del balón en píxeles por segundo y restitución. La reacción al impacto se define en `applyImpact`, que usa `Player.applyKnockback(velocidadX, velocidadY, duración)` para que Dyn4j conserve el impulso aunque el rival intente moverse. Los efectos de control se ajustan en `PlayerStatusEffect` y `PlayerInputHandler`.
