# DOMINIO.md — Referencia del sistema legacy (polyGim)

> **Uso:** referencia de dominio para la reescritura. Se hereda el *qué* (entidades y reglas), nunca el *cómo* (arquitectura, UI Swing, acceso JDBC).
> **Origen:** versión de escritorio (Java Swing + MariaDB), escrita en 2023-2024 para el gimnasio Poly, primer cliente. Documento derivado de la lectura del código; las reglas marcadas **[OBSERVADO]** salen del código y las marcadas **[DECISIÓN]** son cambios para la versión nueva.

## 1. Contexto de negocio

Gimnasio que ofrece clases de **funcional, boxeo y crossfit**. El dominio comprende clientes, venta de membresías, cobros parciales y vencimientos. **[OBSERVADO]** El alta de clientes está incompleta en los controladores y el acceso a datos revisados; no se encontró un listado de próximos vencimientos. El legacy no tenía horarios, clases ni reservas.

**[DECISIÓN]** La versión nueva es un **producto multi-gimnasio**: Poly es el primer tenant y su nombre, disciplinas y membresías son datos de seed, nunca constantes de negocio en el código.

**[DECISIÓN]** La paridad con el legacy es **de dominio, no funcional**: se conservan los conceptos y las reglas acordadas, sin exigir equivalencia con funcionalidades incompletas ni reproducir errores del sistema anterior.

## 2. Glosario

| Término | Significado |
|---|---|
| Servicio | Disciplina que ofrece el gimnasio (funcional, boxeo, crossfit) |
| Membresía | Plan de un servicio con precio y duración (ej. Mensual) |
| Pedido | Compra por un cliente; en la feature 1 contiene un único detalle (una membresía) |
| Detalle | Línea de un pedido: membresía + cantidad |
| Pago | Monto abonado contra un pedido (puede ser parcial) |
| Estado de pago | Situación de cobro del pedido: PENDIENTE o PAGADO, separada de la vigencia |
| Vigencia | Situación temporal de la membresía: VIGENTE o VENCIDA, calculada por fecha |
| Vencimiento | Fecha hasta la cual el cliente tiene la membresía vigente |

## 3. Modelo de datos legacy [OBSERVADO]

```
Localidad(idLocalidad, localidad)
Direccion(idDireccion, calle, altura, idLocalidad→Localidad)
Cliente(dni PK, apellido, nombre, telefono, fecha_ingreso, como_llego, idDireccion→Direccion)
Servicio(idServicio, servicio)
Membresia(idMembresia, membresia, precio, idServicio→Servicio)
Pedido(idPedido, fecha_pedido, estado, fecha_vencimiento, clienteDni→Cliente, total)
Detalle(idDetalle, idPedido→Pedido, idMembresia→Membresia, cantidad, subtotal)
Pago(idPago, montoPago, idPedido→Pedido)
```

Relaciones: un Servicio tiene muchas Membresías; un Cliente tiene una Dirección y muchos Pedidos; un Pedido tiene muchos Detalles y muchos Pagos.

`como_llego` es el canal por el que el cliente conoció el gimnasio (dato de marketing).

## 4. Reglas de negocio observadas

1. **[OBSERVADO] Subtotal de un detalle** = `cantidad × precio de la membresía` (`modelo/Detalle.java`, `setSubtotal`).
2. **[OBSERVADO] Total del pedido** = suma de los subtotales de sus detalles (`modelo/Pedido.java`, `calcularSumaSubtotales`).
3. **[OBSERVADO] Vencimiento** = `fecha del pedido + cantidad` en meses si la membresía se llama "Mensual", y en semanas en cualquier otro caso (`modelo/Pedido.java`, `obtenerFechaVen`). La duración depende del texto del nombre.
4. **[OBSERVADO] Estado del pedido:** `0 = IMPAGO`, `1 = PAGADO`. El código revisado no define un estado de vigencia separado.
5. **[OBSERVADO] Pagos:** se compara el monto con la deuda (`total − suma de pagos previos`) en `controlador/controladorPago.java`.
   - Si `monto < deuda`: se registra el pago parcial y el pedido sigue impago.
   - Si `monto == deuda`: se registra y el pedido pasa a pagado.
   - Si `monto > deuda`: se calcula el vuelto, **la base registra solo la deuda**, pero **la lista en memoria registra todo el monto recibido**; el pedido pasa a pagado. Existe una inconsistencia entre persistencia y memoria.
   - El registro no rechaza explícitamente montos cero o negativos. La acción `cancelarPaggo` solo cierra la ventana; no anula un pago registrado.
