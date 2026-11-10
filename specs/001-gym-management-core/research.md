# Investigación técnica — Núcleo de gestión multi-gimnasio

Fecha de consulta: 2026-11-10. Alcance: decisiones de diseño para la feature 1;
no se ejecutó una implementación del producto. Investigación contrastada con fuentes
primarias y con la spec aprobada. Este archivo distingue evidencia documental del
gate de integración que deberá ejecutarse en M1.

## R1. Versiones compatibles y soporte

**Decisión:** Java 21, Spring Boot parent 3.5.16 y Maven Wrapper 3.9.11.
PostgreSQL 17.11: imagen oficial `postgres:17.11`, digest multiarch observado
`sha256:ff87138949ad9a46f32ff23bd6852b52ce024a13dffe9b4087be98292cdd3b4f`;
Compose y Testcontainers usarán la misma imagen fijada. Usar las
versiones gestionadas por el parent sin sobrescribir Hibernate o Spring Security.
Fijar springdoc-openapi 2.10.0 y JaCoCo 0.8.16 por separado.

| Componente | Versión de la línea elegida |
|---|---|
| Spring Framework | 6.2.19 |
| Hibernate ORM | 6.6.53.Final |
| Spring Security | 6.5.11 |
| Flyway core y módulo PostgreSQL | 11.7.2 |
| PostgreSQL JDBC | 42.7.11 |
| JUnit Jupiter | 5.12.2 |
| Mockito | 5.17.0 |
| Testcontainers | 1.21.4 |
| Surefire / Failsafe | 3.5.6 |
| Maven Compiler Plugin | 3.14.1 |

**Justificación:** combinación coherente con Java 21 y el requisito Spring Boot 3.
Las versiones se comprobaron en el POM oficial, no se dedujeron de números de otras
líneas. El tag de Hibernate utilizado para verificar el aislamiento es `6.6.53`.

**Alternativas:** Boot 4 requiere cambiar el stack aprobado; fijar versiones
individuales sobre el BOM aumenta el riesgo de incompatibilidades.

**Soporte:** Boot 3.5.16 fue anunciado como última versión OSS de 3.5. Se conserva
por el requisito explícito del autor, sin afirmar soporte OSS vigente. Antes de
publicar una demo expuesta o vender un despliegue se deberá revisar esta elección
con el autor; no se realizará una migración de major silenciosa.

