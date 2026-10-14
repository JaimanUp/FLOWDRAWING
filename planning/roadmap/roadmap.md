# Roadmap

Fecha de revisión: 2026-10-13. Las etapas E0 a E6 reordenan las fases de RM según dependencias. Cada etapa tiene criterios de salida. Ninguna etapa empieza sin cerrar la anterior, salvo que la anterior tenga pendientes marcados como Backlog.

## Estado actual

- Terminado: arquitectura de 3 niveles del panel (Fase 1 de UI, D-012). Canvas con zoom, pan y capas. Pincel y borrador base. Bots base. Menú.
- En curso: validación visual de exportación.
- Parcialmente completado: exportación SVG/DXF/PNG/JPG y optimización opcional de trazos.
- Sin empezar: persistencia.

## Principios de orden

1. Cerrar lo que existe antes de añadir más. Cada herramienta visible debe funcionar o estar marcada `[planned]`.
2. Respetar dependencias: campo antes que bots, bots antes que su panel, trazados antes que exportación.
3. Exportación básica (SVG) antes que optimización. Sin trazos, la optimización no tiene qué optimizar.
4. Persistencia y rendimiento al final, con aprobación.

## Etapas

### E0. Base verificada

Objetivo: el proyecto compila, se ejecuta y la documentación no se contradice.

- Ejecutar `.\gradlew.bat build` y `.\gradlew.bat run`. Sin errores ni excepciones.
- Alinear `Config.CANVAS_WIDTH` y `CANVAS_HEIGHT` a 2000 y hacer que `FlowDrawing` las lea (D-002).
- Propuesta: añadir `options.release = 11` a `build.gradle` para validar Java 11 de verdad. `sourceCompatibility` no limita las APIs usadas.
- Sincronizar RM y SR con D-001, D-005, D-007, D-011 y D-012.
- Confirmar que la Fase 1 del panel está en Git. RM recomienda el commit "Phase 1: Tool-Centric UI Architecture".

Requisitos: NFR-01 a NFR-06, NFR-25, NFR-27, FR-CAN-01 a FR-CAN-07, FR-UI-01.

Criterio de salida: build correcto, `run` sin excepciones en consola, lienzo de 2000 x 2000 leído desde `Config`.

### E1. Panel de herramientas completo

Objetivo: cada herramienta de BKGD y VECFD tiene sus controles y cada callback está cableado o marcado.

- BKGD: selector de color en popup y transparencia de capa (OD-01).
- VECFD: Paint y Erase comparten ajustes. Field Actions ofrece Normalize y Randomize; Reset está en la fila general.
- Auditoría de `ToolListener`: cada método cableado o marcado con `setStatus("[planned] ...")`.
- Confirmaciones según OD-02.
- Accesibilidad: alturas de 44 px, tooltips, navegación por teclado.
- Revisar `FlowGenerativeEngine_Documentation.pdf`.
- Decidir OD-03 y OD-09 si aplica.

Requisitos: FR-BKG-01 a FR-BKG-05, FR-VEC-08, FR-VEC-09, FR-UI-02, FR-UI-03, FR-UI-06 a FR-UI-10.

Criterio de salida: en [../requirements/ui-spec.md](../requirements/ui-spec.md), las secciones BKGD y VECFD no tienen estados `Pendiente` ni `Verificar`.

### E2. Motor de campo y pincel

Objetivo: el campo es correcto, se visualiza bien y el pincel cumple la especificación.

- Normalización efectiva con prueba basada en `normalization_log.txt` (D-003).
- Muestreo bilineal del campo.
- Controles en VECFD para tamaño de rejilla, magnitud máxima y `fieldMagnitudeScale`.
- Mapa de flujo real: tono = dirección, brillo = magnitud. Decidir OD-03.
- Verificar espaciado tipo Photoshop y las tres curvas de falloff.

Requisitos: FR-VEC-01 a FR-VEC-07, FR-PIN-01 a FR-PIN-05.

Criterio de salida: pruebas de falloff y normalización pasan (ver [../testing/test-strategy.md](../testing/test-strategy.md)). El campo se edita y se visualiza en flechas y en mapa de flujo.

### E3. Simulación de bots

Objetivo: los bots se mueven de forma estable según el campo, el radar, la vida y la deriva.

