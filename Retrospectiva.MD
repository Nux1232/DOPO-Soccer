# Retrospectiva — DOPOSoccer 2026-2

**Asignatura:** Desarrollo Orientado por Objetos  
**Versión:** Uno — Jugador vs. jugador  
**Integrantes:** Samuel Infante y Juan Pablo Cuervo

## 1. ¿Cuánto tiempo trabajó cada integrante?

| Integrante | Tiempo |
|---|---:|
| Samuel Infante | 16 horas |
| Juan Pablo Cuervo | 16 horas |
| **Total** | **32 horas** |

## 2. ¿Cuál es el estado actual del laboratorio?

El proyecto está parcialmente completo. Ya se puede jugar un partido básico entre dos jugadores. Todavía faltan varias reglas del enunciado y algunas funciones que se desarrollarán en las siguientes versiones.

Por ahora, el juego cuenta con selección de personajes, movimiento, salto, patada, poderes básicos, balón, marcador y una interfaz gráfica. También se hicieron pruebas para revisar el funcionamiento.

Quedan pendientes, entre otras cosas, las estadísticas según el enunciado, los poderes propios de cada país, el daño y aturdimiento, los partidos por tiempo o por goles, la pausa y la separación completa entre presentación y dominio.

## 3. ¿Cuál práctica de XP fue la más útil?

La práctica más útil fueron las pruebas automatizadas. Nos ayudaron a revisar el comportamiento del código y a encontrar errores. También fue útil revisar el código frente al enunciado para identificar qué faltaba.

## 4. ¿Cuál fue el mayor logro?

El mayor logro fue entender mejor el código existente y reconocer qué partes del proyecto ya funcionan y cuáles deben corregirse. Esto nos permitió organizar el trabajo que sigue.

## 5. ¿Cuál fue el mayor problema técnico y cómo se resolvió?

Tuvimos problemas al configurar las pruebas en Maven, porque JUnit no estaba configurado correctamente y las pruebas no se ejecutaban como esperábamos.

Revisamos la estructura del proyecto y la configuración de Maven, agregamos las dependencias y los plugins necesarios, y organizamos los archivos de pruebas. Después de estos cambios, pudimos ejecutar las pruebas y generar el reporte de cobertura.

También identificamos una dependencia entre el dominio y la presentación que queda pendiente de corregir.

## 6. ¿Qué hicimos bien como equipo y qué podemos mejorar?

### Lo que hicimos bien
- Revisamos el código y lo comparamos con el enunciado.
- Trabajamos en el proyecto y en sus pruebas.
- Identificamos los pendientes antes de agregar más funcionalidades.
- Organizamos el trabajo para continuar en las siguientes versiones.

### Compromisos de mejora
1. Corregir las reglas que todavía no cumplen el enunciado.
2. Agregar o actualizar pruebas cuando se realicen cambios.
3. Hacer commits pequeños y claros.
4. Revisar las dudas del enunciado antes de programar.
5. Mejorar la separación entre dominio y presentación.
6. Asegurarnos de que ambos integrantes podamos explicar el código.

## 7. ¿Qué referencias usamos y cuál fue la más útil?

La referencia principal fue el enunciado del proyecto, porque nos permitió comparar el código con los requisitos. También consultamos documentación de Java, JavaFX, Maven, JUnit, JaCoCo y PIT.

Se utilizó una herramienta de IA como apoyo para comprender código y orientar algunas tareas. El código fue revisado y probado por el equipo.

## Anexo A. Miniciclos realizados

| # | Miniciclo | Resultado |
|---|---|---|
| 1 | Revisión del código | Identificamos qué requisitos se cumplen y cuáles faltan. |
| 2 | Documentación | Se agregó documentación Javadoc al código. |
| 3 | Correcciones sencillas | Se ajustaron algunos detalles de la interfaz y del proyecto. |
| 4 | Pruebas unitarias | Se prepararon pruebas para revisar el comportamiento del dominio. |
| 5 | Configuración de Maven | Se configuraron las herramientas para ejecutar pruebas y revisar cobertura. |
| 6 | Diagramas de secuencia | Se propusieron diagramas para explicar algunos comportamientos del juego. |
| 7 | Retrospectiva | Se revisó el trabajo realizado y se identificaron mejoras. |

## Próximos pasos

