<!--
Enmienda 1.1.0 (2026-11-10): el autor autorizó implementar la interfaz del núcleo
y entregar pantallas junto con cada feature. Se sincronizan plan de producto,
dominio y artefactos de feature 1; la feature 2 se actualizará en su rama.

Sync Impact Report
Version change: plantilla sin versión → 1.0.0 (adopción inicial).
Ratificación: 2026-11-10, al solicitar el usuario avanzar a la spec 1.
No cambian los principios ni la versión al completar la ratificación.
Modified principles: los cinco bloques genéricos se reemplazan por:
  I. Arquitectura en capas y simplicidad
  II. Aislamiento multi-tenant obligatorio
  III. Negocio configurable e independencia del primer gimnasio
  IV. Precisión monetaria
  V. Configuración sensible fuera del código
  VI. Pruebas obligatorias y cobertura verificable
  VII. Contratos de error uniformes
  VIII. Evolución de datos mediante migraciones
Added sections: Restricciones técnicas; Flujo de trabajo y controles de calidad;
  gobernanza concreta, revisiones de artefactos y política de versiones.
Removed sections: ejemplos y marcadores genéricos de la plantilla inicial.
Consistency propagation:
  ✅ updated: .specify/templates/plan-template.md
  ✅ updated: .specify/templates/spec-template.md
  ✅ updated: .specify/templates/tasks-template.md
  ✅ updated: .agents/skills/speckit-tasks/SKILL.md
  ✅ updated: .agents/skills/speckit-implement/SKILL.md
  ✅ updated: .specify/workflows/speckit/workflow.yml
  ✅ updated: .specify/workflows/workflow-registry.json
  ✅ updated: .specify/extensions/git/git-config.yml
  ✅ updated: .specify/extensions/git/README.md
  ✅ added: .gitignore (legacy local, archivos de entorno y compilación).
  ✅ reviewed: restantes skills speckit instaladas; sin cambios necesarios.
  ✅ reviewed: polygim-api/legacy/DOMINIO.md; decisiones compatibles.
Deferred: ninguno. Fecha de ratificación completada con la aprobación para avanzar.
-->

# Constitución de la plataforma de gestión de gimnasios

## Core Principles

### I. Arquitectura en capas y simplicidad

El producto DEBE usar Java 21 y Spring Boot 3. El código de aplicación DEBE residir
en `polygim-api/` y organizarse en capas `controller`, `service` y `repository`.
Los controladores DEBEN gestionar el contrato HTTP y delegar los casos de uso a
servicios; los servicios DEBEN coordinar las reglas de negocio y transacciones;
los repositorios DEBEN encapsular la persistencia. Las reglas de negocio NO DEBEN
implementarse en controladores, plantillas de UI ni consultas de presentación.

Cada decisión de diseño DEBE resolver una necesidad del alcance aprobado y explicar
su motivo y sus consecuencias en el plan. Se DEBE elegir la solución más simple que
satisfaga los requisitos, manteniendo las reglas de negocio verificables sin la UI.

### II. Aislamiento multi-tenant obligatorio

Un gimnasio es un tenant. Los gimnasios DEBEN compartir una base PostgreSQL y cada
entidad de negocio DEBE estar asociada a su gimnasio mediante `gimnasio_id`.
El aislamiento DEBE aplicarse automáticamente en la infraestructura de persistencia,
sin depender de filtros manuales repetidos en cada consulta. El contexto del tenant
DEBE obtenerse de una identidad autenticada y autorizada; un identificador enviado
por el cliente NO DEBE permitir cambiar de gimnasio ni eludir el aislamiento.

Ningún usuario de un gimnasio DEBE poder leer, crear, modificar, eliminar ni asociar
datos de negocio de otro gimnasio. La ausencia de un contexto de tenant válido DEBE
rechazar las operaciones sobre datos de negocio. El superadmin DEBE gestionar
gimnasios y primeros administradores sin acceso a sus datos de negocio.

El mecanismo previsto es Hibernate 6 con `@TenantId`. El plan DEBE verificar su
comportamiento en la versión seleccionada y documentar cómo se protege cada vía de
acceso utilizada, incluidas búsquedas por identificador, relaciones y cualquier
consulta especial. Usar la anotación NO sustituye demostrar el aislamiento.

