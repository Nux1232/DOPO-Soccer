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