- Ajustar las estadísticas y el sistema de daño.
- Implementar los poderes propios de cada personaje.
- Agregar los tipos de partido y sus condiciones de victoria.
- Mejorar la separación entre dominio y presentación.
- Implementar las funciones previstas para las siguientes versiones.


# Segunda Entrega:
# RETROSPECTIVA – DOPO-SOCCER

## 1. ¿Cuál fue el tiempo total invertido en el proyecto por cada uno de ustedes? (Horas)

El desarrollo del proyecto se realizó en diferentes sesiones de trabajo, distribuidas entre el diseño, la programación, la integración de funcionalidades, las pruebas y la documentación.

El tiempo invertido por cada integrante debe completarse con base en las horas reales dedicadas al proyecto:

| Integrante | Tiempo invertido |
|---|---:|
| Samuel Infante | 20 horas |
| Juan Pablo Cuervo | 20 horas |

El trabajo no se concentró únicamente en la programación. También se invirtió tiempo en la organización de la arquitectura, la elaboración y actualización de diagramas UML (Aunque falta por actualizar), la búsqueda de recursos gráficos, la integración de librerías externas y la solución de errores encontrados durante las pruebas.

---

## 2. ¿Cuál es el estado actual del proyecto? ¿Por qué?

El proyecto se encuentra en un estado funcional avanzado, aunque todavía tiene algunos aspectos pendientes de integración, documentación y validación.

Actualmente, DOPO-SOCCER cuenta con una aplicación desarrollada en Java 21 y JavaFX. El proyecto permite seleccionar modos de juego, escoger los países de los jugadores y configurar las condiciones del partido.

El estado actual se puede explicar mediante los siguientes ciclos de desarrollo:

### Ciclo 1. Reestructuración de la física, arquitectura y reglas del dominio

En este ciclo se trabajó principalmente en mejorar la implementación interna del videojuego y separar las responsabilidades de sus componentes.

Las actividades realizadas fueron:

- Incorporación de la librería externa Dyn4j para gestionar la simulación física.
- Sustitución de parte de la física personalizada por una implementación apoyada en Dyn4j.
- Reestructuración del motor de física para mejorar el manejo de las colisiones y el comportamiento del balón y los jugadores.
- Creación de la interfaz `PhysicsSimulation`, con el objetivo de desacoplar la lógica del juego de una implementación específica de la física.
- Modificación de `PhysicsEngine` para implementar `PhysicsSimulation`.
- Incorporación de la inyección de dependencias del motor de física en la clase `Game`.
- Creación y organización de la clase `PlayerInput` para representar las entradas de los jugadores.
- Implementación y revisión de las reglas de negocio relacionadas con el movimiento, daño, poderes y estadísticas.
- Incorporación de la clase `DopoSoccerException` para manejar errores propios del dominio.
- Definición de mensajes específicos para errores de configuración, tiempo negativo, daño negativo, puntos insuficientes y estadísticas en su máximo valor.
- Incorporación de `DopoSoccerLog` para registrar las excepciones.
- Revisión de las responsabilidades de las clases del dominio y de presentación.

Este ciclo permitió mejorar la separación de responsabilidades y preparar el proyecto para incorporar diferentes implementaciones del comportamiento de un partido.

### Ciclo 2. Implementación de poderes y efectos visuales

En este ciclo se trabajó en los poderes de los jugadores y en la presentación visual de sus efectos.

Las actividades realizadas fueron:

- Implementación del sistema de carga del poder.
- Incorporación del poder de aura (`AURA`).
- Implementación de la activación y consumo del poder.
- Implementación del efecto de aturdimiento (`STUN`).
- Incorporación de estados temporales relacionados con el aturdimiento.
- Desarrollo de la clase `PlayerEffectsLayer` para separar los efectos visuales de la representación general del partido.
- Carga de archivos GIF para representar visualmente el aura y el stun.
- Posicionamiento de los efectos visuales sobre los jugadores.
- Ajustes de tamaño, escala y desplazamiento de los GIF.
- Integración de los efectos con la pantalla del partido.
- Ajustes visuales del HUD y del marcador.

Durante este ciclo se identificaron algunos problemas de posicionamiento y centrado del GIF del aura. Se realizaron diferentes ajustes para mejorar su ubicación y tamaño. El efecto de stun quedó visualmente integrado, mientras que el aura requirió más correcciones.

### Ciclo 3. Implementación de modalidades de partido

