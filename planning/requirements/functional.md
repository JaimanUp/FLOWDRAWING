# Requisitos funcionales

Fecha de revisión: 2026-10-13. Etapas según [roadmap/roadmap.md](../roadmap/roadmap.md). Fuentes según la tabla de [../README.md](../README.md).

## 1. Lienzo

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-CAN-01 | Zoom con la rueda del ratón. | E0 | Implementado | SR 4.1 |
| FR-CAN-02 | Pan arrastrando con el ratón. | E0 | Implementado | SR 4.1 |
| FR-CAN-03 | Reset de vista con `r` y ajuste inicial al arrancar (`resetCanvasToFit`). | E0 | Implementado | SR 4.1, RS 4.1 |
| FR-CAN-04 | Zoom y pan solo afectan a la visualización, nunca a coordenadas de simulación. | E0 | Implementado y probado (conversión de pantalla a mundo para entrada y zoom anclado al cursor) | SR 4.1, RS 8 |
| FR-CAN-05 | Orden de render: fondo, campo, trazos del usuario, trazos y visualización de bots. | E0 | Implementado | SR 4.1, RM Fase 1 |
| FR-CAN-06 | Atajos: Espacio + `+`/`-` para zoom y Espacio + flechas para pan. | E0 | Implementado | RM (2026-04-03) |
| FR-CAN-07 | Tamaño del lienzo 2000 x 2000 por defecto, definido solo en `Config.java`. | E0 | Implementado y probado | RS 11 #2, D-002 |

## 2. Fondo (BKGD)

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-BKG-01 | Mostrar u ocultar la capa sin cambiar la herramienta activa. | E1 | Implementado | GUI 4, RS 8 |
| FR-BKG-02 | Cargar imagen de referencia (PNG, JPG, JPEG, BMP, GIF) con `JFileChooser`. | E1 | Implementado | SR 4.2, RS 4.2 |
| FR-BKG-03 | Quitar la imagen de fondo. | E1 | Implementado | RS 4.2 |
| FR-BKG-04 | Selector de color de fondo (popup). | E1 | Implementado | SR 4.2, TUI |
| FR-BKG-05 | Transparencia de la capa BKGD. | E1 | Parcial (display only; alcance por herramienta/capa sigue pendiente de OD-01) | RS 3, OD-01, D-018 |
| FR-BKG-06 | La luminancia del fondo alimenta la vida de los bots. | E3 | Implementado y probado con color, imagen, limpieza de imagen y decay claro/oscuro | SR 4.2, RM |

## 3. Campo vectorial (VECFD)

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-VEC-01 | Rejilla regular con celda de 10 px. Cada celda guarda `(vx, vy)`. | E2 | Implementado | SR 5.1-5.2 |
| FR-VEC-02 | Magnitud máxima por vector (`maxVectorMagnitude` = 5.0). | E2 | Implementado | SR 5.3, RS 4.3 |
| FR-VEC-03 | Escala global `fieldMagnitudeScale` con control en la UI. | E2 | Implementado (0-200%; afecta muestreo de bots y visualización sin alterar los vectores almacenados) | SR 5.3, RM |
| FR-VEC-04 | Si el 30 % o más de los vectores alcanza el 95 % de la magnitud máxima, escalar proporcionalmente todo el campo hasta que su máximo quede en el 70 % del límite. | E2 | Implementado; validado con umbral, escala proporcional y estabilidad | SR 5.4, RS 8 #1, D-003 |
| FR-VEC-05 | Muestreo bilineal del campo, con límites clamped al borde de la rejilla. | E2 | Implementado; validado con promedio, borde y copia defensiva | RM Fase 5, RS 3 |
| FR-VEC-06 | Visualización en flechas. | E2 | Implementado | SR 4.3, RS 3 |
| FR-VEC-07 | Mapa de flujo: tono = dirección, brillo = magnitud; selector en View > Vector Field Display. | E2 | Implementado | SR 4.3, RS 3, D-015 |
| FR-VEC-08 | Visibilidad de la capa independiente de la herramienta activa. | E1 | Implementado | SR 4.3, GUI 5 |
| FR-VEC-09 | Field Actions contiene Normalize y Randomize; Reset Vector Field es una acción general de capa. Todas afectan la rejilla completa y no activan entrada de pincel. | E1 | Implementado y probado; Paint y Erase son las únicas herramientas de pincel | SR 4.3, TUI |