**Fuentes:** [imagen oficial PostgreSQL](https://hub.docker.com/v2/repositories/library/postgres/tags/17.11),
[Maven 3.9.11](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11.pom),
[BOM 3.5.16](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.16/spring-boot-dependencies-3.5.16.pom),
[requisitos Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html),
[matriz springdoc](https://springdoc.org/v2/#_what_is_the_compatibility_matrix_of_springdoc_openapi_with_spring_boot),
[POM springdoc 2.10.0](https://repo.maven.apache.org/maven2/org/springdoc/springdoc-openapi/2.10.0/springdoc-openapi-2.10.0.pom),
[JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/changes.html),
[fin de releases OSS 3.5](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/).

## R2. Qué garantiza @TenantId en Hibernate 6.6.53

**Decisión:** entidades del negocio con `@TenantId`, tenant UUID resuelto desde la
identidad autorizada y `CurrentTenantIdentifierResolver` que rechaza contexto ausente.
`isRoot()` siempre devuelve false; SUPERADMIN nunca es un tenant root.

**Evidencia:** el binder del tag exacto establece `applyToLoadByKey=true`; el test
oficial verifica que `find` de un identificador ajeno devuelve null. La asignación
al persistir rechaza un tenant explícito distinto. Estas propiedades respaldan el
diseño, pero no sustituyen ejecutarlo con Spring Data, Security y PostgreSQL reales.

**Límite:** Hibernate no añade el filtro a SQL nativo. La aplicación de esta feature
no usará SQL nativo, JDBC de negocio, bulk JPQL, borrados rápidos por proxy ni merge
de entidades reconstruidas desde DTOs. Las actualizaciones cargan una entidad
propia administrada y aplican dirty checking. Las altas usan persist explícito en
una infraestructura de repositorio pequeña para no decidir merge por UUID asignado.

**Alternativas:** predicados tenant repetidos por cada consulta son frágiles; RLS
sería una defensa adicional si aparecieran otras vías de SQL, pero añade políticas,
roles y contexto de conexión innecesarios para las vías acotadas de esta feature.
No se promete aislamiento de acceso SQL externo directo al motor.

**Fuentes:** [binder del tag 6.6.53](https://raw.githubusercontent.com/hibernate/hibernate-orm/6.6.53/hibernate-core/src/main/java/org/hibernate/binder/internal/TenantIdBinder.java),
[test oficial por ID](https://raw.githubusercontent.com/hibernate/hibernate-orm/6.6.53/hibernate-core/src/test/java/org/hibernate/orm/test/tenantid/TenantIdTest.java),
[generación tenant](https://raw.githubusercontent.com/hibernate/hibernate-orm/6.6.53/hibernate-core/src/main/java/org/hibernate/generator/internal/TenantIdGeneration.java),
[guía de multitenancy](https://docs.hibernate.org/orm/6.6/introduction/html_single/#multitenancy),
[resolver](https://docs.jboss.org/hibernate/orm/6.6/javadocs/org/hibernate/context/spi/CurrentTenantIdentifierResolver.html).

## R3. Identidad global y negocio aislado con una sola persistencia

**Decisión:** una base, un DataSource y una EntityManagerFactory. Separar
`CuentaAcceso` global (email, hash, rol, estado, gimnasio asignado) de
`UsuarioGimnasio`, vínculo tenant que permite gestionar recepción y auditar actores
sin consultas globales de negocio. El vínculo comparte el UUID de la cuenta.
`Gimnasio` es la raíz administrativa global; el resto del negocio usa `gimnasio_id`.

**Justificación de diseño:** el login por email global debe localizar la identidad
antes de conocer el tenant. AUTH y PLATFORM usan el UUID sentinel reservado `00000000-0000-0000-0000-000000000000`, nunca
un gimnasio real ni un root. Sus transacciones se cierran antes de abrir las de
negocio. Un guard central rechaza negocio fuera de TENANT y exige ADMIN o RECEPCION.
El repositorio global de identidad solo expone las operaciones internas necesarias;
no hay endpoint global de búsqueda de cuentas ni datos de negocio.

Para gestionar recepción, primero se carga `UsuarioGimnasio` por su ID bajo el
filtro tenant; solo entonces se accede a su cuenta global. La FK compuesta impide
que el vínculo apunte a una cuenta de otro gimnasio. Los DTOs no aceptan tenant,
rol ni IDs de actor elegidos por el solicitante.

**Alternativas:** marcar todas las identidades con `@TenantId` hace imposible el
login global sin un bypass; una EMF por scope o un datasource administrativo distinto
complica este monolito. La separación anterior mantiene la excepción de identidad
explícita y fuera de las interfaces del negocio.

**Reglas de sesión:** OSIV desactivado; sin caché L2 ni query cache. Contexto por
request/scope con limpieza finally; no cambiar el tenant de una sesión abierta,
ni heredar contexto en hilos async. Sin jobs de negocio en esta feature.

## R4. Alta atómica de gimnasio y aprovisionamiento

**Decisión:** un ejecutor de aprovisionamiento interno autoriza SUPERADMIN,
preasigna un UUID de gimnasio, abre un scope tenant de aprovisionamiento y recién
entonces inicia una transacción. En ella crea gimnasio, configuración inicial,
CuentaAcceso y UsuarioGimnasio ADMIN. Un fallo revierte todo. El scope no permite
usar endpoints de negocio ni leer tenants existentes. Tiene una autorización específica
de aprovisionamiento, no el guard ordinario TENANT/ADMIN/RECEPCION ni un bypass general.

**Justificación:** evita persistir entidades tenant con el sentinel de PLATFORM y
mantiene la atomicidad de FR-001 con una sola conexión/transacción. El UUID lo genera
el servidor; no procede de un parámetro de gimnasio enviado a un endpoint de negocio.

**Alternativas:** crear gimnasio y admin en dos commits deja estados parciales;
activar un tenant root para aprovisionar abre una vía de acceso global prohibida.

## R5. Login, JWT y cuentas deshabilitadas

**Decisión:** Spring Security, BCrypt mediante DelegatingPasswordEncoder y login
propio que emite JWT con NimbusJwtEncoder. HS256 explícito, clave aleatoria de al
menos 256 bits inyectada por entorno, issuer/audience configurados y expiración
inicial de 15 minutos. No hay refresh tokens ni registro público en esta feature.

Cada request valida firma, algoritmo, issuer, audience y exp y vuelve a cargar por
sub el estado, rol y gimnasio actuales de la cuenta. No hay caché de esa decisión.
Una cuenta deshabilitada no autoriza solicitudes iniciadas después de confirmar la
operación de deshabilitación, aunque conserve un JWT no vencido. Una operación ya
autorizada en curso no se revierte retroactivamente.

El login no acepta tenant. Respuestas de credenciales fallidas son genéricas;
passwords y tokens no se registran. API bearer sin cookies ni sesión HTTP; CORS
cerrado por defecto. La futura UI requerirá revisar almacenamiento de tokens/CSRF.

**Alternativas:** solo claims del JWT mantienen permisos de cuentas deshabilitadas;
Keycloak y refresh token storage añaden infraestructura fuera de la necesidad actual.

**Fuentes:** [Custom JWT y emisión](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/index.html#oauth2resourceserver-jwt),
[hash de contraseñas](https://docs.spring.io/spring-security/reference/6.5/features/authentication/password-storage.html).

## R6. Precisión monetaria y fechas

**Decisión:** BigDecimal y NUMERIC(19,2), sin valores binarios intermedios.
La API recibe y devuelve dinero como string decimal canónico con dos posiciones.
Se rechazan importes con más precisión; `RoundingMode.UNNECESSARY` impide redondeos
silenciosos. Multiplicar cantidad entera, sumar y restar no exige redondeo.
Los límites de representación se validan antes de persistir.

Duraciones enteras positivas, LocalDate para compra/vencimiento e Instant UTC para
auditoría. Un Clock inyectado con APP_TIME_ZONE, valor local de ejemplo
America/Argentina/Tucuman, fija la fecha de evaluación una vez por operación.
Se usa plusDays/plusWeeks/plusMonths sobre la base definida en la spec; se guardan
precio, duración, unidad y fechas de compra como snapshots inmutables.

**Alternativas:** dinero JSON como número puede perder precisión en clientes;
redondear entradas con más decimales oculta errores. La zona de instalación es
uniforme en la feature 1, no se agrega configuración por gimnasio no solicitada.

**Fuentes:** [NUMERIC exacto](https://www.postgresql.org/docs/17/datatype-numeric.html).

## R7. Consistencia de pagos y cadenas de renovación

**Decisión:** transacciones de servicio con READ COMMITTED y PESSIMISTIC_WRITE sobre
el pedido antes de consultar pagos no anulados. El mismo orden de locks se usa al
cobrar y anular; deuda y estado se calculan, no se guardan como flags mutables.
Anular de nuevo devuelve el recurso existente sin un segundo efecto económico.

Renovar bloquea el antecedente; se verifica que no tenga sucesor y se respalda con
UNIQUE(gimnasio_id, pedido_anterior_id). Un segundo intento da 409. La compra puede
contener un @Embedded DetallePedido en su misma fila: columnas obligatorias garantizan
exactamente una membresía sin una tabla de detalles con cardinalidad arbitraria.

**Alternativas:** optimistic locking y reintentos necesitan más coordinación para
un saldo agregado; SERIALIZABLE para toda la aplicación o locks de tabla son excesivos.
No se introduce un sistema de idempotency keys para repetir compras/pagos: cada
request de creación aceptada es una operación nueva; no se reintenta automáticamente
un cobro ambiguo. Anulación repetida y renovación duplicada sí tienen semántica explícita.

**Fuentes:** [locks de filas](https://www.postgresql.org/docs/17/explicit-locking.html),
[transacciones Spring Data](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html).

## R8. Integridad y evolución del esquema

**Decisión:** UUIDs, FK compuestas (gimnasio_id, referencia_id), unicidad tenant+ID
para destinos y unicidad tenant+DNI. Pago/anulación apuntan al usuario tenant del
actor. No se usan CHECKs que consulten otras filas; el saldo requiere la disciplina
transaccional de R7. Las únicas escrituras del esquema son Flyway, con Hibernate
`ddl-auto=validate`; Flyway incluye el módulo PostgreSQL separado.

V1 inicializa la historia; V2 añade identidad/gimnasio/vínculo tenant/configuración inicial para M1;
las siguientes migraciones incorporan catálogos, clientes, compras y pagos en ese
orden. Las migraciones ya aplicadas no se editan. La prueba de actualización aplica
la versión anterior relevante y luego la nueva, además de construir desde vacío.

**Alternativas:** PK DNI impide corregir identidad y obliga a propagar cambios;
FK solo por ID no protege asociaciones cruzadas. DDL automático contradice la constitución.

**Fuentes:** [FK y unicidad](https://www.postgresql.org/docs/17/ddl-constraints.html),
[Flyway PostgreSQL](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database),
[migraciones versionadas](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/versioned-migrations).

## R9. Errores, documentación y verificación

**Decisión:** ProblemDetail centralizado y RFC 7807 como contrato exigido; incluir
handlers de Security, validación y negocio. RFC 9457 sucede a RFC 7807, pero no se
cambia el contrato del proyecto: se conservan sus campos y application/problem+json.
400 para datos inválidos, 401 para identidad inválida, 403 para rol insuficiente,
404 uniforme para recursos ausentes/ajenos y 409 para conflictos de estado/unicidad.

OpenAPI 3.0.3 describe contratos; springdoc publica la implementación y se compara
con el contrato durante las pruebas. Unit tests de servicios/políticas y Failsafe
para integración real. JaCoCo verifica 80% LINE/COVEREDRATIO en el conjunto de lógica
de negocio. Integración y unitarias producen un reporte combinado sin excluir reglas.

**Alternativas:** respuestas de errores ad hoc y estados 403 para IDs ajenos revelan
información o contradicen la spec; mocks no demuestran aislamiento.

**Fuentes:** [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807),
[ProblemDetail en Framework 6.2](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-ann-rest-exceptions.html).

## R10. Seed, entorno y evidencia pendiente

**Decisión:** seed opt-in con datos YAML de gimnasios, disciplinas y membresías
ficticias, procesados por un cargador genérico. Nombres comerciales solo en datos.
Idempotente por identificadores estables del archivo y sin sobrescribir compras,
pagos o credenciales al reiniciar. SUPERADMIN y primer ADMIN reciben sus credenciales
por entorno; no hay contraseñas en datos YAML. Cada gimnasio recibe su cantidad
máxima inicial desde configuración de aprovisionamiento; ADMIN puede cambiarla.

No se instala infraestructura ni se escribe código de aplicación durante plan.
El host observado tiene Java 17 y Docker operativo; no se detectaron Maven ni
PowerShell. M0 debe proveer Wrapper y Java 21 (host o imagen de herramientas).
Testcontainers 1.21.4 ya contiene el ajuste para Docker Engine 29: intenta API 1.44
antes del fallback. No se añade DOCKER_API_VERSION ni se cambia el BOM preventivamente.
La compatibilidad del daemon local queda por comprobar con el primer IT de M1.
Fuentes: [release 1.21.4](https://github.com/testcontainers/testcontainers-java/releases/tag/1.21.4),
[selección de API en el tag](https://github.com/testcontainers/testcontainers-java/blob/1.21.4/core/src/main/java/org/testcontainers/dockerclient/DockerClientProviderStrategy.java#L412-L430).

Los scripts Spec Kit de PowerShell se resuelven con sus equivalentes de ruta/copia
en esta sesión; no se alteran las opciones instaladas ni se declara que corrieron.

**Evidencia que falta por ejecución:** gate M1 real en Hibernate/Spring Data,
constraints/migraciones, concurrencia, formato de errores y cobertura. No se marcan
estos resultados como aprobados por existir documentación o tests upstream.
