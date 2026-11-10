# PLAN_DE_TRABAJO.md — PolyGym

> **Para quién:** el agente de código y el autor. **Cómo se usa:** es la fuente de verdad de *producto* (qué se construye, en qué orden y cuándo está terminado). Spec Kit genera sus propios `spec.md`, `plan.md` y `tasks.md` por feature: deben ser coherentes con este documento. Si hay contradicción, se frena y se consulta al autor; no se resuelve en silencio.
> **Relacionados:** `polygim-api/legacy/DOMINIO.md` (dominio y reglas heredadas), `polygim-api/legacy/schema.sql` (esquema viejo, solo referencia), `original/` (código viejo, solo referencia).

## 1. Visión

Plataforma web multi-gimnasio (multi-tenant) para gestionar clientes, membresías, pedidos, pagos y, luego, reservas de clases con cupo. Nació de una app de escritorio hecha para el gimnasio Poly, que es el primer tenant. Objetivos: (1) producto adaptable que se pueda mostrar y vender a otros gimnasios; (2) proyecto de portfolio defendible en una entrevista técnica.

**Principio rector:** simple y justificado antes que sofisticado. Cada decisión debe poder explicarse en una entrevista.

## 2. Stack (todo Java + Spring)

Java 21 · Spring Boot 3 · Spring Data JPA (Hibernate 6, `@TenantId`) · Spring Security · Thymeleaf + HTMX · PostgreSQL · Flyway · Maven · Docker Compose · Testcontainers · JUnit 5 · Mockito · JaCoCo · springdoc-openapi · GitHub Actions. Sin frontend JavaScript separado.

La validación concreta de `@TenantId` (versión, comportamiento con consultas nativas, integración con Security) se resuelve en el `plan` de la feature 1, con una prueba mínima antes de construir sobre ella.

## 3. Reglas transversales (innegociables)

1. **Aislamiento entre gimnasios:** ningún dato de un tenant es visible ni modificable desde otro. Se aplica de forma automática y se prueba con tests de integración. **Es lo primero que se construye y se testea.**
2. **Dinero en `BigDecimal`** (escala 2). Nunca `double`.
3. **Sin credenciales en el código.** Todo por variables de entorno; `.env.example` sin valores reales.
4. **Nada de Poly en el código.** Sus disciplinas y membresías son datos de seed.
5. **Reglas de negocio configurables por gimnasio**, nunca constantes.
6. **Arquitectura en capas** (controller → service → repository). La lógica de negocio vive en services y se testea de forma unitaria.
7. **Migraciones solo con Flyway.** Nunca DDL manual.
8. **Errores en formato RFC 7807** (`application/problem+json`).
9. **Cobertura mínima 80%** en la lógica de negocio (JaCoCo en CI).
10. **No se copia código de `original/`.** Se lee la regla, se entiende y se reescribe.

## 4. Decisiones de dominio vigentes