## 4. Pincel (Paint y Erase)

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-PIN-01 | Paint: `vector += Δmouse * falloff(d)` para los vectores dentro del radio. | E2 | Implementado | SR 6, RM Fase 4 |
| FR-PIN-02 | Erase: `vector -= Δmouse * falloff(d)`. Los vectores tienden a cero. | E2 | Implementado | SR 7, RM (2026-04-03) |
| FR-PIN-03 | Parámetros: tamaño (5 a 200 px), dureza, fuerza y espaciado. | E2 | Implementado | SR 6.1, RM (2026-04-03) |
| FR-PIN-04 | Falloff `Linear`, `Gaussian` y `Hard Edge` (`FalloffType`). | E2 | Implementado | SR 6.1, RM Fase 4 |
| FR-PIN-05 | El espaciado coloca estampas uniformes a lo largo del cursor y acumula el movimiento entre eventos. | E2 | Implementado y probado | SR 6.1, RM Fase 4 |
| FR-PIN-06 | Herramientas Pencil, Brush y Marker del roadmap original. | - | Eliminado | D-007 |

## 5. Bots (BOTFD)

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-BOT-01 | Atributos: posición, velocidad, vida (`life` / `maxLife`), radar, deriva y trazado (`path`). | E3 | Implementado | SR 4.5, RM Fase 5 |
| FR-BOT-02 | Inicialización aleatoria: posición, dirección y vida máxima. | E3 | Implementado | SR 8.1 |
| FR-BOT-03 | Movimiento por iteración: `fieldForce + driftVector`, normalizar y multiplicar por la velocidad base. | E3 | Implementado (base) | SR 8.2, RM Fase 5 |
| FR-BOT-04 | Radar con 16 muestras radiales por defecto, configurable de 4 a 32. | E3 | Implementado y probado | RM Fase 6, RM (2026-04-09) |
| FR-BOT-05 | Dependencia de la influencia en el radar: `Linear`, `Gaussian` o `Hard Edge`. | E3 | Implementado | SR 8.4, RM |
| FR-BOT-06 | Balance de fuerzas (campo, repulsión, deriva) ajustable. | E3 | Implementado en Auto | SR 4.5, RM (2026-04-09) |
| FR-BOT-07 | Zozobra: random walk continuo con modos `Unique`, `Collective` o `Canvas`. | E3 | Implementado y probado según D-016 | SR 8.5, GUI 6 |
| FR-BOT-08 | Vida: pierde más sobre píxeles claros y menos sobre oscuros (decay por luminancia activable). | E3 | Implementado y probado | SR 9, RM |
| FR-BOT-09 | Al llegar a vida 0, el bot reaparece en posición aleatoria conservando sus trazos anteriores. | E3 | Implementado y probado | SR 9 |
| FR-BOT-10 | Control de simulación: iniciar, pausar, reiniciar, borrar bots y borrar trazos. | E3 | Implementado | RM (2026-04-03) |
| FR-BOT-11 | Número de bots y auto-spawn con spawn rate configurable. | E3 | Implementado y probado | SR 4.5, RM |
| FR-BOT-12 | Trazado como polilínea. Un punto nuevo se añade solo si la distancia al último supera un umbral de 1 a 2 px. | E3 | Implementado con umbral de 2 px | SR 10, RM Fase 7 |
| FR-BOT-13 | Suavizado adaptativo: `v = lerp(v_prev, v_actual, factor)` según el cambio de ángulo; los giros mayores usan un factor menor. | E3 | Implementado y probado | SR 4.5, RM Fase 8 |
| FR-BOT-14 | Estabilidad: si `fieldVariance`, `botDensity` o `curvatureVariance` indican inestabilidad, reducir deriva y magnitud. | E3 | Implementado y probado; umbrales en `Config` | SR 4.5, RM Fase 9 |
| FR-BOT-15 | Representación de bots: punto, círculo o triángulo (indica dirección). | E4 | Implementado | SR 4.5, RM Fase 10 |
| FR-BOT-16 | Modos de color: constante, opacidad según vida o paleta cíclica de dos colores cada 2 s. | E4 | Implementado | SR 4.5, RM Fase 11 |
| FR-BOT-17 | Fade de trazos activable. | E4 | Implementado | RM (2026-04-03) |

