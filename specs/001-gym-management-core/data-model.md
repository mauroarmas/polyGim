# Modelo de datos — Núcleo de gestión multi-gimnasio

Estado: diseño propuesto para revisión; no es una migración ejecutada.
Referencias: [spec](spec.md), [investigación](research.md), [contrato REST](contracts/api.md).

## Convenciones

- PostgreSQL, esquema `public`, UUID como PK. Todos los IDs los genera el servidor.
- Entidades de negocio tenant: columna `gimnasio_id UUID NOT NULL`, `@TenantId`,
  no actualizable, FK a gimnasio y UNIQUE(gimnasio_id,id) donde sea destino de otra FK.
- Identidad global y raíz de gimnasio son la excepción administrativa explícita;
  no constituyen un catálogo global de clientes, compras o pagos.
- Relaciones entre entidades tenant: FK compuesta `(gimnasio_id, referencia_id)`.
  Las entidades se cargan bajo el tenant actual antes de asociarlas.
- Sin borrado físico ni cascadas REMOVE en esta feature. FKs ON DELETE RESTRICT.
- Auditoría con `created_at TIMESTAMPTZ`; mutable agrega `updated_at` y `version BIGINT`
  para optimistic locking. Los timestamps no aceptan valores del cliente.
- Dinero NUMERIC(19,2) / BigDecimal. Montos recibidos y devueltos en JSON como string
  decimal de dos posiciones; se rechaza pérdida de escala o desborde.
- Estados de pago y vigencia son resultados calculados; no columnas enum del pedido.
- Textos se recortan; nombres obligatorios no pueden quedar vacíos. Email normalizado
  con trim y minúsculas, único global incluso para cuentas deshabilitadas.

## Entidades y campos

### Gimnasio — tabla gimnasio (administración global)

| Campo | Tipo / regla |
|---|---|
| id | UUID PK; no puede ser el sentinel interno reservado |
| nombre | VARCHAR(100), obligatorio |
| logo_url | VARCHAR(2048), URI HTTP(S) de un recurso gráfico; sin upload de archivos |
| color_primario | CHAR(7), formato #RRGGBB |
| created_at / updated_at / version | Auditoría y concurrencia de metadatos |

SUPERADMIN crea y consulta estos metadatos. No se habilita acceso a tablas de negocio.
El alta también crea configuración inicial y primer ADMIN atómicamente.

### CuentaAcceso — tabla cuenta_acceso (identidad global privada)

| Campo | Tipo / regla |
|---|---|
| id | UUID PK |
| email | VARCHAR(254), normalizado, obligatorio, UNIQUE |
| password_hash | VARCHAR(255), hash con identificador de PasswordEncoder |
| rol | VARCHAR(20): SUPERADMIN, ADMIN, RECEPCION |
| habilitada | BOOLEAN NOT NULL |
| gimnasio_id | UUID nullable, FK a gimnasio; inmutable tras el alta |
| created_at / updated_at / version | Auditoría y concurrencia |

CHECK: SUPERADMIN tiene gimnasio_id nulo; ADMIN/RECEPCION lo tienen no nulo.
UNIQUE(gimnasio_id,id) habilita la FK del vínculo tenant. No se anotan estos datos
con @TenantId porque el email debe resolverse antes de conocer el gimnasio.
Acceso restringido a servicios internos de identidad y aprovisionamiento; sin endpoint
de listados globales de cuentas, password_hash jamás se serializa.

### UsuarioGimnasio — tabla usuario_gimnasio (tenant)

| Campo | Tipo / regla |
|---|---|
| id | UUID PK, mismo valor que CuentaAcceso.id; persist explícito |
| gimnasio_id | UUID NOT NULL, @TenantId |
| created_at | Auditoría |

FK `(gimnasio_id,id)` → cuenta_acceso(gimnasio_id,id). Un SUPERADMIN no puede tener
este vínculo porque su gimnasio es nulo. Sirve como referencia de actores y como
frontera de acceso para crear/deshabilitar recepción: primero se resuelve el vínculo
propio y luego se modifica exclusivamente la CuentaAcceso vinculada.
No duplica email, hash, rol o habilitación. Es una asociación técnica, no otro rol.

