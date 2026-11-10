# Flujo Git del proyecto

- `main` es la rama de producción; no trabajar directamente en ella.
- `dev` es la rama de integración del desarrollo y nace de `main`.
- Crear una rama independiente por spec, a partir de `dev`, con el formato
  `NNN-descripcion` utilizado por Spec Kit.
- Antes de crear la rama de una nueva spec, verificar que el trabajo actual esté
  guardado y seleccionar `dev` como base. No descartar cambios para cambiar de rama.
- Los cambios se trabajan y se commitean en la rama de la spec. Integrar en `dev` y
  promover a `main` requiere la indicación del autor; no hacerlo como parte de specify.
- Usar Conventional Commits y los hooks de autocommit autorizados de Spec Kit.

# Contexto y artefactos

- Consultar `docs/PLAN_DE_TRABAJO.md`, `polygim-api/legacy/DOMINIO.md` y la constitución
  `.specify/memory/constitution.md` para mantener coherencia con las decisiones acordadas.
- Presentar spec, plan y tasks para aprobación antes de avanzar de etapa.
- `original/` es referencia de dominio de solo lectura y no se copia al producto.

# graphify

- Al recibir `/graphify`, usar la skill instalada de graphify antes de cualquier
  otra acción. No generar artefactos de grafo durante una tarea de solo lectura.