| Tema | Decisión |
|---|---|
| Estado de pago | `PENDIENTE` / `PAGADO`, derivado de pagos vigentes (no anulados) vs total |
| Vigencia | `VIGENTE` / `VENCIDA`, calculada por fecha; el día de vencimiento está incluido |
| Pago parcial | No altera la vigencia; la deuda se rastrea aparte |
| Pedido | En la feature 1, un pedido = un detalle (una membresía). Varias disciplinas = pedidos separados |
| Duración | Explícita en la membresía (`duracion` + `unidad`: DIAS, SEMANAS, MESES). Vencimiento = inicio + cantidad × duración |
| Inicio de vigencia | Fecha de compra (por defecto hoy). Renovación: continúa desde el vencimiento anterior si sigue vigente |
| Precio | El precio de la membresía debe ser mayor a cero; no se admiten membresías gratuitas en la feature 1. El detalle congela el precio unitario al registrar la compra. En compras retroactivas se usa el precio vigente al registrarlas, sin reconstruir un catálogo histórico |
| Historial comercial | Cambiar el precio, nombre o duración del catálogo no modifica precio congelado, total ni vencimiento de compras existentes |
| Fecha de compra | Por defecto hoy; se permiten fechas pasadas y se rechazan futuras |
| Selección de renovación | Se elige una compra previa del mismo gimnasio, cliente y membresía; no se selecciona automáticamente por disciplina |
| Cadena de renovaciones | Cada compra admite una sola renovación; se continúa desde la última compra de la cadena. Si el antecedente ya fue renovado se rechaza, incluso ante solicitudes simultáneas |
| Cálculo de renovación | La vigencia del antecedente se evalúa respecto de la fecha de compra registrada: si sigue vigente se suma desde su vencimiento; si venció se suma desde esa fecha de compra |
| Calendario | Semanas de siete días; al sumar meses se conserva el día o se usa el último día válido del mes destino |
| Edición | Un pedido con pagos registrados no se edita, aunque se hayan anulado. La edición de pedidos sin pagos no se requiere en la feature 1 |
| Pagos | Monto recibido y aplicado > 0. No se cobra un pedido sin deuda. Si el monto recibido supera la deuda, se guarda solo la deuda y se informa el vuelto |
| Anulaciones | Solo ADMIN del gimnasio; no se borra el pago original. Se registra quién anuló y cuándo; repetir la anulación no duplica su efecto económico |
| Consistencia | Cobros simultáneos no superan el total aplicado. Altas, compras, pagos y anulaciones no dejan efectos parciales ante un fallo |
| DNI | `String`, 7 u 8 dígitos, único por gimnasio |
| Datos del cliente | DNI, nombre y apellido obligatorios y no vacíos; teléfono, cómo llegó, dirección y localidad opcionales. La ausencia de esos datos opcionales no impide registrar ni vender una membresía |
| Cantidad máxima | Configurable por gimnasio (los rangos 1-12 meses / 1-3 semanas del legacy no son requisito) |
| "Cómo llegó" | Catálogo configurable por gimnasio (seed: recomendación, iba pasando, redes sociales, promociones, otro) |
| Cuentas | Una cuenta pertenece a un solo gimnasio (salvo SUPERADMIN). Se accede con email único global y contraseña, sin indicar gimnasio. La unicidad incluye SUPERADMIN y cuentas deshabilitadas; no se reasigna ni reutiliza el email de otra cuenta |
| Personal de recepción | ADMIN crea y deshabilita cuentas RECEPCION propias; una cuenta deshabilitada no puede operar aunque tuviera una sesión previa |

**Roles:** `SUPERADMIN` gestiona gimnasios y sus primeros administradores, sin acceso a datos de negocio. `ADMIN` hace todo dentro de su gimnasio. `RECEPCION` opera clientes, pedidos y pagos, sin configurar ni anular pagos. `CLIENTE` aparece en la feature 2.

## 5. Roadmap de features

Un ciclo completo de Spec Kit por feature: `specify → clarify → plan → tasks → implement`. No se empieza una feature sin cerrar la anterior (Definición de hecho, sección 8).

### Feature 1 — Núcleo multi-tenant (API REST)

**Objetivo:** un gimnasio puede operar su negocio de membresías y cobros por API, aislado de los demás.

**Historias de usuario**
- **H1.1** Como SUPERADMIN, doy de alta un gimnasio (nombre, logo, color) y su primer ADMIN.
- **H1.2** Como usuario, me autentico y solo veo datos de mi gimnasio.
- **H1.3** Como ADMIN, configuro disciplinas, membresías (precio, duración) y el catálogo de "cómo llegó".
- **H1.4** Como ADMIN o RECEPCION, registro y edito clientes (dni, nombre, apellido, teléfono, cómo llegó, dirección y localidad).
- **H1.5** Como ADMIN o RECEPCION, creo un pedido de una membresía para un cliente con total calculado y precio congelado.
- **H1.6** Como ADMIN o RECEPCION, registro pagos (incluidos parciales) y veo estado de pago y vigencia.
- **H1.7** Como ADMIN, anulo un pago y el estado se recalcula.
- **H1.8** Como ADMIN, creo y deshabilito cuentas de RECEPCION de mi gimnasio.
- **H1.9** Como responsable del producto, dispongo de un seed inicial de Poly con Funcional, Boxeo y Crossfit y membresías y precios ficticios, reemplazables como datos.

