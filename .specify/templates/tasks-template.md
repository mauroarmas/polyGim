---
description: "Task list template for gym platform feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: Constitución aprobada, spec.md vigente y plan.md aprobados;
research.md, data-model.md y contracts/ según el alcance.

**Tests**: OBLIGATORIOS para lógica de negocio y aislamiento multi-tenant conforme a
`.specify/memory/constitution.md`. Incluir JUnit 5, Mockito cuando corresponda,
Testcontainers con PostgreSQL y Flyway, y JaCoCo ≥80% de líneas de lógica de negocio.
El silencio de la especificación no convierte estas pruebas en opcionales.

**Organization**: Tasks are grouped by user story to enable independent implementation
and testing of each story. Presentar tasks.md para aprobación antes de implementar.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo si afecta archivos distintos y no tiene
  dependencias pendientes; no implica autorización para delegar a agentes.
- **[Story]**: Historia correspondiente, por ejemplo US1, US2 o US3.
- Incluir rutas exactas y criterios verificables. Asignar IDs consecutivos.

## Path Conventions

- Aplicación: `polygim-api/src/main/java/<base-package>/`.
- Capas: `controller/`, `service/`, `repository/`; otros paquetes se justifican en el plan.
- Pruebas: `polygim-api/src/test/java/<base-package>/`.
- Migraciones: `polygim-api/src/main/resources/db/migration/`.
- Build y cobertura: `polygim-api/pom.xml`; CI: `.github/workflows/`.
- `original/` es referencia de solo lectura; no se copia código ni configuración.
- Reemplazar `<base-package>` y ejemplos por rutas concretas del plan aprobado.

<!-- Las tareas siguientes son ejemplos. Reemplazarlas por el alcance aprobado;
     no conservar tareas genéricas ni generar funciones de features posteriores. -->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Estructura Java 21 / Spring Boot 3 y herramientas del proyecto.

- [ ] T001 Crear estructura Maven en polygim-api/ conforme al plan aprobado
- [ ] T002 Configurar dependencias, tests y control JaCoCo en polygim-api/pom.xml
- [ ] T003 [P] Configurar verificación Maven y secretos por entorno en .github/workflows/verify.yml

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura compartida necesaria para las historias del alcance.

- [ ] T004 Definir migraciones Flyway e integridad por gimnasio en polygim-api/src/main/resources/db/migration/[version]__[description].sql
- [ ] T005 Implementar resolución autorizada y aislamiento automático del tenant en polygim-api/src/main/java/<base-package>/[tenant-path].java
- [ ] T006 Configurar Spring Security y permisos en polygim-api/src/main/java/<base-package>/[security-path].java
- [ ] T007 Centralizar errores RFC 7807 en polygim-api/src/main/java/<base-package>/controller/[error-handler].java
- [ ] T008 Configurar valores sensibles por variables de entorno en polygim-api/src/main/resources/application.yml
- [ ] T009 Probar migraciones y aislamiento con dos gimnasios y Testcontainers en polygim-api/src/test/java/<base-package>/[tenant-integration-test].java

**Checkpoint**: Infraestructura validada, sin accesos cruzados ni generación automática de DDL.

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Resultado de negocio de la historia]

**Independent Test**: [Escenario verificable de forma independiente]

### Tests for User Story 1 (mandatory when applicable by constitution)

- [ ] T010 [P] [US1] Probar reglas, límites y cálculos monetarios en polygim-api/src/test/java/<base-package>/service/[ServiceTest].java
- [ ] T011 [P] [US1] Probar persistencia, permisos y denegación entre gimnasios en polygim-api/src/test/java/<base-package>/[StoryIntegrationTest].java
- [ ] T012 [P] [US1] Probar errores HTTP RFC 7807 en polygim-api/src/test/java/<base-package>/controller/[ControllerTest].java

### Implementation for User Story 1

- [ ] T013 [US1] Implementar modelo y restricciones de la historia en polygim-api/src/main/java/<base-package>/[entity-path].java
- [ ] T014 [US1] Implementar persistencia aislada en polygim-api/src/main/java/<base-package>/repository/[Repository].java
- [ ] T015 [US1] Implementar reglas y transacciones en polygim-api/src/main/java/<base-package>/service/[Service].java
- [ ] T016 [US1] Implementar contrato HTTP y delegación en polygim-api/src/main/java/<base-package>/controller/[Controller].java

**Checkpoint**: Historia funcional, tests aplicables aprobados y aislamiento preservado.

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Resultado de negocio de la historia]

**Independent Test**: [Escenario verificable de forma independiente]

### Tests for User Story 2 (mandatory when applicable by constitution)

[Agregar tareas concretas de reglas de negocio, integración y errores del alcance.]

### Implementation for User Story 2

[Agregar tareas concretas con rutas y dependencias, conforme al plan aprobado.]

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Resultado de negocio de la historia]

**Independent Test**: [Escenario verificable de forma independiente]

### Tests for User Story 3 (mandatory when applicable by constitution)

[Agregar tareas concretas de reglas de negocio, integración y errores del alcance.]

### Implementation for User Story 3

[Agregar tareas concretas con rutas y dependencias, conforme al plan aprobado.]

## Phase N: Polish & Cross-Cutting Concerns

- [ ] TXXX Ejecutar mvn verify en polygim-api/pom.xml y comprobar JaCoCo ≥80% de líneas de negocio
- [ ] TXXX Validar aislamiento, permisos, precisión monetaria, migraciones y contratos de error en las pruebas aplicables de polygim-api/src/test/java/
- [ ] TXXX Revisar ausencia de secretos y constantes del primer tenant en polygim-api/src/
- [ ] TXXX Actualizar contratos y validación manual reproducible en specs/[###-feature-name]/quickstart.md

## Dependencies & Execution Order

- Setup precede a Foundational; Foundational bloquea historias que dependan de ella.
- Cada historia declara sus dependencias y sus pruebas obligatorias antes de implementación.
- Persistencia y reglas preceden a endpoints; las reglas permanecen fuera del controlador.
- Migraciones preceden a pruebas de integración que las requieran.
- Los controles finales se ejecutan antes de declarar la feature completa.

## Parallel Example: User Story 1

[Identificar tareas independientes por archivo; no paralelizar cambios sobre el mismo archivo.]

## Implementation Strategy

- Implementar por historias en orden de prioridad dentro de la feature aprobada.
- Validar cada incremento con sus tests antes de continuar.
- Consultar ambigüedades materiales y actualizar los artefactos afectados.
- Usar Conventional Commits por cambios coherentes; no reemplazar revisiones por hooks.

## Notes

- Mantener trazabilidad entre tareas, requisitos, historias y controles constitucionales.
- Toda tarea debe tener alcance y resultado revisables, sin decisiones de negocio implícitas.
- No crear código de aplicación antes de la aprobación de este artefacto.