- Radar con 16 muestras y dependencias `Linear`, `Gaussian` y `Hard Edge` (OD-13).
- Pesos del balance de fuerzas en `Config` (FR-BOT-06).
- Modos de zozobra `Unique`, `Collective` y `Canvas` (FR-BOT-07).
- Modos de zozobra `Unique`, `Collective` y `Canvas` (FR-BOT-07, implementados según D-016).
- Vida con decay por luminancia del fondo (FR-BOT-08, FR-BKG-06).
- Trazado compacto con umbral de 1 a 2 px (FR-BOT-12).
- Suavizado adaptativo y control de estabilidad (FR-BOT-13, FR-BOT-14).
- Iniciar, pausar y reiniciar, con contador de bots correcto (FR-BOT-10).

Requisitos: FR-BOT-01 a FR-BOT-14, NFR-10, NFR-13.

Criterio de salida (propuesta): 100 bots durante 5 minutos sin excepciones y sin crecimiento sostenido de memoria. No hay `ConcurrentModificationException`.

### E4. Panel BOTFD y render de bots

Objetivo: todos los controles de bots están cableados y los bots se dibujan con sus formas y colores.

- Place, Random y Auto con los grupos de ajustes de [../requirements/ui-spec.md](../requirements/ui-spec.md).
- La fila general de BOTFD ofrece acciones separadas para borrar bots y trazados.
- Representación punto, círculo o triángulo (FR-BOT-15).
- Modos de color y fade (FR-BOT-16, FR-BOT-17).
- Resolver OD-04, OD-12, OD-13 y OD-14.

Requisitos: FR-BOT-15 a FR-BOT-17, FR-UI-04.

Criterio de salida: en `ui-spec.md`, BOTFD no tiene estados `Pendiente` ni `Parcial`, salvo la herramienta Export (E5).

### E5. Exportación

Objetivo: exportar los trazados a formatos de salida, con optimización opcional.

Orden de trabajo:

1. SVG/DXF y PNG/JPG (FR-EXP-01 a FR-EXP-03): implementados y cubiertos por checks de salida.
2. Herramienta Export en BOTFD (FR-UI-05): implementada.
3. Douglas-Peucker, vecino más cercano y protección geométrica (FR-EXP-04 a FR-EXP-06): implementados y cubiertos por checks.
4. Abrir y revisar archivos en visores externos, incluida la forma visual tras optimizar.

Criterio de salida: los cuatro formatos abren correctamente en aplicaciones externas; la comparación visual confirma que la optimización conserva la forma.

### E6. Persistencia y rendimiento

Objetivo: guardar y abrir proyectos y alcanzar los objetivos de rendimiento.

Requiere aprobación del usuario (D-004).

- Guardar y abrir en JSON, conectado a las opciones de menú de File (FR-PER-02).
- Perfilar con 500 bots en 2000 x 2000 y después con 4000 x 4000 y rejilla de 10 px.
- Objetivo: 20 a 40 FPS (NFR-09).

Criterio de salida: un proyecto guardado y reabierto reproduce el mismo estado. Las métricas de rendimiento quedan registradas.

### Backlog (sin etapa)

- FR-UI-11: Undo, Redo y Preferences (OD-07).
- FR-UI-12: ventana de atajos (OD-05).
- OD-08: presets de herramientas.
- FR-PER-03: semilla reproducible.

## Trazabilidad con RM

| Fase de RM | Tema | Etapa |
|---|---|---|
| 0 | Configuración del proyecto | E0 |
| 1 | Canvas (CanvasManager, CameraController, LayerRenderer) | E0 (implementado) |
| 2 | Motor de campo | E2 |
| 3 | Visualización del campo | E2 |
| 4 | Sistema de pincel | E1 (panel) y E2 (motor) |
| 5 | Simulación de bots | E3 |
| 6 | Muestreo del radar | E3 |
| 7 | Streaming de trazados | E3 |
| 8 | Controlador de suavizado | E3 |
| 9 | Supervisor de estabilidad | E3 |
| 10 | Motor de render | E4 |
| 11 | Sistema de color | E4 |
| 12 | Exportación | E5 |
| 13 | Optimización de trazados | E5 |
| 14 | Preservación de topología | E5 |
| 15 | Guardado de proyecto | E6 |
| 16 | Rendimiento | E6 |

## Flujo por etapa

1. Crear rama `phase-N-<nombre>` desde `master`.
2. Implementar solo los requisitos de la etapa.
3. Ejecutar `.\gradlew.bat build` y `.\gradlew.bat run`, y completar el checklist de [../testing/test-strategy.md](../testing/test-strategy.md).
4. Actualizar estados en `requirements/functional.md`, `requirements/ui-spec.md` y en este archivo.
5. Commit `Phase N: <descripción>`.
6. Abrir PR al completar la etapa.