Las pruebas de integración DEBEN usar al menos dos gimnasios y comprobar tanto
operaciones permitidas dentro del propio gimnasio como intentos de acceso y
asociación entre gimnasios. Una fuga de datos DEBE bloquear la entrega.

### III. Negocio configurable e independencia del primer gimnasio

Las disciplinas, membresías, precios, duraciones, cantidades máximas, canales de
llegada, cupos y políticas de reserva y cancelación DEBEN ser configuración por
gimnasio. NO DEBEN existir decisiones de negocio basadas en nombres de membresías,
disciplinas o gimnasios fijados en el código.

Poly es el primer tenant y solo DEBE aparecer como dato de seed o referencia
documental de origen. Su nombre y oferta NO DEBEN aparecer en clases, paquetes,
condicionales, nombres de pruebas ni configuración estructural de la aplicación.
Los seeds DEBEN cargar datos mediante mecanismos genéricos; los planes y precios
iniciales DEBEN identificarse como ejemplos ficticios.

`original/` DEBE utilizarse únicamente como referencia del dominio. NO DEBEN copiarse
su código, arquitectura, estilo, UI Swing ni acceso JDBC. Las reglas recuperadas
DEBEN explicarse y reescribirse conforme a estas normas. La paridad es de dominio,
no una obligación de reproducir funcionalidades incompletas o errores del legacy.

### IV. Precisión monetaria

Todos los importes, precios, subtotales, totales, pagos, deudas y vueltos DEBEN
representarse y calcularse con `BigDecimal`, con escala monetaria de dos decimales,
y persistirse con tipos decimales exactos. NO DEBEN usarse `float` ni `double`,
incluidas conversiones intermedias de los cálculos o la construcción de importes.

El plan DEBE explicitar la precisión de almacenamiento y la política de redondeo
necesaria para las operaciones de la feature. Las pruebas DEBEN cubrir valores
decimales y casos límite. Los cambios del catálogo NO DEBEN alterar los precios
unitarios congelados en compras anteriores.

### V. Configuración sensible fuera del código

Las credenciales y demás valores sensibles DEBEN suministrarse mediante variables
de entorno. NO DEBEN incorporarse a código, migraciones, seeds, pruebas,
documentación, logs ni historial Git. Los archivos de ejemplo DEBEN usar valores
ficticios; los archivos de entorno locales DEBEN excluirse del seguimiento.

Las credenciales presentes en `original/` NO DEBEN copiarse ni reproducirse en
archivos del producto. La configuración de CI y despliegue DEBE inyectar sus
secretos como variables de entorno.

### VI. Pruebas obligatorias y cobertura verificable

Toda lógica de negocio nueva o modificada DEBE tener tests unitarios con JUnit 5 y,
cuando sean necesarios dobles de prueba, Mockito. Los casos DEBEN comprobar reglas,
validaciones y límites; la cobertura numérica NO sustituye probar el comportamiento.

JaCoCo DEBE comprobar una cobertura mínima del 80% de líneas del conjunto de clases
que contienen lógica de negocio, incluidas las capas de servicio y dominio que la
implementen. La configuración DEBE identificar ese conjunto de forma explícita;
NO DEBE excluirse lógica de negocio para alcanzar el umbral. El control DEBE fallar
en `mvn verify` y en CI cuando no se alcance el mínimo.

El aislamiento multi-tenant DEBE verificarse con tests de integración contra
PostgreSQL mediante Testcontainers, ejecutando las migraciones Flyway reales.
Los tests unitarios con mocks NO sustituyen estas pruebas. Los errores HTTP y la
autorización DEBEN probarse cuando la feature exponga o modifique esos contratos.

### VII. Contratos de error uniformes

Los errores de la API REST DEBEN seguir RFC 7807, con tipo de contenido
`application/problem+json`, estado HTTP coherente y los campos `type`, `title`,
`status`, `detail` e `instance`. El tratamiento DEBE centralizarse, incluyendo
errores de validación, negocio, autenticación y autorización.

Los errores NO DEBEN exponer trazas internas, SQL, secretos ni datos de otro tenant.
El contrato de la feature DEBE definir los errores relevantes y sus pruebas DEBEN
comprobar el formato y el estado HTTP.

