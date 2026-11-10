# Contrato REST — Feature 1

Propuesta revisable. Contrato machine-readable: [openapi.yaml](openapi.yaml).
Modelo e invariantes: [data-model.md](../data-model.md).

## Reglas comunes

- Base `/api/v1`; JSON UTF-8, identificadores UUID y fechas comerciales ISO YYYY-MM-DD.
- Dinero como string exacto con dos decimales, por ejemplo `"200.50"`; no números
  JSON binarios. Se admiten 17 dígitos enteros; montos con otra escala o fuera de
  NUMERIC(19,2) producen 400. Membresías y montos recibidos deben ser > 0.
- `gimnasioId`, actor, rol, importes calculados, estado y fechas de auditoría no son
  campos de entrada del negocio. Propiedades desconocidas producen 400.
- Tenant de la cuenta vigente validada, nunca de header, query, path ni body.
- Todas las operaciones salvo login requieren bearer JWT; no hay cookies ni sesión HTTP.
- Login por email global normalizado/contraseña. Crear una cuenta con email ya usado
  produce 409 genérico, sin revelar existencia de otro gimnasio o su identidad.
- Una cuenta deshabilitada no puede autorizar nuevas solicitudes aunque el token no
  haya vencido. No se modifica una operación ya autorizada y en curso.
- Colecciones paginadas: page desde 0, size por defecto 20, máximo 100. Orden estable
  definido por el servidor; sin selección de columnas o sort arbitrario del cliente.
- No hay endpoints para borrar clientes, catálogos, pedidos o pagos, editar pedidos,
  activar otra vez cuentas ni gestionar cuentas CLIENTE en esta feature.

## Matriz de operaciones

| Recurso / operación | SUPERADMIN | ADMIN | RECEPCION |
|---|---|---|---|
| Login / identidad propia | Sí | Sí | Sí |
| Crear/listar/consultar metadatos de gimnasios y primer ADMIN | Sí | No | No |
| Crear/deshabilitar recepción propia | No | Sí | No |
| Leer configuración y catálogos propios | No | Sí | Sí |
| Editar cantidad máxima y crear/editar catálogos propios | No | Sí | No |
| Registrar/editar/listar/consultar clientes propios | No | Sí | Sí |
| Crear/renovar/listar/consultar pedidos propios | No | Sí | Sí |
| Registrar/consultar pagos propios | No | Sí | Sí |
| Anular pagos propios | No | Sí | No |

Primero se comprueba identidad y permiso de operación, después se resuelven recursos
bajo el tenant. Una operación prohibida devuelve 403 sin consultar si el ID existe;
una operación permitida que referencia un ID ajeno responde 404 igual que un ID
inexistente. Esta precedencia evita revelar información sobre el recurso.

## Contratos particulares

### Autenticación y aprovisionamiento

- POST `/auth/login`: email/password → token bearer de 15 minutos y usuario actual.
- GET `/auth/me`: identidad actual, sin hash ni otros usuarios.
- POST `/plataforma/gimnasios`: nombre, logoUrl HTTP(S), colorPrimario #RRGGBB y
  administradorInicial {email,password} → 201 gimnasio y primer administrador.
  El alta completa se revierte si falla alguna parte. La cantidad máxima inicial
  se toma de configuración de aprovisionamiento, no de una constante de cliente.
- GET `/plataforma/gimnasios` y `/{id}`: solo metadatos administrativos.
- POST `/usuarios-recepcion`: {email,password} → 201 usuario RECEPCION del gimnasio.
- PUT `/usuarios-recepcion/{id}/deshabilitacion`: sin body → 200 usuario deshabilitado;
  repetición sin efectos nuevos. ID de otro gimnasio o que no sea recepción: 404.

La política inicial de passwords de cuentas nuevas es 12–72 caracteres y como máximo
72 bytes UTF-8 por BCrypt; se documenta y valida sin truncar contraseñas. El password
solo aparece en requests de alta/login, nunca en responses o logs.

### Clientes y catálogos

- POST/GET `/clientes`, GET/PUT `/clientes/{id}`. DNI, nombre y apellido son obligatorios;
  teléfono, canalLlegadaId, direccion y localidadId son opcionales. PUT reemplaza los
  datos editables completos: omitir opcionales los limpia. FechaIngreso es de salida.
