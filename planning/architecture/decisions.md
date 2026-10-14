# Registro de decisiones

Fecha de revisión: 2026-10-13. Las decisiones adoptadas (D-*) mandan sobre los documentos originales. Las decisiones abiertas (OD-*) no se implementan hasta que el usuario decida. Cada OD incluye una recomendación.

## Decisiones adoptadas

| ID | Tema | Decisión | Motivo | Fuente |
|---|---|---|---|---|
| D-001 | Plataforma | Swing se mantiene. Processing queda como legado (`.pde`) y no se migra. Actualizar la Fase 0 de RM. | El código, el build y la documentación activa usan Swing. | RS 11 #1 |
| D-002 | Tamaño del lienzo | 2000 x 2000 por defecto. Valor único en `Config.java`. No configurable desde la UI en v1. El objetivo de 4000 x 4000 queda para E6. | Es el valor que el código ya usa. | RS 11 #2 |
| D-003 | Normalización del campo | Cuando al menos el 30 % de los vectores alcanza el 95 % de `maxMagnitude`, escalar todo el campo para que su máximo quede en el 70 % de `maxMagnitude`. | Los vectores se limitan individualmente antes de comprobar el umbral; la normalización global crea margen proporcional sin permitir valores fuera del límite. | RS 11 #3 |
| D-004 | Persistencia | Fuera de la versión 1. Guardado de proyecto en E6, con aprobación. `Save Project` sigue deshabilitado. | Los requisitos (SR 13) lo excluyen en v1. | RS 11 #4 |
| D-005 | Color de hover | Solo verde. Hover = borde `#00FF00` sin cambio de fondo. Se descarta el cian `#00FFD1`. | Evita un segundo color de acento. | RS 11 #5 |
| D-006 | Altura de controles | 44 px mínimo para controles interactivos. | Es el valor más estricto de los documentos (WCAG). | RS 11 #6 |
| D-007 | Herramientas VECFD | Paint y Erase son las herramientas de pincel; Field Actions agrupa Randomize, Normalize y Reset sobre toda la rejilla. Overlay se elimina por duplicar Paint. | Separa la edición local con pincel de las operaciones globales sobre el campo. | Toolbar refactor |
| D-008 | Tipos de datos | Java manda: `Vector2D` y `BufferedImage`. `PVector` y `PGraphics` del roadmap son pseudocódigo. | Código activo en Java. | RS 11 #8 |
| D-009 | Archivos legacy | No tocar hasta cerrar E1. Después proponer destino. No borrar sin aprobación. | Evita romper referencias mientras se trabaja en UI. | RS 11 #9 |
| D-010 | Nombres de archivo | No renombrar `deveopment_roadmap.txt` ni `Software requirements`. Corregir el typo solo si el usuario lo pide. | El README los referencia. | RS 11 #10 |
| D-011 | Panel activo | `ui/ToolPanel.java` es el panel activo. `ui/Archive/UIPanel.java` está archivado y no se usa. | Estado real del código. | RS 11 #11 |
| D-012 | Estado de la Fase 1 del panel | La arquitectura de 3 niveles está cerrada. La funcionalidad de cada herramienta se completa en E1. Actualizar `docs/PHASE_1_COMPLETION_SUMMARY.md`: `ToolPanelRefactored` pasa a `ToolPanel`. | Evita confundir la Fase 1 de la UI con la Fase 1 de RM (canvas). | RS 11 #12 |
| D-013 | Estructura de la planificación | La documentación de planificación vive en `planning/` en la raíz del espacio de trabajo, con IDs estables (FR, NFR, D, OD, E). Los documentos originales se conservan. | Petición del usuario. | Sesión actual |
| D-014 | Pruebas de lógica | Se usa `src/test/java/FlowDrawingLogicChecks.java` sin dependencias, ejecutado con `gradlew.bat logicChecks` (tarea Gradle `logicChecks`). No se añade JUnit. | Mantiene NFR-04 y permite validar la lógica central. | OD-10 |
| D-015 | Ubicación del selector de visualización del campo | Los modos Arrows y Flow Map están en View > Vector Field Display. | Sigue GUI §5 y mantiene los controles de herramientas en el panel izquierdo. | OD-03 |
| D-016 | Modos de deriva BOTFD | Unique usa random walk independiente por bot; Collective comparte un vector aleatorio que cambia cada segundo; Canvas deriva con un vector dependiente de la posición en el lienzo. | Comportamientos aprobados por el usuario. | Sesión actual |
| D-017 | Color BOTFD | El selector de color configura un color base para Constant y Life opacity. Cyclic palette conserva su paleta de dos colores. | Comportamiento aprobado por el usuario. | Sesión actual |
| D-018 | Transparencia BKGD y luminancia | La transparencia afecta solo a la visualización; el decay de vida usa la imagen/color de fondo sin componer con alpha. | Comportamiento aprobado por el usuario. | Sesión actual |

## Decisiones abiertas

| ID | Tema | Opciones | Recomendación | Afecta a |
|---|---|---|---|---|
| OD-01 | Transparencia: por herramienta o por capa | (a) Por herramienta, cerrada en GUI §9. (b) Por capa, según SR 4.2, RS 4.2 y TUI. | (a). Sigue la decisión cerrada de la GUI. | FR-BKG-05, ui-spec |
| OD-02 | Confirmaciones de acciones destructivas | GUI pide confirmación en Clear (BKGD) y Reset (VECFD). RM (2026-04-09) las eliminó. | Mantener sin confirmación, salvo Reset del campo. No hay Undo y la acción no se recupera. | FR-UI-06, E1 |
| OD-04 | Botón de simulación en Auto | (a) Play / Pause, según GUI y RM. (b) Play / Stop, según TUI. | (a) | FR-BOT-10, ui-spec |
| OD-05 | Atajos de teclado por herramienta (GUI §9) | Sí o no | Backlog | FR-UI-12 |
| OD-06 | Persistir transparencias entre sesiones (GUI §9) | Sí o no | No. Sin persistencia en v1 (D-004). | FR-PER-01 |
| OD-07 | Undo y Redo en v1 (GUI §9, menú Edit) | Sí o no. Requiere patrón de comandos. | Backlog. Revisar con OD-02. | FR-UI-11 |
| OD-08 | Presets de herramientas (GUI §9) | Sí o no | Backlog | - |
| OD-09 | Botón "Reset All Settings" por capa (GUI §9) | Sí o no | Decidir en E1 | FR-UI-02 |
| OD-10 | Pruebas unitarias con JUnit | Resuelta por D-014: usar comprobaciones ejecutables sin dependencias externas. | No añadir JUnit sin aprobación | [../testing/test-strategy.md](../testing/test-strategy.md) |
| OD-11 | "Ultra Kernel" del diagrama final de RM | No está definido en el código ni en los requisitos. | Interpretarlo como `BotEngine` y confirmar. | overview §9 |
| OD-12 | "Strength (samples)" en el radar de repulsión (TUI, GUI) | (a) Un solo control. (b) Dos controles: Strength y Samples. | (b) | ui-spec, E4 |
| OD-13 | Número de muestras del radar | (a) 16 fijo, según Config y RM Fase 6. (b) Configurable 4 a 32, según RM (2026-04-09). | (b), con 16 por defecto | FR-BOT-04, E3 |
| OD-14 | Parámetros de la GUI con "Show More ▼" en BOTFD (GUI §6) | (a) Mantener. (b) Eliminar, según la decisión cerrada de GUI §9 de no usar grupos colapsables. | (b) | ui-spec, E4 |