6. **[OBSERVADO] Identidad y validación del cliente:** el DNI es la clave primaria y se representa como `int`. La validación de pedidos exige exactamente ocho dígitos, aunque las comprobaciones del cliente y DNI están comentadas dentro de `registrarPedido` y se ejecutan en otro evento de la interfaz.
7. **[OBSERVADO] Modificación de pedidos:** permite cambiar membresía y cantidad, reemplaza la fecha original por `LocalDate.now()`, recalcula vencimiento y total, y compara el nuevo total con los pagos acumulados (`controlador/controladorPedido.java`, `actualizarPedido`).
   - Si lo pagado supera el nuevo total, elimina los pagos anteriores, inserta uno por la suma acumulada e informa el vuelto. Pierde el historial y conserva un pago superior al nuevo total.
8. **[OBSERVADO] Cantidad de detalles:** el modelo y el esquema admiten muchos detalles por pedido, pero el alta revisada agrega uno y la modificación opera sobre el primero. El vencimiento es único por pedido; no se define cómo combinar distintas duraciones.
9. **[OBSERVADO] Rangos de cantidad:** el selector ofrece de 1 a 12 para "Mensual" y de 1 a 3 para "Semanal" (`controlador/controladorPedido.java`, `combobox`). Son restricciones del flujo de UI ligadas al nombre de la membresía.
10. **[OBSERVADO] Precios históricos:** la base guarda el subtotal, pero al cargar los detalles se ignora ese valor y se recalcula con el precio actual de la membresía (`controlador/controladorDatos.java`, `loadDetallesAndPagos`; `modelo/Pedido.java`, `agregarDetalles`). Puede diferir del total almacenado del pedido.
11. **[OBSERVADO] Canal de llegada:** existe un catálogo fijo en código: "Recomendación", "Iba Pasando", "Redes Sociales", "Promociones/Descuentos" y "Otro" (`controlador/controladorDatos.java`, `loadComoLlego`).
12. **[OBSERVADO] Funcionalidad incompleta y alcance comprobado:** `controladorCliente.registrarCliente` solo lee campos y los métodos de alta, modificación y eliminación de `modelo/Db/ClienteDb.java` están vacíos. No se encontró un listado de próximos vencimientos en los modelos y controladores revisados.

## 5. Problemas conocidos del legacy (no replicar)

- **[OBSERVADO]** Dinero en `double`. **[DECISIÓN]** usar `BigDecimal` con escala 2.
- Lógica de negocio dentro de controladores de UI (`controladorPedido`, 685 líneas).
- Conexión nueva por consulta y credenciales en el código fuente.
- Sin tests, sin migraciones, un solo commit.
- Sin autenticación ni roles.
- **[OBSERVADO]** Inconsistencias entre pagos en memoria y persistidos, destrucción del historial al editar pedidos y recálculo de subtotales históricos con precios actuales.
- **[OBSERVADO]** Restricciones de duración, cantidad y canal de llegada fijadas por nombres o constantes de UI, y validación insuficiente de montos de pago.

## 6. Modelo objetivo [DECISIÓN]

Las reglas siguientes son decisiones para la versión nueva; reemplazan los comportamientos incompatibles del legacy.

- **[DECISIÓN] Gimnasio (tenant):** nombre, logo, color primario. Base compartida; toda entidad de negocio lleva `gimnasio_id`, y el aislamiento entre gimnasios es automático y probado con tests de integración. Se prevé Hibernate 6 con `@TenantId`; su alcance concreto debe verificarse en el plan técnico.
- **[DECISIÓN] Configuración por gimnasio:** disciplinas, membresías (precio y duración), cantidad máxima por compra, canales de llegada, cupos y políticas de reserva y cancelación. Nada de esto es constante de negocio en el código.
- **[DECISIÓN] Identidad del cliente:** clave propia y DNI como `String`, único por gimnasio, con validación de siete u ocho dígitos. La exigencia legacy de ocho dígitos no se hereda.
- **[DECISIÓN] Canal de llegada:** catálogo configurable por gimnasio. Seed inicial: recomendación, iba pasando, redes sociales, promociones y otro.
- **[DECISIÓN] Seed inicial:** Poly con Funcional, Boxeo y Crossfit como datos. Las membresías y precios serán ejemplos ficticios, a ajustar después; no se presentan como valores recuperados del legacy.

### Pedidos, duración y vigencia

