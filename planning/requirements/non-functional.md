# Requisitos no funcionales

Fecha de revisión: 2026-10-13. Fuentes según la tabla de [../README.md](../README.md).

## Plataforma y tecnología

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-01 | Aplicación de escritorio local para Windows. | Ejecución con `.\gradlew.bat run`. | SR, RS 2 |
| NFR-02 | Java con `sourceCompatibility` y `targetCompatibility` en 11. No usar records, switch expressions ni text blocks. | Compilar con `--release 11` (propuesta en E0). | RS 2, RS 8 #7 |
| NFR-03 | Interfaz con Swing: `JFrame`, `JMenuBar`, `JPanel`, `JFileChooser`. | Revisión de código. | RS 2, D-001 |
| NFR-04 | Sin dependencias externas. Cualquier librería nueva requiere aprobación. | Revisión de `build.gradle`. | RS 8 #6 |
| NFR-05 | Build con Gradle wrapper. `mainClass = 'FlowDrawing'` (paquete por defecto). Comandos: `.\gradlew.bat build` y `.\gradlew.bat run`. | Ambos comandos sin error. | RS 2 |
| NFR-06 | Versiones de JDK y Gradle fijadas y documentadas. RM menciona Gradle 9.4.1 y 8.5 en distintas sesiones. | Revisar `gradle/wrapper/gradle-wrapper.properties`. | RM |

## Calidad y rendimiento

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-07 | Prioridad: corrección y claridad sobre rendimiento (prototipo). | Revisión en cada etapa. | SR 12, RM |
| NFR-08 | Render continuo a 60 FPS (`DEFAULT_FPS`). | Contador de FPS en la barra de estado o en log. | RS 5 |
| NFR-09 | Objetivo final: 500 bots, lienzo de 4000 x 4000, rejilla de 10 px, 20 a 40 FPS. | Benchmark en E6. | RM Fase 16, RS 5 |
| NFR-10 | Memoria acotada: los trazados se compactan por umbral de distancia y no crecen sin límite. | Prueba de larga duración en E3. | RM Fase 7, RS 5 |

## Hilos

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-11 | La UI se construye en el Event Dispatch Thread con `SwingUtilities.invokeLater`. | Revisión de código. | RS 5 |
| NFR-12 | Los componentes Swing solo se modifican en el EDT. | Revisión de código. | RS 5 |
| NFR-13 | El bucle de render y la simulación corren en hilos propios. Las listas de trazados se copian antes de iterar. | Prueba con 100+ bots sin `ConcurrentModificationException`. | RM (2026-04-03), RS 3 |

## Configuración y código

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-14 | Umbrales y conteos viven en `config/Config.java`. No hay valores mágicos en la lógica. | Revisión de código. | RS 7, RS 8 #8 |
| NFR-15 | Convenciones: clases en PascalCase, constantes en UPPER_SNAKE_CASE. | Revisión de código. | RS 9 |
| NFR-16 | Código existente de Processing (`src/*.pde`, `src/bots/`, `src/brushes/`, `src/export/`, `src/optimization/`, `src/simulation/`, `src/ui/`) no se modifica sin aprobación. | Revisión de cambios. | RS 11 #9 |
| NFR-17 | `ui/Archive/` no se elimina ni se mueve sin confirmación. | Revisión de cambios. | RS 9 |
| NFR-18 | No renombrar `deveopment_roadmap.txt` ni `Software requirements`. El README los referencia. | Revisión de cambios. | RS 11 #10 |

## Experiencia de usuario y accesibilidad

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-19 | Estilo: fondo `#2E2E2E`, texto `#E8E8E8`, activo `#00FF00`, hover solo con borde `#00FF00`. | Revisión visual. | GUI 7, RS 11 #5 |
| NFR-20 | Sin animaciones. Las interacciones son instantáneas. Scroll vertical únicamente. | Revisión visual. | GUI 7 |
| NFR-21 | Contraste suficiente según `COLOR_CONTRAST_ANALYSIS.md`. | Revisión con el análisis de contraste. | RS 5 |
| NFR-22 | Navegación por teclado y etiquetas accesibles según `KEYBOARD_NAVIGATION_TEST_PLAN.md`. | Plan de pruebas de teclado. | RS 5 |

## Control de versiones

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-23 | `master` contiene versiones estables. Ramas de trabajo `phase-N-...` o `feature/...`. | Revisión de ramas. | RS 9 |
| NFR-24 | Commit por etapa con formato `Phase N: <descripción>`, y PR al completar la etapa. | Revisión del historial. | RS 9 |
| NFR-25 | `.gitignore` excluye `bin/`, `build/`, `*.class`, `*~` y `.gradle/`. | Revisión de `.gitignore`. | RS 9 |

## Documentación

| ID | Requisito | Verificación | Fuente |
|---|---|---|---|
| NFR-26 | Documentación del proyecto en español. Nombres de código en inglés. | Revisión de documentos. | RS 9 |
| NFR-27 | `deveopment_roadmap.txt` y `Software requirements` se mantienen sincronizados con las decisiones de `architecture/decisions.md`. | Revisión al cerrar cada etapa. | RS 9, RM |
