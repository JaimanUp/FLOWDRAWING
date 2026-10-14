# Estrategia de pruebas

Fecha de revisión: 2026-10-13. Las comprobaciones de lógica automatizadas no usan dependencias externas (D-014). También se usan compilación, ejecución y checklist manual.

## 1. Niveles

| Nivel | Qué cubre | Cómo | Cuándo |
|---|---|---|---|
| Compilación | Sintaxis, Java 11, imports | `.\gradlew.bat build` | Cada cambio |
| Ejecución | Arranque, excepciones en consola | `.\gradlew.bat run` | Cada cambio |
| Regresión manual de UI | Canvas, capas, herramientas, callbacks | Checklist de la sección 3 | Al cerrar cada etapa |
| Lógica automatizada | Campo y pincel | `gradlew.bat logicChecks` | Cada cambio en la lógica del campo o pincel |
| Exportación | SVG y DXF válidos | Abrir en visor externo | E5 |
| Rendimiento | FPS y memoria con N bots y tamaño de lienzo | Medición con contador de FPS y perfilador | E3 y E6 |
| Accesibilidad | Teclado, contraste, tamaños | `KEYBOARD_NAVIGATION_TEST_PLAN.md`, `COLOR_CONTRAST_ANALYSIS.md` | Al cerrar E1 y E4 |

## 2. Casos prioritarios

Estos casos deben existir como pruebas antes de cambiar la lógica correspondiente.

Las comprobaciones implementadas de muestreo bilineal, falloff y dureza, borrador, espaciado del pincel y vida por luminancia se ejecutan con `gradlew.bat logicChecks`.

| ID | Área | Caso | Resultado esperado |
|---|---|---|---|
| T-01 | Falloff | Linear con `d = 0` | 1 |
| T-02 | Falloff | Linear con `d = radius` | 0 |
| T-03 | Falloff | Hard Edge con `d < radius` y con `d >= radius` | 1 y 0 |
| T-04 | Falloff | Gaussian es monótona decreciente en `d` | Verdadero para una muestra de valores |
| T-05 | Normalización | Campo con 30 % de vectores en `maxMagnitude` | Campo completo escala proporcionalmente hasta 70 % del límite (D-003) |
| T-06 | Normalización | Reproducir saturación repetida | La escala se aplica una vez y permanece estable bajo el umbral |
| T-07 | Muestreo | Bilineal en el centro de una celda de 4 vectores | Media de los 4 vectores |
| T-08 | Trazado | Punto nuevo con distancia menor que el umbral | No se añade |
| T-09 | Trazado | Punto nuevo con distancia mayor que el umbral | Se añade |
| T-10 | Vista | Zoom y pan no modifican posiciones de bots ni vectores | Posiciones iguales antes y después |
| T-11 | Exportación | Douglas-Peucker con epsilon positivo y polilínea cerrada | Reduce puntos; conserva extremos y al menos 3 puntos del bucle |
| T-12 | Exportación | Vecino más cercano en 3 trazos conocidos | Orden y reversión de extremos comprobados |
| T-13 | Vida de bots | Comparar vida restante sobre fondos negro y blanco con decay por luminancia activo | El fondo blanco consume más vida que el negro |
| T-14 | Estabilidad de bots | Comparar fuerza de deriva con inestabilidad mínima y máxima | Inestabilidad máxima reduce la magnitud de movimiento |
| T-15 | Auto-spawn | 60 bots/s, objetivo 3, avanzar 3 frames | Añade como máximo un bot por frame y alcanza el objetivo |

## 3. Checklist de regresión

Copiar esta lista en la descripción del PR de cada etapa.

### Arranque
- [ ] `.\gradlew.bat build` sin errores ni warnings nuevos.
- [ ] `.\gradlew.bat run` abre la ventana sin excepciones en consola.

### Lienzo
- [ ] Zoom con la rueda.
- [ ] Pan arrastrando.
- [ ] `r` restaura la vista.
- [ ] Espacio + `+`/`-` y Espacio + flechas funcionan.

### Capas y herramientas
- [ ] Cambiar de pestaña de capa actualiza el ribbon y los ajustes.
- [ ] Cambiar de herramienta actualiza la selección en verde y los ajustes.
- [ ] El ojo muestra y oculta la capa sin cambiar la herramienta activa.
- [ ] La transparencia aplica a la capa o herramienta correcta (OD-01).
- [ ] Ningún control hace nada sin estar marcado como `[planned]`.

### Campo y pincel (desde E2)
- [ ] Paint modifica el campo y Erase lo reduce.
- [ ] Los tres falloffs se comportan según T-01 a T-04.
- [ ] El campo no se edita mientras la simulación corre.

### Bots (desde E3)
- [ ] Start, Pause y Reset funcionan, incluido un toggle rápido.
- [ ] El contador de bots se actualiza en todos los cambios de estado.
- [ ] Sin excepciones tras 5 minutos con 100 bots.

### Exportación
- [ ] El SVG abre en un visor externo.
- [ ] El DXF abre en un visor externo.
- [ ] PNG/JPG exportados abren y conservan el tamaño nativo del lienzo.
- [ ] Simplificación y ordenación solo se aplican cuando se seleccionan.

## 4. Datos de prueba

- Imagen de fondo: usar al menos una imagen clara y una oscura para validar la vida por luminancia (FR-BKG-06).
- Campo: usar un campo vacío, uno aleatorio (Random) y uno con trazo uniforme para cada caso de E2.
- Número de bots: 1, 50 (valor por defecto), 100 y 500 (solo E6).