En este ciclo se incorporó la posibilidad de jugar bajo diferentes condiciones de victoria, utilizando herencia y polimorfismo.

Se realizaron las siguientes actividades:

- Creación de la enumeración `MatchType`, con las modalidades `GOALS` y `TIME`.
- Actualización de `GameMatchConfig` para almacenar la modalidad seleccionada.
- Incorporación de la configuración del límite de goles.
- Incorporación de la configuración de duración del partido.
- Establecimiento de valores predeterminados de 10 goles y 4 minutos.
- Creación de `GoalGame`, que hereda de `Game` y representa los partidos que terminan cuando un jugador alcanza el límite de goles.
- Creación de `TimeGame`, que hereda de `Game` y representa los partidos que terminan cuando se agota el tiempo.
- Implementación del control del tiempo restante.
- Incorporación de la comparación de marcadores para determinar el ganador.
- Implementación del estado de finalización del partido.
- Actualización de `CharacterSelectionPane` para permitir seleccionar la modalidad y configurar el límite correspondiente.
- Cambio de los controles de configuración para permitir introducir valores numéricos.
- Actualización de la presentación de la modalidad seleccionada.
- Integración de las nuevas modalidades con el flujo de inicio del partido.
- Ajustes de la interfaz para organizar los controles, tarjetas de jugadores, instrucciones y botones.

La implementación de las modalidades permitió ampliar el comportamiento del videojuego sin tener que duplicar toda la lógica común del partido.

### Ciclo 4. Integración visual, documentación y revisión de arquitectura

En este ciclo se trabajó en la consolidación de la interfaz y en la revisión de la documentación técnica.

Se realizaron las siguientes actividades:

- Ajustes de dimensiones y alineación de los componentes de `CharacterSelectionPane`.
- Reorganización del panel de selección de modalidad.
- Corrección de tamaños de los controles numéricos.
- Ajustes de los títulos y etiquetas de la pantalla.
- Mejoras en la distribución de las tarjetas de selección de países.
- Revisión de la visualización en ventanas normales y en pantalla completa.
- Incorporación y ajuste de recursos gráficos para los modos de juego.
- Revisión de los paquetes y responsabilidades de las clases.
- Actualización parcial de los diagramas de clases de Domain y Presentation.
- Revisión de las relaciones de herencia entre `Game`, `GoalGame` y `TimeGame`.
- Revisión de la interfaz `PhysicsSimulation` y su implementación por `PhysicsEngine`.
- Revisión de las asociaciones y dependencias entre las clases del dominio y de presentación.

También se identificó una necesidad importante de mejora arquitectónica: la capa Presentation conoce directamente diferentes clases internas de Domain. Se planteó revisar el uso de una fachada de dominio para centralizar la comunicación entre ambas capas. Esta refactorización todavía debe implementarse y validarse.

### Pendientes actuales

Aunque se cuenta con una versión funcional avanzada, todavía se deben realizar las siguientes actividades:

- Revisar completamente el diagrama de clases para que represente fielmente el código actual.
- Actualizar correctamente los diagramas de secuencia, teniendo en cuenta los nuevos flujos de selección, creación e inicio de partidos.
- Revisar las relaciones, multiplicidades, atributos y operaciones de los diagramas UML.
- Evaluar e implementar correctamente la fachada de Domain para evitar que Presentation dependa directamente de las entidades internas.
- Validar completamente el comportamiento de los modos por goles y por tiempo.
- Revisar el comportamiento visual del aura y sus posiciones.
- Realizar pruebas adicionales sobre las reglas de negocio y las excepciones.
- Revisar la documentación técnica para que coincida con la implementación final.
- Ejecutar una validación general del proyecto después de integrar todos los cambios.

Por lo anterior, consideramos que el proyecto tiene un avance importante y una base funcional sólida, pero no se encuentra completamente terminado porque todavía existen actividades de documentación, integración y validación pendientes.

---

## 3. Considerando las prácticas XP del Proyecto, ¿cuál fue la más útil? ¿Por qué?

Consideramos que una de las prácticas más útiles fue el desarrollo incremental acompañado de integración frecuente.

El proyecto se construyó mediante ciclos en los que primero se implementaron las funcionalidades básicas y posteriormente se fueron incorporando características más complejas, como la física con Dyn4j, los poderes, los efectos visuales y las modalidades de partido.