- **[DECISIÓN]** En la feature 1, cada pedido tiene un único detalle: una membresía y su cantidad. Las distintas disciplinas se compran en pedidos separados; un cliente puede tener compras de distintas disciplinas simultáneamente.
- **[DECISIÓN]** Cada membresía tiene `duracion` y `unidadDuracion` (DIAS, SEMANAS, MESES) explícitas. La duración contratada es `cantidad × duracion`, en la unidad configurada, sin interpretar el nombre de la membresía.
- **[DECISIÓN]** La cantidad máxima es configurable por gimnasio. Los rangos legacy de 1–12 meses y 1–3 semanas no son requisitos del producto.
- **[DECISIÓN]** El detalle congela el precio unitario vigente al comprar. Su subtotal es `cantidad × precio unitario congelado`; en la feature 1, el total del pedido coincide con ese subtotal. Los cambios de precio del catálogo no modifican compras anteriores.
- **[DECISIÓN]** La vigencia comienza en la fecha de compra, por defecto hoy. Si se renueva una membresía que sigue vigente, la renovación continúa desde el vencimiento anterior. El día de vencimiento está incluido en la vigencia.
- **[DECISIÓN]** Pago y vigencia son conceptos separados: estado de pago `PENDIENTE` o `PAGADO`; vigencia `VIGENTE` o `VENCIDA`, calculada por fecha. `VENCIDO` no es un estado de pago. Un pago parcial no altera la vigencia; la deuda se rastrea aparte.

### Pagos y conservación del historial

- **[DECISIÓN]** Los montos de pago deben ser mayores a cero. Se admiten pagos parciales; la deuda se calcula con el total y los pagos aplicados no anulados. El pedido queda `PENDIENTE` mientras tenga deuda y `PAGADO` cuando esté saldado.
- **[DECISIÓN]** Si el monto recibido supera la deuda, se registra como pago solo lo aplicado a la deuda; el vuelto se informa y no se incorpora al importe del pago, tanto en memoria como en persistencia.
- **[DECISIÓN]** Un pedido con pagos no se edita. Los pagos no se borran: solo `ADMIN` puede anularlos y la anulación queda registrada, conservando el historial.

### Cuentas y permisos

- **[DECISIÓN]** Cada cuenta pertenece a un solo gimnasio, excepto `SUPERADMIN`, que opera a nivel de plataforma.
- **[DECISIÓN] ADMIN:** puede realizar todas las operaciones de su gimnasio, incluidas configuración y anulación de pagos.
- **[DECISIÓN] RECEPCION:** gestiona clientes, pedidos y pagos de su gimnasio; no accede a configuración ni realiza anulaciones.
- **[DECISIÓN] SUPERADMIN:** gestiona gimnasios y sus primeros administradores, sin acceso a datos de negocio de los gimnasios.
- **[DECISIÓN]** Se conserva el rol `CLIENTE` para el cliente final; sus permisos concretos quedan pendientes de definición.

### Features siguientes (ciclos separados de Spec Kit)

1. **[DECISIÓN] Núcleo multi-tenant con paridad de dominio:** API REST para clientes, servicios, membresías, pedidos, pagos y cálculo de vigencia y vencimientos; alta de gimnasio y primer administrador. Sin pantallas y sin importación de datos. Incluye el seed inicial con membresías y precios ficticios. El listado de próximos vencimientos queda para la feature 2.
2. **[DECISIÓN] Reservas de clases con cupo y listado de próximos vencimientos:**
   - Clase = disciplina + día + hora + cupo.
   - Reserva solo con membresía vigente; exigir además pago completo es configurable por gimnasio.
   - Nunca más reservas que el cupo, incluso con pedidos simultáneos.
   - Sin dos clases solapadas el mismo día para un cliente.
   - Cancelación hasta N horas antes (N configurable por gimnasio; valor inicial propuesto: 2).
3. **[DECISIÓN] Interfaz web:** pantallas con Thymeleaf + HTMX y branding por gimnasio, sin frontend JavaScript separado.
4. **[DECISIÓN] Demo pública:** datos ficticios y deploy.

## 7. Fuera de alcance por ahora

Cobros online, facturación fiscal, control de acceso físico (molinetes), app móvil nativa, lista de espera.

**[DECISIÓN]** La feature 1 excluye pantallas, importación de datos, reservas y listado de próximos vencimientos; su distribución por features se detalla arriba.

## 8. Preguntas abiertas (resolver en `/speckit.clarify`)

- ¿Qué permisos concretos tendrá el rol `CLIENTE` y en qué feature se habilita su cuenta?
- Para reservas (feature 2), ¿la membresía debe cubrir la fecha de la clase, además de estar vigente al reservar? ¿Qué pasa con reservas futuras si una anulación de pago deja deuda y el gimnasio exige pago completo?
- ¿Qué ventana usa el listado de próximos vencimientos de la feature 2? La referencia inicial propone siete días; falta confirmar el intervalo y si es configurable.
- ¿Cuál es el valor inicial de la política de cancelación de reservas? Se propusieron dos horas, sin confirmar todavía.