- `direccion` contiene calle/altura opcionales; localidadId se suministra por separado.
- GET clientes permite filtro exacto `dni`; mismo DNI en otro gimnasio no es conflicto.
- POST/GET `/disciplinas`, `/canales-llegada`, `/localidades`; GET/PUT de cada `/{id}`.
- POST/GET `/membresias`, GET/PUT `/membresias/{id}`. Alta incluye disciplinaId; edición
  modifica nombre/precio/duracion/unidadDuracion, sin mover el plan de disciplina.
- GET membresías admite disciplinaId. Un filtro por ID ajeno produce 404.
- GET/PUT `/configuracion`: cantidadMaximaCompra positiva, solo ADMIN puede cambiarla.

### Pedidos y renovaciones

POST `/pedidos` recibe clienteId, membresiaId, cantidad, fechaCompra opcional y
pedidoAnteriorId opcional. No recibe una lista de detalles, precio, total ni tenant.
Usa el precio y duración actuales al registrar, incluso con fecha comercial pasada.

La renovación exige mismo cliente y membresía, antecedente cronológicamente previo
y sin sucesor. Si hay dos intentos, uno como máximo produce 201 y el restante 409.
La fecha de referencia es fechaCompra; no se cambia la decisión por consultar otro día.
GET `/pedidos` permite clienteId propio; GET `/{id}` devuelve snapshots, fechas,
deuda, estadoPago y vigencia calculados. El historial se consulta por `/pedidos/{id}/pagos`.
No hay endpoint para modificar un pedido, tampoco antes de registrar pagos.

### Pagos y anulaciones

POST `/pedidos/{id}/pagos` recibe montoRecibido positivo. Devuelve 201 con pago,
vuelto, deudaRestante y estadoPago. El pago solo guarda el montoAplicado a la deuda.
Deuda cero produce 409, no un pago cero. La transacción bloquea el pedido antes de
recalcular la deuda; el monto aplicado nunca excede lo pendiente.

GET `/pedidos/{id}/pagos` lista pagos y evidencia de anulación; GET `/pagos/{id}`
consulta uno propio. PUT `/pagos/{id}/anulacion`, sin body, devuelve 201 al crear la
anulación y 200 con la misma evidencia si ya estaba anulada. Solo ADMIN.
No hay DELETE, reactivación ni modificación del importe del movimiento.

Un POST aceptado es una nueva operación de compra o cobro: no hay deduplicación por
un identificador de reintento. Si se pierde la respuesta, consultar el historial antes
de decidir repetir; los clientes no reintentan automáticamente POST financieros.
La idempotencia de PUT de anulación y la unicidad de sucesor son garantías separadas.

## Errores RFC 7807

Content-Type application/problem+json. Siempre type, title, status, detail e instance;
extensiones code y, cuando corresponda, errors con field/message. No incluir SQL,
trazas, emails de otros usuarios, contraseñas, tokens o detalles de constraints internos.

| HTTP | Ejemplos |
|---|---|
| 400 | Formato inválido, propiedades desconocidas, DNI inválido, campos obligatorios vacíos, precio/monto no positivo, cantidad o fechas inválidas, overflow monetario/temporal |
| 401 | Falta token, credenciales erróneas, token inválido/expirado o cuenta deshabilitada |
| 403 | Rol sin permiso para la operación |
| 404 | Recurso inexistente, ajeno, relación ajena o recepción no resoluble dentro del gimnasio |
| 409 | DNI/email/nombre de catálogo duplicado, antecedente ya renovado, pedido sin deuda, conflicto de versión |
| 405 / 415 / 500 | Método no admitido / formato de contenido no admitido / error interno genérico |

Ejemplo ficticio:

```json
{
  "type": "urn:gym-api:problem:not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "No se encontró el recurso solicitado.",
  "instance": "/api/v1/clientes/00000000-0000-0000-0000-000000000001",
  "code": "RESOURCE_NOT_FOUND"
}
```

## Verificación del contrato

Pruebas de controlador y Security comprueban estados, DTOs y problem+json, incluidos
401/403 generados fuera de ControllerAdvice. Pruebas con dos tenants comprueban cada
lookup y relación, filtros de colecciones, cuenta deshabilitada con token vigente y
roles. La documentación springdoc se compara con este inventario y sus schemas,
sin exigir igualdad byte a byte de ejemplos o campos generados.