**Criterios de aceptación clave**
- Un usuario de un gimnasio recibe 404 (no 403) al pedir un recurso de otro.
- Dos clientes con el mismo DNI son válidos en gimnasios distintos y rechazados en el mismo.
- Total, vencimiento y estado de pago se calculan en el service y están cubiertos por tests unitarios con casos borde (pago exacto, parcial, excedido, anulado, vencimiento en el día).
- RECEPCION recibe 403 al anular un pago o editar configuración.
- Un pedido con pagos rechaza la edición.
- Montos ≤ 0 se rechazan con RFC 7807.

**Fuera de alcance:** reservas, listado de vencimientos próximos, pantallas web, importación de datos.

### Feature 2 — Reservas de clases con cupo

**Historias:** ADMIN define clases (disciplina, día, hora, cupo); CLIENTE o personal reserva y cancela; ADMIN o RECEPCION lista las membresías por vencer en los próximos 7 días.

**Reglas**
- Solo se reserva con membresía vigente de esa disciplina (y, opcionalmente, pago completo: configurable por gimnasio).
- Nunca más reservas que el cupo, incluso con pedidos simultáneos.
- Un cliente no puede tener dos clases solapadas el mismo día.
- Se cancela hasta N horas antes (N configurable por gimnasio; valor inicial 2).

**Criterio estrella:** test de concurrencia que lanza varias reservas simultáneas sobre el último lugar y verifica que solo una prospera. La elección de locking (optimista o pesimista) se justifica por escrito en el `plan`.

### Feature 3 — Interfaz web (Thymeleaf + HTMX)

Panel para ADMIN y RECEPCION (clientes, pedidos, pagos, clases) y vista de reservas para CLIENTE. Branding por gimnasio (nombre, logo, color primario). Responsive, usable desde el celular en recepción.

### Feature 4 — Demo pública y deploy

Gimnasio de demostración con datos ficticios que se restablece periódicamente, reutilización del seed inicial incorporado en la feature 1, despliegue con Docker Compose en un VPS, README con capturas o GIF, diagrama de multi-tenancy y link a la demo.

## 6. Orden de implementación de la feature 1 (rebanadas verticales)

Cada hito termina con tests en verde y un commit convencional. No se avanza con tests rojos.

| Hito | Contenido | Se considera terminado cuando |
|---|---|---|
| **M0** Base | Proyecto Maven, Docker Compose con PostgreSQL, Flyway V1 vacía, `.env.example`, manejo de errores RFC 7807, CI con build y tests | `docker compose up` + build local y CI en verde |
| **M1** Tenant y seguridad | Entidad Gimnasio, Usuario y roles, login con JWT, `@TenantId` con tenant tomado del usuario autenticado | **Test de aislamiento** entre dos gimnasios pasando, antes de modelar nada más |
| **M2** Superadmin | Alta de gimnasio y primer ADMIN | H1.1 con tests de integración |
| **M3** Catálogos | Disciplina, Membresía (precio, duración, unidad), "cómo llegó", localidad | H1.3, con validaciones y permisos por rol |
| **M4** Clientes | CRUD, DNI único por gimnasio, dirección | H1.4 + test de DNI duplicado entre tenants |
| **M5** Pedidos | Creación con un detalle, precio congelado, total en `BigDecimal`, vigencia calculada | H1.5, tests unitarios del cálculo |
| **M6** Pagos | Registrar, parcial, exceso con vuelto informado, anular, recálculo de estado | H1.6 y H1.7 con tabla de casos borde |
| **M7** Cierre | Seed de Poly, OpenAPI documentado, README, cobertura ≥ 80% | Definición de hecho de la sección 8 |

## 7. Estrategia de pruebas

- **Unitarias (JUnit 5 + Mockito):** cálculo de total, vencimiento, estado de pago, reglas de pagos.
- **Integración (Testcontainers + PostgreSQL real):** aislamiento entre tenants, permisos por rol, migraciones Flyway, constraints.
- **Concurrencia (feature 2):** reservas simultáneas sobre el último cupo.
- **Datos de prueba:** al menos dos gimnasios en todo test de integración, para que un fallo de aislamiento sea visible.
- **CI:** build, tests y reporte de cobertura en cada push.

## 8. Definición de hecho (por feature)

- [ ] Criterios de aceptación cumplidos y verificados con tests
- [ ] Tests unitarios e integración en verde en CI
- [ ] Cobertura ≥ 80% en lógica de negocio
- [ ] Aislamiento entre tenants probado
- [ ] OpenAPI actualizado
- [ ] README actualizado (cómo correr, ejemplo de request/response)
- [ ] Sin credenciales ni referencias a Poly en el código
- [ ] Spec, plan y tasks de Spec Kit coherentes con este documento
- [ ] Dato real para el CV anotado (cantidad de tests, cobertura)

