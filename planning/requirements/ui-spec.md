# Matriz de controles de la interfaz

Fecha de revisión: 2026-10-13. Estructura según GUI, ajustes según TUI. El estado describe la implementación actual. Las decisiones pendientes se referencian como OD-* en [../architecture/decisions.md](../architecture/decisions.md).

## Niveles del panel izquierdo

1. Pestañas de capa (BKGD, VECFD, BOTFD). Solo una activa.
2. Submenú compacto de controles generales: icono de ojo abierto/cerrado, slider de transparencia sin etiqueta ni porcentaje y acciones de limpieza/reset apropiadas a la capa.
3. Ribbon con las herramientas propias de la capa. No contiene controles generales.
4. Panel de ajustes con scroll vertical. Cambia según la herramienta seleccionada.

Reglas: visibilidad y transparencia aplican a toda la capa activa y persisten por capa; ambas son independientes de la herramienta seleccionada.

## Capa BKGD

El botón general de limpieza quita la imagen cargada y vuelve al color de fondo seleccionado. No es una herramienta del ribbon.

| Herramienta | Ajustes | Estado | Etapa |
|---|---|---|---|
| Color | Selector de color en popup. | Implementado | E1 |
| Image | Cargar imagen (popup). | Implementado | E1 |

## Capa VECFD

Paint y Erase comparten los mismos ajustes: falloff (`Linear` | `Gaussian` | `Hard Edge`), tamaño, dureza y fuerza. La transparencia se controla para toda la capa VECFD en el submenú general.

| Herramienta | Ajustes específicos | Estado | Etapa |
|---|---|---|---|
| Paint | Ajustes comunes | Implementado | E2 |
| Erase | Ajustes comunes. Debe coincidir con Paint. | Implementado | E1 |
| Field Actions | Normalize Whole Field y Randomize Whole Field; operaciones sobre toda la rejilla, sin entrada de pincel | Implementado y probado | E1 |

Reset vector field to zero está en la fila general y afecta toda la rejilla.

Visualización del campo: View > Vector Field Display ofrece los modos Arrows y Flow Map (D-015).

## Capa BOTFD

### Herramientas

| Herramienta | Ajustes específicos | Estado | Etapa |
|---|---|---|---|
| Place | Click en el lienzo para ubicar un bot; botón alternativo para el centro. | Implementado; controles de movimiento, radar, deriva y representación compartidos con Auto | E4 |
| Random | Cantidad, spawn aleatorio. Grupos de ajustes. | Implementado para controles definidos; radar de repulsión pendiente de OD-12 | E4 |
| Auto | Play / Pause. Contador, parámetros de movimiento/radar, auto-spawn con spawn rate, vida por luminancia, fade, representación y color. | Implementado; radar de repulsión pendiente de OD-12 | E3/E4 |
| Export | BOTFD y File > Export ofrecen SVG, DXF, PNG y JPG; SVG/DXF permiten simplificar y ordenar trazos opcionalmente. | Implementado | E5 |

La fila general incluye acciones separadas para borrar todos los bots y todos los trazados.

### Grupos de ajustes (Place, Random y Auto)

| Grupo | Controles | Notas |
|---|---|---|
| General | Amount, Color (popup) | Transparencia de capa en el submenú general |
| Balance de fuerzas | Vector field, Repulsion, Drifting | Pesos fijos según RM. Hacerlos ajustables en E3. |
| Vida | Duration, Velocity, Illuminance decay | El decay depende del fondo (FR-BOT-08, FR-BKG-06) |
| Radar de campo | Falloff (`Linear` / `Gaussian` / `Hard Edge`), Radius, Hardness (peso del centro), Strength | |
| Radar de repulsión | Falloff, Radius, Hardness, Strength y Samples | TUI usa "Strength (samples)". Separar en dos controles (OD-12). |
| Deriva | Mode (`Unique` / `Collective` / `Canvas`), Velocity, Strength | Implementado según D-016 |
| Show More | Parámetros adicionales | Contradice GUI §9 (OD-16) |

Nota: la sección BOTFD de TUI repite bloques de ajustes. Esta matriz recoge cada grupo una sola vez.