### VIII. Evolución de datos mediante migraciones

Todo cambio de esquema del producto DEBE expresarse como una migración Flyway
versionada y revisable. NO DEBE aplicarse DDL manual fuera de esas migraciones ni
usarse Hibernate para crear o actualizar automáticamente el esquema.

Las migraciones DEBEN poder construir una base vacía y evolucionar la versión
anterior aplicable sin modificar migraciones ya ejecutadas. Las restricciones de
integridad y tenant DEBEN formar parte del diseño y de sus pruebas de integración.
`polygim-api/legacy/schema.sql` es referencia histórica, no una migración del producto.

## Restricciones técnicas

- Persistencia: Spring Data JPA, Hibernate 6, PostgreSQL y Flyway.
- Seguridad: Spring Security y permisos por rol y gimnasio, conforme al dominio aprobado.
- Interfaz: Thymeleaf + HTMX, entregada junto con cada feature funcional; sin frontend
  JavaScript separado.
- Herramientas: Maven, Docker Compose, JUnit 5, Mockito, JaCoCo y Testcontainers.
- Contratos REST documentados con springdoc-openapi y validación automatizada en
  GitHub Actions.
- Las versiones concretas compatibles y la configuración se DEBEN justificar en el
  plan; esta constitución no autoriza escribir código de aplicación antes de los
  artefactos y aprobaciones correspondientes.

## Flujo de trabajo y controles de calidad

El trabajo DEBE seguir constitution → specify → clarify → plan → tasks → implement,
con una feature por ciclo. La constitución y cada artefacto `spec.md`, `plan.md` y
`tasks.md` DEBEN presentarse al usuario para aprobación antes de avanzar. Si clarify
modifica una especificación aprobada, la versión actualizada DEBE aprobarse antes
de planificar. Las ambigüedades materiales DEBEN consultarse, no resolverse mediante
suposiciones silenciosas.

El orden de features DEBE ser: (1) núcleo multi-tenant con paridad de dominio y API
REST e interfaz funcional, (2) reservas con cupo, listado de próximos vencimientos
y sus pantallas, (3) branding y mejoras de experiencia y (4) demo pública y deploy.
Cada feature DEBE incluir un flujo reproducible de prueba desde el navegador. Las decisiones de negocio y
límites acordados se consultan en `polygim-api/legacy/DOMINIO.md`; este documento no
reemplaza las especificaciones de cada feature.

El plan DEBE incluir un Constitution Check antes de investigar y después de diseñar.
Las tareas DEBEN incluir tests unitarios, pruebas de aislamiento, migraciones,
contratos de error y validación de cobertura aplicables al alcance. Una feature
solo DEBE considerarse completa con los checks aplicables aprobados y ejecutados;
los checks no ejecutados DEBEN informarse explícitamente.

Los commits DEBEN seguir Conventional Commits: `tipo(alcance opcional): descripción`,
por ejemplo `docs: define gym platform constitution`. Cada revisión DEBE comprobar
aislamiento, permisos, exactitud monetaria, secretos, migraciones y pruebas. Los
hooks automáticos NO DEBEN sustituir la aprobación de los artefactos ni adelantar
etapas del ciclo.

## Governance

Esta constitución rige el producto nuevo. Las especificaciones, planes, tareas,
plantillas y revisiones DEBEN respetarla. Una incompatibilidad DEBE corregirse antes
de continuar; una justificación de complejidad no autoriza incumplir un principio.

Toda enmienda DEBE describir el motivo, el impacto en artefactos e implementación y
la transición necesaria. El usuario DEBE aprobarla antes de su adopción. Se DEBEN
actualizar las plantillas, instrucciones y artefactos afectados y registrar un
Sync Impact Report en este archivo.

La versión DEBE seguir SemVer: MAJOR para eliminar o redefinir principios de forma
incompatible; MINOR para agregar principios o ampliar obligaciones; PATCH para
aclaraciones que no cambien obligaciones. La versión 1.0.0 corresponde a la primera
constitución propuesta, no a una enmienda de una versión ratificada anterior.

**Estado**: Ratificada por el usuario al solicitar avanzar a la spec 1 el 2026-11-10.

**Version**: 1.1.0 | **Ratified**: 2026-11-10 | **Last Amended**: 2026-11-10
