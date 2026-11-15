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