### ConfiguracionGimnasio — tabla configuracion_gimnasio (tenant)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUID; un registro por gimnasio, UNIQUE(gimnasio_id) |
| cantidad_maxima_compra | INTEGER positivo |
| created_at / updated_at / version | Auditoría y concurrencia |

Se inicializa desde DEFAULT_GYM_MAX_QUANTITY al aprovisionar; luego ADMIN la cambia.
No incluye todavía cupos ni políticas de reservas/cancelación de la feature 2.

### CanalLlegada, Localidad y Disciplina — tablas tenant independientes

Cada tabla tiene `id`, `gimnasio_id`, `nombre VARCHAR(100)` obligatorio y auditoría/version.
Nombre único dentro de su propio catálogo y gimnasio, comparado tras normalización
case-insensitive para evitar duplicados por mayúsculas. La migración crea el índice
funcional correspondiente. No hay catálogo de localidades compartido entre tenants.
No se permite borrar elementos referenciados; esta feature solo crea/edita/consulta.

### Membresia — tabla membresia (tenant)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUIDs de identidad y pertenencia |
| disciplina_id | UUID, FK compuesta a disciplina |
| nombre | VARCHAR(100), no vacío |
| precio | NUMERIC(19,2), CHECK > 0 |
| duracion | INTEGER, CHECK > 0 |
| unidad_duracion | VARCHAR(10), CHECK DIAS/SEMANAS/MESES |
| created_at / updated_at / version | Auditoría y concurrencia |

No se deduce duración del nombre. Se permite cambiar nombre/precio/duración para
nuevas ventas. El vínculo de disciplina se fija al crear para no reinterpretar
la disciplina de compras existentes; un plan de otra disciplina se crea como otro registro.

### Cliente con Direccion embebida — tabla cliente (tenant)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUIDs |
| dni | VARCHAR(8), regex siete u ocho dígitos, UNIQUE(gimnasio_id,dni) |
| nombre / apellido | VARCHAR(100), obligatorios y no vacíos |
| telefono | VARCHAR(40), nullable |
| canal_llegada_id | UUID nullable, FK compuesta a canal_llegada |
| calle | VARCHAR(160), nullable |
| altura | VARCHAR(20), nullable; admite números alfanuméricos o sin número |
| localidad_id | UUID nullable, FK compuesta a localidad |
| fecha_ingreso | DATE, asignada al registrar el cliente |
| created_at / updated_at / version | Auditoría y concurrencia |

`Direccion` es un value object embebido: no tiene identidad ni tabla propia porque
no se comparte entre clientes en el alcance aprobado. Los campos opcionales no
se reemplazan por datos ficticios ni por el canal "otro" automáticamente.
La edición conserva id, pertenencia y compras; no puede cambiar gimnasio_id.

### Pedido con DetallePedido embebido — tabla pedido (tenant)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUIDs |
| cliente_id / membresia_id | UUIDs obligatorios, FK compuestas propias |
| cantidad | INTEGER, CHECK > 0; service compara con máximo del gimnasio |
| precio_unitario | NUMERIC(19,2), snapshot, CHECK > 0 |
| duracion_membresia / unidad_duracion | Snapshot de duración positiva y unidad de catálogo |
| total | NUMERIC(19,2), CHECK > 0; coincide con cantidad × precio_unitario |
| fecha_compra | DATE, hoy por defecto, admite pasado; no futuro |
| fecha_base_vencimiento | DATE, compra o vencimiento del antecedente |
| fecha_vencimiento | DATE, fecha base + cantidad × duración en la unidad |
| pedido_anterior_id | UUID nullable, FK compuesta a pedido |
| registrado_por_id | UUID, FK compuesta a usuario_gimnasio |
| created_at | Instant de registro, diferente de la fecha comercial retroactiva |

DetallePedido es un @Embedded con las columnas de membresía, cantidad y snapshots.
No hay tabla de detalle con cardinalidad múltiple: los NOT NULL garantizan un detalle.
Esto conserva el concepto del dominio y simplifica el requisito de una sola línea.

UNIQUE(gimnasio_id,pedido_anterior_id), permitiendo nulos para compras nuevas,
impide dos sucesores del mismo antecedente. La fila antecedente se bloquea antes
de verificar/crear el sucesor. El servicio exige mismo cliente y membresía y una
fecha comercial que no preceda a la de su antecedente; no admite ciclos.

