# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]

**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3, Spring Data JPA / Hibernate 6, Spring Security,
springdoc-openapi; Thymeleaf + HTMX cuando la feature incluya pantallas.
[Documentar versiones concretas compatibles y dependencias adicionales justificadas.]

**Storage**: PostgreSQL compartido, aislamiento automático por `gimnasio_id`, Flyway.

**Testing**: JUnit 5, Mockito, Testcontainers con PostgreSQL y migraciones reales;
JaCoCo con mínimo 80% de líneas de lógica de negocio, exigido en Maven y CI.

**Target Platform**: Aplicación web Java; Docker Compose para el entorno local.

**Project Type**: Aplicación Spring en capas `controller`, `service`, `repository`.

**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k lines/sec, 60 fps or NEEDS CLARIFICATION]

**Constraints**: [domain-specific, e.g., <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]

**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50 screens or NEEDS CLARIFICATION]

## Constitution Check

- [ ] La feature entrega pantallas Thymeleaf + HTMX junto con sus casos de uso, sesión web protegida y prueba funcional en navegador.

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [ ] Constitución, especificación vigente y aclaraciones aprobadas antes de planificar.
- [ ] Código nuevo dentro de `polygim-api/`, capas separadas y decisiones justificadas.
- [ ] Tenant resuelto desde identidad autorizada; aislamiento automático en lecturas,
      escrituras, búsquedas por ID y relaciones; sin acceso global a datos de negocio.
- [ ] Verificación de `@TenantId` en la versión seleccionada y de toda vía de consulta usada.
- [ ] Pruebas de integración con dos gimnasios, accesos propios y accesos cruzados denegados.
- [ ] Negocio configurable por gimnasio; primer tenant solo en seed de datos ficticios.
- [ ] Reglas del legacy explicadas y reescritas, sin copiar código de `original/`.
- [ ] Dinero con `BigDecimal`, decimal exacto, escala 2 y redondeo documentado.
- [ ] Tests unitarios de negocio y JaCoCo ≥80% de líneas de negocio en `mvn verify` y CI.
- [ ] Errores REST RFC 7807, incluidos seguridad y validación, con pruebas del contrato.
- [ ] Esquema solo mediante Flyway; sin DDL manual ni generación automática de Hibernate.
- [ ] Secretos solo por variables de entorno; ejemplos ficticios y sin secretos en Git.
- [ ] Plan presentado para aprobación antes de generar tareas; commits convencionales.

Para cada control registrar evidencia, tareas previstas o motivo verificable de no
aplicación. Una violación bloquea el avance; no se dispensa mediante Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

<!-- Expandir con el paquete Java concreto y los archivos de la feature.
     original/ es referencia de dominio de solo lectura. -->

```text
polygim-api/
├── pom.xml
├── legacy/                         # Referencia, no código ejecutable
└── src/
    ├── main/
    │   ├── java/<base-package>/
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   └── [otros paquetes justificados por la feature]
    │   └── resources/
    │       ├── db/migration/
    │       └── [templates y static solo en la feature de interfaz]
    └── test/java/<base-package>/    # Tests unitarios e integración
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Registrar complejidad adicional y alternativas evaluadas. Las violaciones de la
> constitución bloquean el avance y requieren corregirse o una enmienda aprobada.**

| Added Complexity | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., additional cache] | [measured need] | [why the simpler approved design is insufficient] |
