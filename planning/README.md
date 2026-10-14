# FlowDrawing: documentación de planificación

Esta carpeta organiza la planificación por áreas. Consolida los documentos originales de la raíz y de `docs/` sin reemplazarlos. Fecha de revisión: 2026-10-13.

## Estructura

| Área | Documento | Contenido |
|---|---|---|
| Requisitos | [requirements/functional.md](requirements/functional.md) | Requisitos funcionales (FR-*) con etapa y estado |
| Requisitos | [requirements/non-functional.md](requirements/non-functional.md) | Requisitos no funcionales (NFR-*) |
| Requisitos | [requirements/ui-spec.md](requirements/ui-spec.md) | Matriz de controles por capa y herramienta |
| Arquitectura | [architecture/overview.md](architecture/overview.md) | Módulos, dependencias, flujo de datos, hilos, configuración |
| Arquitectura | [architecture/decisions.md](architecture/decisions.md) | Decisiones adoptadas (D-*) y abiertas (OD-*) |
| Roadmap | [roadmap/roadmap.md](roadmap/roadmap.md) | Etapas E0 a E6 con criterios de salida y trazabilidad |
| Pruebas | [testing/test-strategy.md](testing/test-strategy.md) | Niveles de prueba, casos prioritarios y checklist de regresión |

## Fuentes originales

| Abrev. | Documento (raíz de FLOWDRAWING salvo indicación) |
|---|---|
| SR | `Software requirements` |
| RM | `deveopment_roadmap.txt` |
| GUI | `gui_menu_design.txt` |
| TUI | `docs/TestUI.md` |
| RS | `RESUMEN_PROYECTO_PARA_AGENTES.md` (REFERENCIAS) |

Pendiente de revisión: `FlowGenerativeEngine_Documentation.pdf`. El entorno no tiene lector de PDF. Revisarlo antes de cerrar la etapa E1.

## Reglas de mantenimiento

- Los IDs (FR-*, NFR-*, D-*, OD-*, E*) son estables. No se reutilizan. Un requisito retirado pasa a estado `Eliminado`.
- Estados válidos:
  - `Implementado`: existe y cumple el requisito.
  - `Parcial`: existe pero incompleto.
  - `Pendiente`: no existe.
  - `Verificar`: hay código, pero no se ha comprobado contra el requisito.
  - `Eliminado`: retirado por decisión.
- Si un documento original contradice esta carpeta, prevalece `architecture/decisions.md` hasta que el usuario decida.
- Una OD abierta no se implementa sin decisión explícita.
- Al cerrar una etapa: actualizar estados en `requirements/functional.md` y `roadmap/roadmap.md`, y sincronizar RM y SR.
- Documentación en español. Nombres de clases, métodos y variables en inglés.