No existe endpoint de edición ni borrado de pedidos. El agregado comercial es
inmutable desde el alta, por lo que también respeta la prohibición de editar con pagos.
No se usan columnas `estado_pago`, `deuda` ni `vigencia` como fuentes de verdad.

### Pago — tabla pago (tenant, movimiento inmutable)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUIDs |
| pedido_id | UUID, FK compuesta a pedido |
| monto_aplicado | NUMERIC(19,2), CHECK > 0 |
| registrado_por_id | UUID, FK compuesta a usuario_gimnasio |
| created_at | Timestamp de registro |

No guarda el monto entregado ni el vuelto como pagos adicionales. La respuesta de
creación informa aplicado y vuelto; el historial conserva el importe aplicado.
Un CHECK no verifica sumas de otras filas: el lock de pedido serializa altas y anulaciones.

### AnulacionPago — tabla anulacion_pago (tenant, registro inmutable)

| Campo | Tipo / regla |
|---|---|
| id / gimnasio_id | UUIDs |
| pago_id | UUID, UNIQUE(gimnasio_id,pago_id), FK compuesta a pago |
| anulado_por_id | UUID, FK compuesta a usuario_gimnasio, ADMIN autorizado |
| created_at | Momento de la anulación |

La presencia del registro hace que el pago se informe ANULADO; su ausencia,
REGISTRADO. No hay borrado ni reactivación. Repetir PUT de anulación devuelve la
misma evidencia. Se bloquea el pedido padre antes de revisar/crear la anulación.

## Estados y cálculos

| Concepto | Regla |
|---|---|
| Pagos válidos | Suma de Pago.monto_aplicado sin AnulacionPago |
| Deuda | Pedido.total − pagos válidos; nunca negativa |
| Estado de pago | PENDIENTE con deuda > 0, PAGADO con deuda = 0 |
| Vigencia | VIGENTE si fecha de evaluación ≤ vencimiento; VENCIDA desde el día siguiente |
| Pago recibido | Debe ser > 0 y existir deuda; aplicado = min(recibido,deuda) |
| Vuelto | Recibido − aplicado, informado en la respuesta, sin saldo a favor |
| Renovación vigente | Base = vencimiento anterior según fecha_compra del nuevo pedido |
| Renovación vencida | Base = nueva fecha_compra |

Todas las compras tienen fecha comercial ≤ hoy. Para cierre de mes se usa el último
día válido; los cambios del catálogo o los pagos no recalculan las fechas guardadas.

## Índices y límites de consistencia

- PK por UUID y UNIQUE tenant+ID en los destinos de FKs compuestas.
- Cliente: UNIQUE(gimnasio_id,dni); búsqueda/listado por gimnasio y apellido/id.
- Membresia: índice (gimnasio_id,disciplina_id).
- Pedido: índices (gimnasio_id,cliente_id,created_at,id) y sucesor único por antecedente.
- Pago: índice (gimnasio_id,pedido_id,created_at,id); anulación única por pago.
- Configuración: gimnasio único. CuentaAcceso: email único global.
- Los índices de listados siguen los filtros/paginación efectivos, sin agregar
  búsquedas de texto o tablas de auditoría generales fuera del alcance.
- Actualizar catálogos/clientes usa @Version y devuelve 409 si compiten escrituras.
  No se promete detección de edición basada en una pantalla desactualizada sin token de versión.

## Migraciones por hito

| Versión | Introducción |
|---|---|
| V1 | Historia inicial vacía, sin DDL automático |
| V2 | gimnasio, cuenta_acceso, usuario_gimnasio, configuracion_gimnasio y constraints de identidad/tenant para M1 |
| V3 | canal_llegada, localidad, disciplina, membresia |
| V4 | cliente y sus datos de dirección embebidos |
| V5 | pedido y detalle embebido, snapshots y cadena de renovaciones |
| V6 | pago y anulacion_pago con referencias a actores |

Son contratos de evolución, no archivos SQL escritos en esta etapa. El seed es DML
opt-in del cargador de datos, no cambios del esquema ni credenciales en migraciones.