Esta forma de trabajo permitió identificar errores sin tener que esperar hasta el final del desarrollo. Por ejemplo, al incorporar el motor de física fue posible detectar la necesidad de desacoplar su implementación mediante `PhysicsSimulation`. De igual manera, al implementar las modalidades de juego se identificó que era conveniente utilizar herencia para representar las diferentes reglas de finalización.

También fue importante mantener una versión ejecutable del proyecto durante el desarrollo, realizando pruebas manuales después de los cambios más importantes.

Otras prácticas relacionadas con XP que aportaron al trabajo fueron:

- **Diseño simple:** se intentó mantener una estructura organizada y evitar duplicar la lógica común de los partidos.
- **Integración continua:** se realizaron integraciones de los avances mediante Git para reunir el trabajo desarrollado.
- **Pruebas frecuentes:** se ejecutó el videojuego después de incorporar funcionalidades para comprobar su comportamiento.
- **Refactorización:** se reorganizaron componentes, especialmente el motor de física y las responsabilidades de las clases.
- **Propiedad colectiva del código:** se integraron avances de diferentes integrantes y se realizaron ajustes sobre componentes existentes.

No todas las prácticas de XP se aplicaron de manera estricta o sistemática, pero las anteriores fueron las que tuvieron mayor impacto en el desarrollo.

---

## 4. ¿Cuál consideran que fue el mayor logro? ¿Por qué?

Consideramos que el mayor logro fue conseguir una versión funcional del videojuego que integra diferentes componentes y mecánicas, y que además permite ampliar las reglas del partido mediante herencia.

Inicialmente se contaba con una estructura básica de un videojuego de fútbol. Durante el desarrollo se logró incorporar una simulación física apoyada en Dyn4j, poderes para los jugadores, efectos visuales, configuración de personajes y diferentes modalidades de partido.

La incorporación de `GoalGame` y `TimeGame` representa un avance importante porque permite especializar el comportamiento de la clase base `Game` sin duplicar todas sus funcionalidades.

También destacamos la implementación de `PhysicsSimulation`, ya que permite desacoplar el comportamiento general del partido de la implementación concreta del motor de física.

Este logro es importante porque no solamente se consiguió agregar funcionalidades visibles para el usuario, sino que también se avanzó en la organización interna del proyecto y en su capacidad de extensión.

---

## 5. ¿Cuál consideran que fue el mayor problema técnico? ¿Qué hicieron para resolverlo?

El mayor problema técnico fue mantener una arquitectura organizada mientras se integraban funcionalidades que involucraban diferentes responsabilidades, especialmente la física, las reglas del dominio y la presentación gráfica.

Uno de los principales inconvenientes apareció al integrar el motor de física. La implementación inicial tenía responsabilidades relacionadas directamente con el comportamiento del partido. Para mejorar esta situación se incorporó Dyn4j y se creó la interfaz `PhysicsSimulation`, permitiendo desacoplar la lógica del juego de la implementación específica de la física.

Otro problema importante fue la integración de los efectos visuales. Los GIF del aura y del stun debían posicionarse correctamente sobre los jugadores, teniendo en cuenta la escala del campo y los desplazamientos de la cámara o del área de juego. Se realizaron diferentes ajustes sobre los tamaños, offsets y posiciones de los elementos visuales.

También surgieron dificultades en la organización de `CharacterSelectionPane`. Al incorporar la selección de modalidades y los controles numéricos, algunos elementos quedaron desalineados, se superpusieron o no se adaptaban correctamente al tamaño de la ventana. Para solucionarlo se reorganizaron los contenedores, se ajustaron sus dimensiones y se modificaron las políticas de distribución y desplazamiento.

Finalmente, se identificó un problema arquitectónico relacionado con las dependencias entre Presentation y Domain. Actualmente, algunas clases de presentación conocen directamente entidades y clases internas del dominio. Se analizó la necesidad de implementar una fachada para centralizar esta comunicación, pero esta actividad todavía está pendiente.

La solución de estos problemas se realizó mediante cambios incrementales, pruebas manuales, revisión del código y apoyo en la documentación de las librerías utilizadas.

---

## 6. ¿Qué hicieron bien como equipo? ¿Qué se comprometen a hacer para mejorar los resultados?

Como equipo consideramos que uno de nuestros principales aciertos fue mantener el desarrollo en movimiento e integrar funcionalidades progresivamente.