## 6. Exportación

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-EXP-01 | Exportar trazos de bots a SVG (polilíneas). | E5 | Implementado desde File > Export | SR 11, RM Fase 12 |
| FR-EXP-02 | Exportar trazos de bots a DXF. | E5 | Implementado desde File > Export | SR 11, RM Fase 12 |
| FR-EXP-03 | Exportar imagen raster PNG y JPG. | E5 | Implementado desde File > Export | SR 11 |
| FR-EXP-04 | Reducción de puntos con Douglas-Peucker. `epsilon` de 1 a 10 (slider). | E5 | Implementado como opción en exportación vectorial | RM Fase 13 |
| FR-EXP-05 | Ordenar trazos por vecino más cercano para reducir el recorrido de la pluma. | E5 | Implementado como opción en exportación vectorial | RM Fase 13 |
| FR-EXP-06 | Preservar topología: comprobar ángulo y longitud de segmento, proteger bucles y ramas. | E5 | Implementado en simplificación; requiere validación visual en visor externo | RM Fase 14 |

## 7. Persistencia

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-PER-01 | En la versión 1 no hay guardado de proyecto. | - | Decidido (D-004) | SR 13, RS 11 #4 |
| FR-PER-02 | Guardar y abrir proyecto en JSON: tamaño del lienzo, campo, parámetros de bots, semilla e imagen de fondo. Requiere aprobación. | E6 | Pendiente | RM Fase 15 |
| FR-PER-03 | Reproducibilidad con semilla aleatoria. | Backlog | Pendiente | SR 13 |

## 8. Interfaz

| ID | Requisito | Etapa | Estado | Fuente |
|---|---|---|---|---|
| FR-UI-01 | Barra de menú File, Edit, View y Help bajo la barra nativa de Windows. Las opciones `[planned]` van deshabilitadas. | E0 | Implementado | GUI 1-2 |
| FR-UI-02 | Panel izquierdo de 3 niveles: pestañas de capa, ribbon de herramientas y panel de ajustes con scroll. | E1 | Implementado | GUI 3, RS 3 |
| FR-UI-03 | Ajustes de VECFD según TUI. Erase debe compartir los ajustes de Paint; Field Actions agrupa acciones de campo completo. | E1 | Implementado y probado para Paint, Erase y Field Actions; revisión visual pendiente | TUI, RM (2026-04-10) |
| FR-UI-04 | Ajustes completos de BOTFD (Place, Random, Auto). | E4 | Parcial: controles de deriva/color implementados; ajustes del radar de repulsión siguen pendientes de OD-12 | TUI, GUI 6, D-016, D-017 |
| FR-UI-05 | Herramienta Export en BOTFD con selector de formato y botón. | E5 | Implementado | GUI 6 |
| FR-UI-06 | Callbacks sin implementar se marcan con `setStatus("[planned] ...")`. | E1 | Verificar | RS 6, RM |
| FR-UI-07 | Panel de 230 px por defecto y 200 px mínimo. | E1 | Implementado y probado | GUI 7 |
| FR-UI-08 | Controles interactivos de al menos 44 px de alto. Iconos del ribbon de 32 px con área de pulsación de 44 px. | E1 | Implementado en ToolPanel; falta revisión visual | GUI 7, RS 11 #6 |
| FR-UI-09 | Tooltips y etiquetas en todos los controles. | E1 | Verificar | RS 5 |
| FR-UI-10 | Navegación por teclado según el plan de pruebas. | E1 | Parcial | RS 5, KEYBOARD_NAVIGATION_TEST_PLAN.md |
| FR-UI-11 | Undo, Redo y Preferences. | Backlog | Pendiente | GUI 2, OD-07 |
| FR-UI-12 | Ventana de atajos (Help > Shortcuts). | Backlog | Pendiente | GUI 2 |