## 9. Cómo trabaja el agente

1. Lee este documento, `polygim-api/legacy/DOMINIO.md` y los artefactos de Spec Kit de la feature en curso antes de escribir código.
2. Trabaja **un hito por vez**, con commits pequeños y convencionales.
3. Escribe primero los tests de las reglas de negocio; después el código.
4. Corre los tests antes de dar un hito por terminado y reporta el resultado real.
5. Ante ambigüedad o contradicción entre documentos, **pregunta**; no asume.
6. No amplía el alcance: lo que está fuera de alcance se anota, no se construye.
7. Si descubre una regla del legacy no documentada, la propone para `DOMINIO.md` en vez de implementarla directamente.

## 10. Riesgos y puntos a vigilar

| Riesgo | Mitigación |
|---|---|
| `@TenantId` no cubre consultas nativas o jobs sin usuario autenticado | Prueba mínima en M1; revisar cada consulta nativa |
| Fuga de datos entre tenants por un `WHERE` olvidado | Aislamiento automático + tests con dos gimnasios |
| Ampliar el alcance (cobros online, facturación, molinetes) | Mantenerlos fuera hasta cerrar las 4 features |
| Estado de pago inconsistente al anular pagos | Estado derivado de la suma de pagos vigentes, nunca guardado a mano |
| Que el agente replique el legacy (double, DNI int, vencimiento por nombre) | Reglas transversales 2, 6 y 10 en la constitution |

## 11. Registro de avance

Completar al cerrar cada hito.

| Hito | Estado | Tests | Cobertura | Notas |
|---|---|---|---|---|
| M0 | Base local verificada; CI remoto pendiente | 4 unitarios de contrato HTTP, 0 fallos (`clean verify`) | Sin clases de negocio aún; no corresponde informar porcentaje | Java 21, Wrapper, imagen app y PostgreSQL con healthcheck verificados; workflow creado, no ejecutado en GitHub |
| M1 | Gate local aprobado | 18 unitarios y 13 de integración, 0 fallos (`clean verify`); regresión adicional de 10 pruebas HTTP de seguridad | 85,11% de líneas actuales de servicio/modelo (40/47) | PostgreSQL 17.11/Testcontainers, Flyway V1→V2, aislamiento A/B por ID/list/count y repositorio, restricciones tenant, JWT y cuenta deshabilitada; CI remoto pendiente |
| M2 | Gate local aprobado | 26 unitarios y 22 de integración, 0 fallos (`clean verify`) | 95,95% de líneas actuales de servicio/modelo (71/74) | Alta atómica de gimnasio y primer ADMIN; recepción propia, revocación inmediata, IDs ajenos y permisos probados; CI remoto pendiente |
| M3 | Gate local aprobado | 31 unitarios y 24 de integración, 0 fallos (`clean verify`) | 97,32% de líneas actuales de servicio/modelo (145/149) | V3; catálogos, membresías y máximo por gimnasio; aislamiento, permisos y dinero canónico probados; CI remoto pendiente |
| M4 | Gate local aprobado | 34 unitarios y 27 de integración, 0 fallos (`clean verify`) | 97,57% de líneas actuales de servicio/modelo (201/206) | V4; alta y edición de clientes, DNI textual único por gimnasio, opcionales y referencias aisladas; CI remoto pendiente |
| M5 | Gate local aprobado; saldos completos pendientes de M6 | 39 unitarios y 31 de integración, 0 fallos (`clean verify`) | 97,99% de líneas actuales de servicio/modelo (292/298) | V5; snapshots y renovaciones concurrentes verificadas; todavía no hay pagos y la deuda inicial es el total; CI remoto pendiente |
| M6 | Parcial: cobros verificados; anulación pendiente | 42 unitarios y 35 de integración, 0 fallos (`clean verify`) | 95,94% de líneas actuales de servicio/modelo (331/345) | V6; pagos parciales, exceso/vuelto, deuda real y cobros concurrentes probados; falta US7 y CI remoto |
| M7 | ☐ | | | |