Se logró distribuir el trabajo entre diferentes componentes del proyecto, permitiendo avanzar en paralelo en aspectos como la lógica del juego, la interfaz gráfica, los efectos visuales y la documentación.

También fue positivo mantener el código versionado mediante Git, ya que permitió integrar avances, revisar cambios y conservar el historial de desarrollo.

Otro aspecto positivo fue la disposición para revisar las implementaciones existentes cuando se identificaban problemas, en lugar de limitarse a agregar nuevas funcionalidades sobre una estructura que podía necesitar mejoras.

Sin embargo, durante el desarrollo también se presentaron dificultades de coordinación y algunos cambios de arquitectura se realizaron cuando ya existían componentes que dependían de las implementaciones anteriores. Esto generó ajustes adicionales y pérdida de tiempo.

Para mejorar los resultados nos comprometemos a:

- Definir con mayor claridad las responsabilidades de cada integrante.
- Establecer acuerdos de arquitectura antes de implementar funcionalidades que afecten varios paquetes.
- Integrar los avances con mayor frecuencia y validar los cambios antes de mezclarlos con la rama principal.
- Realizar pruebas después de cada modificación importante.
- Mantener actualizados los diagramas UML durante el desarrollo y no únicamente al final.
- Documentar las decisiones técnicas que afecten la estructura del proyecto.
- Revisar las dependencias entre capas antes de incorporar nuevas clases.
- Distribuir mejor el tiempo entre programación, pruebas y documentación.
- Evitar realizar cambios estructurales grandes cerca de la fecha de entrega.

---

## 7. ¿Qué referencias usaron? ¿Cuál fue la más útil? Incluyan citas con estándares adecuados.

Durante el desarrollo se utilizaron referencias técnicas relacionadas con Java, JavaFX, Dyn4j, pruebas de software, diseño orientado a objetos y programación extrema.

Las referencias principales fueron:

1. **Oracle. (s. f.). _Java Documentation_.**  
   https://docs.oracle.com/en/java/

   Se utilizó como referencia para el lenguaje Java, sus clases, interfaces, excepciones, herencia, enumeraciones y características de programación orientada a objetos.

2. **OpenJFX. (s. f.). _JavaFX Documentation_.**  
   https://openjfx.io/

   Fue útil para la construcción de las interfaces gráficas, el manejo de componentes, escenas, eventos, animaciones y elementos visuales del videojuego.

3. **Dyn4j. (s. f.). _dyn4j Documentation_.**  
   https://dyn4j.org/

   Se consultó para la incorporación de la librería de simulación física, el manejo de cuerpos, colisiones y comportamiento físico de los objetos.

4. **JUnit. (s. f.). _JUnit 5 User Guide_.**  
   https://junit.org/junit5/docs/current/user-guide/

   Se utilizó como referencia para la estructura y ejecución de pruebas automatizadas en Java.

Consideramos que la documentación oficial de Java y JavaFX fue especialmente útil durante la implementación, mientras que la documentación de Dyn4j fue importante para resolver los aspectos relacionados con la simulación física.

La especificación de UML también fue relevante porque permitió representar los cambios realizados en la arquitectura y detectar diferencias entre los diagramas y el código implementado.

---

## Conclusión

El desarrollo de DOPO-SOCCER permitió construir un videojuego funcional y aplicar diferentes conceptos de programación orientada a objetos, como encapsulamiento, herencia, polimorfismo, interfaces, excepciones y separación de responsabilidades.

A través de los ciclos de trabajo se pasó de una versión básica del videojuego a una implementación con simulación física, poderes, efectos visuales y modalidades configurables de partido.

Uno de los aprendizajes más importantes fue comprender que implementar una funcionalidad no consiste únicamente en conseguir que funcione visualmente, sino también en integrarla correctamente con las responsabilidades y dependencias del resto del sistema.

Finalmente, reconocemos que todavía falta realizar una revisión completa del diagrama de clases y actualizar correctamente los diagramas de secuencia para que representen los flujos reales de la implementación. También queda pendiente terminar de evaluar y corregir las dependencias entre Presentation y Domain.

A pesar de estos pendientes, el proyecto cuenta con una base funcional y una estructura sobre la cual se pueden seguir incorporando mejoras sin tener que reconstruir el videojuego desde cero.
