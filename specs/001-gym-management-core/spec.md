# Feature Specification: Núcleo de gestión multi-gimnasio

**Feature Branch**: `001-gym-management-core`

**Created**: 2026-11-10

**Status**: Draft — pendiente de revisión y aprobación del autor

**Input**: Plataforma web (API REST) de gestión de gimnasios aislados entre sí:
alta de gimnasio y primer administrador, clientes, disciplinas, membresías,
pedidos de una membresía, pagos parciales y anulaciones, estado de pago y vigencia.
Recepción opera sin configurar ni anular pagos. Poly es el primer gimnasio de seed,
con Funcional, Boxeo y Crossfit y membresías ficticias. Fuera de alcance: reservas,
listado de vencimientos próximos, pantallas web e importación de datos.

**Referencias**: [Constitución 1.0.0](../../.specify/memory/constitution.md),
[dominio acordado](../../polygim-api/legacy/DOMINIO.md) y
[plan de trabajo de producto](../../PLAN_DE_TRABAJO.md).
La paridad es de dominio; las funcionalidades incompletas y los errores del legacy
no son requisitos. Las decisiones técnicas se desarrollan en la etapa plan.

## Clarifications

### Session 2026-11-10

- Q: ¿Cómo se crean las cuentas de recepción en esta feature? → A: ADMIN crea y deshabilita cuentas de RECEPCION dentro de su gimnasio.
- Q: ¿Qué se considera una renovación? → A: Se elige una compra previa del mismo cliente y de la misma membresía para renovarla.
- Q: ¿Qué fechas de compra se permiten? → A: Hoy o una fecha pasada; no se permiten fechas futuras.
- Directiva Git: `main` es producción, `dev` integra desarrollo y cada spec se trabaja en una rama creada a partir de `dev`.

## User Scenarios & Testing *(mandatory)*

Cada historia se verifica con datos propios de prueba, sin depender de ejecutar
previamente otras historias. Las pruebas de integración incorporan al menos dos
gimnasios para hacer observable cualquier pérdida de aislamiento.

### User Story 1 - Habilitar un gimnasio y su administrador (Priority: P1)

Como SUPERADMIN quiero registrar un gimnasio y su primer ADMIN para que pueda
comenzar a operar con identidad propia y sin compartir sus datos con otros clientes.

**Why this priority**: Es el ingreso de cada gimnasio al producto y establece su administración.

**Independent Test**: Con un SUPERADMIN autorizado, registrar un gimnasio y verificar
que su primer ADMIN puede autenticarse y operar exclusivamente en él.

**Acceptance Scenarios**:

1. **Given** un SUPERADMIN autenticado, **When** registra nombre, logo, color primario y los datos del primer ADMIN, **Then** se crean el gimnasio y su administrador vinculados.
2. **Given** datos inválidos del gimnasio o de su administrador, **When** se solicita el alta, **Then** se informa el error sin dejar un gimnasio o una cuenta creados parcialmente.
3. **Given** un ADMIN o RECEPCION autenticado, **When** intenta dar de alta otro gimnasio, **Then** recibe una denegación de permisos.
4. **Given** un SUPERADMIN autenticado, **When** intenta consultar clientes, compras o pagos de cualquier gimnasio, **Then** no obtiene acceso a esos datos de negocio.

### User Story 2 - Operar con identidad, roles y aislamiento (Priority: P1)

Como usuario del gimnasio quiero acceder únicamente a las operaciones y datos que
me corresponden. Como ADMIN quiero habilitar o deshabilitar cuentas de recepción.

**Why this priority**: El aislamiento y los permisos son condición para que distintos gimnasios utilicen el mismo producto.

**Independent Test**: Preparar dos gimnasios con ADMIN y RECEPCION propios y ejecutar
operaciones permitidas y prohibidas para cada rol, incluyendo identificadores ajenos.

**Acceptance Scenarios**:

1. **Given** una cuenta habilitada de un gimnasio, **When** se autentica, **Then** opera con el rol y el gimnasio asignados a esa cuenta.
2. **Given** un ADMIN del gimnasio A, **When** crea o deshabilita una cuenta RECEPCION, **Then** afecta únicamente una cuenta de A; una cuenta deshabilitada no puede realizar operaciones protegidas, aunque hubiera iniciado sesión antes.
3. **Given** un usuario de A, **When** consulta o modifica por identificador un recurso de B, **Then** recibe una respuesta de recurso inexistente (404), sin datos ni confirmación de existencia del recurso ajeno.
4. **Given** un usuario de A, **When** intenta crear una compra con un cliente o una membresía de B, o un pago para una compra de B, **Then** se rechaza sin crear relaciones entre gimnasios.
5. **Given** RECEPCION de A, **When** intenta configurar A, gestionar cuentas o anular un pago, **Then** recibe una denegación de permisos (403).
6. **Given** una solicitud sin identidad válida o sin gimnasio autorizado para operar el negocio, **When** intenta una operación protegida, **Then** se rechaza sin devolver datos de negocio.
7. **Given** un usuario de A, **When** intenta cambiar el gimnasio declarado en una solicitud o en los datos de una entidad, **Then** no consigue operar como B ni transferir datos entre gimnasios.

### User Story 3 - Registrar y mantener clientes (Priority: P1)

Como ADMIN o RECEPCION quiero registrar, editar y consultar clientes de mi gimnasio
para identificar a quién se vende cada membresía y conservar sus datos de contacto.

**Why this priority**: Toda compra se vincula con un cliente registrado del mismo gimnasio.

**Independent Test**: Registrar y editar un cliente completo, consultarlo y comprobar
la unicidad de su DNI dentro del gimnasio y su independencia de otro gimnasio.

**Acceptance Scenarios**:

1. **Given** un usuario ADMIN o RECEPCION, **When** registra DNI, nombre, apellido, teléfono, canal de llegada, dirección y localidad de un cliente, **Then** el cliente queda asociado a su gimnasio con identidad propia y fecha de ingreso.
2. **Given** un cliente propio, **When** se editan sus datos, **Then** los cambios se conservan sin alterar su identidad ni perder sus compras anteriores.
3. **Given** un DNI de siete u ocho dígitos no registrado en A, **When** se da de alta en A, **Then** se acepta aunque el mismo DNI exista en B.
4. **Given** un DNI ya registrado en A, **When** se intenta crear otro cliente con ese DNI o editar otro para duplicarlo en A, **Then** se rechaza sin sobrescribir al cliente existente.
5. **Given** un DNI con letras o con longitud distinta de siete u ocho dígitos, **When** se registra o edita, **Then** se informa el error sin guardar el cambio.
6. **Given** un canal de llegada o una localidad de otro gimnasio, **When** se intenta asignarlo a un cliente de A, **Then** se rechaza la asociación.

### User Story 4 - Configurar la oferta de cada gimnasio (Priority: P1)

Como ADMIN quiero definir disciplinas, membresías y canales de llegada, y configurar
la cantidad máxima por compra, para adaptar el producto a la oferta de mi gimnasio.

**Why this priority**: Permite vender distintos planes sin adaptar el código al primer cliente.

**Independent Test**: Configurar ofertas diferentes en dos gimnasios y verificar que
cada uno puede vender sus propios planes y aplicar su propia cantidad máxima.

**Acceptance Scenarios**:

1. **Given** un ADMIN, **When** crea o modifica una disciplina y sus membresías con nombre, precio, duración y unidad, **Then** el catálogo se guarda exclusivamente en su gimnasio.
2. **Given** una membresía de duración explícita, **When** cambia su nombre, **Then** su unidad y duración no cambian por el texto elegido.
3. **Given** un ADMIN, **When** configura los canales de llegada o la cantidad máxima por compra, **Then** se aplican a nuevas operaciones de su gimnasio sin modificar otros gimnasios.
4. **Given** RECEPCION, **When** consulta disciplinas, membresías, localidades y canales propios necesarios para operar, **Then** puede utilizarlos; cuando intenta configurarlos, recibe una denegación.
5. **Given** una compra existente, **When** se actualiza el precio o duración de su membresía en el catálogo, **Then** no cambian su precio congelado, total ni vencimiento ya calculado.

### User Story 5 - Comprar o renovar una membresía (Priority: P1)

Como ADMIN o RECEPCION quiero registrar una compra de una membresía para un cliente,
con total y vencimiento calculados, para vender el período contratado sin errores manuales.

**Why this priority**: Es la operación central que vincula cliente, oferta y cobro.

**Independent Test**: Crear compras con distintas unidades de duración y cantidades,
verificar sus importes y renovaciones, y consultar su estado sin registrar pagos.

**Acceptance Scenarios**:

1. **Given** un cliente y una membresía propios con precio 100,25, **When** se registra cantidad 2, **Then** se crea un pedido con un detalle, precio unitario congelado 100,25 y total 200,50.
2. **Given** una membresía de duración 2 SEMANAS, **When** se compra cantidad 3 el 2026-11-10, **Then** el vencimiento se calcula agregando seis semanas y resulta 2026-12-22.
3. **Given** una compra válida sin fecha indicada, **When** se registra, **Then** toma la fecha actual; una fecha pasada explícita se respeta y una futura se rechaza.
4. **Given** una compra propia vigente de una membresía que vence el 2026-11-30, **When** se elige renovarla el 2026-11-10 por una duración contratada de un mes, **Then** la nueva compra usa el vencimiento anterior como base y vence el 2026-12-30.
5. **Given** una compra elegida para renovación cuyo vencimiento ya pasó respecto de la fecha de compra registrada, **When** se renueva, **Then** el nuevo vencimiento se calcula desde esa fecha de compra.
6. **Given** una compra de otro cliente, otra membresía u otro gimnasio, **When** se intenta elegirla como antecedente de renovación, **Then** se rechaza sin registrar el pedido.
7. **Given** una compra cuyo vencimiento es hoy, **When** se consulta hoy, **Then** está VIGENTE; al consultarla al día siguiente está VENCIDA, cualquiera sea su estado de pago.
8. **Given** una cantidad cero, negativa o superior al máximo configurado, **When** se solicita la compra, **Then** se rechaza sin guardar el pedido.
9. **Given** un cliente que compra dos disciplinas, **When** se registran sus compras, **Then** se crean pedidos separados, cada uno con una sola membresía y su propio vencimiento.
10. **Given** un pedido con pagos registrados, **When** se intenta editar sus datos comerciales o fechas, **Then** se rechaza y se conserva su historial.

### User Story 6 - Registrar cobros y consultar la deuda (Priority: P1)

Como ADMIN o RECEPCION quiero registrar pagos parciales o completos y consultar la
deuda para cobrar sin confundir la situación de pago con la vigencia de la membresía.

**Why this priority**: Completa el circuito de venta con importes exactos y pagos trazables.

**Independent Test**: Sobre una compra de total 200,50, registrar un parcial, un pago
que excede la deuda y consultar importes, vuelto, estado de pago y vigencia.

**Acceptance Scenarios**:

1. **Given** un pedido de total 200,50 sin pagos, **When** se registra un pago de 50,25, **Then** se aplica 50,25, queda deuda 150,25 y el estado de pago sigue PENDIENTE.
2. **Given** deuda 150,25, **When** se reciben 200,00, **Then** se registra un pago de 150,25, se informa vuelto 49,75 y el pedido pasa a PAGADO con deuda cero.
3. **Given** una deuda positiva, **When** se registra exactamente ese importe, **Then** el pedido queda PAGADO y no se informa vuelto positivo.
4. **Given** un monto cero o negativo, **When** se intenta registrar un pago, **Then** se rechaza sin alterar pagos, deuda, estado de pago ni vigencia.
5. **Given** un pedido PAGADO con deuda cero, **When** se intenta registrar otro cobro, **Then** no se registra un pago aplicado de monto cero ni se genera saldo a favor.
6. **Given** una membresía VIGENTE, **When** recibe un pago parcial, **Then** sigue VIGENTE y conserva su vencimiento; un pago completo de una compra VENCIDA tampoco renueva su vigencia.
7. **Given** cobros simultáneos para el mismo pedido, **When** se procesan, **Then** la suma aplicada nunca supera el total y la deuda y el historial quedan consistentes.

### User Story 7 - Anular un pago conservando el historial (Priority: P1)

Como ADMIN quiero anular un pago incorrecto conservando evidencia de la operación,
para corregir el cobro y reconstruir cómo se obtuvo la deuda actual.

**Why this priority**: Permite corregir errores sin borrar movimientos ni alterar la vigencia.

**Independent Test**: Anular un pago de una compra saldada y comprobar que reaparece
la deuda correspondiente y se conserva el pago original y el registro de anulación.

**Acceptance Scenarios**:

1. **Given** una compra PAGADA con pagos de 50,25 y 150,25, **When** ADMIN anula el segundo, **Then** la deuda pasa a 150,25 y el estado vuelve a PENDIENTE, conservando ambos pagos y la anulación.
2. **Given** una anulación registrada, **When** ADMIN o RECEPCION consulta el historial de la compra, **Then** distingue el pago anulado y puede identificar quién lo anuló y cuándo.
3. **Given** un pago ya anulado, **When** se repite el intento de anulación, **Then** no se vuelve a incrementar la deuda ni se duplica el efecto económico.
4. **Given** RECEPCION o un ADMIN de otro gimnasio, **When** intenta anular el pago, **Then** se rechaza según su falta de permiso o la pertenencia ajena del recurso y no se modifica el historial.
5. **Given** una compra con fecha de vencimiento definida, **When** se anula un pago, **Then** su vigencia y vencimiento no cambian.

### User Story 8 - Iniciar con un gimnasio de ejemplo (Priority: P2)

Como responsable del producto quiero un primer gimnasio con oferta ficticia para
mostrar el circuito de compras y pagos sin depender de una importación del sistema anterior.

**Why this priority**: Facilita una demostración reproducible sin condicionar el producto a esa oferta.

**Independent Test**: Preparar los datos iniciales, operar una compra en el gimnasio
inicial y verificar que un segundo gimnasio puede tener una oferta diferente.

**Acceptance Scenarios**:

1. **Given** una instalación preparada para datos iniciales, **When** se carga el seed, **Then** existe Poly con Funcional, Boxeo y Crossfit, al menos una membresía ficticia por disciplina con precio y duración explícitos, y los canales recomendación, iba pasando, redes sociales, promociones y otro.
2. **Given** ese seed y un segundo gimnasio, **When** el segundo configura una disciplina y membresía distintas, **Then** opera sin incorporar nombres o reglas del primer gimnasio y sin acceder a sus datos.
3. **Given** el catálogo inicial, **When** se consulta su documentación de ejemplo, **Then** los planes y precios se identifican como ficticios y ajustables, sin atribuirlos a datos recuperados del legacy.

### Edge Cases

- Mismo DNI en distintos gimnasios; duplicación en el mismo gimnasio al crear y al editar.
- Identificadores ajenos en consultas individuales, listados y relaciones entre entidades.
- Solicitudes sin identidad o sin tenant válido y cuentas de recepción deshabilitadas con acceso previo.
- Cantidad cero, negativa, superior al máximo o intento de enviar varios detalles en un pedido.
- Cambio de nombre de una membresía y actualización de catálogo con compras históricas existentes.
- Compra con fecha pasada, intento de fecha futura y consulta de vigencia en el día exacto del vencimiento.
- Renovación de compra vigente o vencida, y referencia de otro cliente o membresía.
- Duraciones en días, semanas y meses; cierre de mes y año bisiesto. La suma de meses usa el último día válido cuando el mes destino no contiene el día original.
- Pago exacto, parcial, superior a la deuda, sin deuda, cero o negativo; pagos simultáneos.
- Anulación repetida y anulación del último pago que completó el total; el pago original no se elimina.
- Intento de editar un pedido que tuvo pagos, aunque después se hayan anulado.
- Fallo durante un alta de gimnasio, pedido, pago o anulación: no dejar datos ni estados parciales.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a SUPERADMIN dar de alta un gimnasio con nombre, logo y color primario y crear su primer ADMIN en una operación completa o sin efectos parciales.
- **FR-002**: El sistema DEBE autenticar a las cuentas habilitadas antes de permitir operaciones protegidas y reconocer su rol y gimnasio autorizado.
- **FR-003**: Cada cuenta de gimnasio DEBE pertenecer a uno solo; SUPERADMIN opera a nivel de plataforma sin acceder a datos de negocio.
- **FR-004**: ADMIN DEBE poder crear y deshabilitar cuentas RECEPCION de su propio gimnasio. RECEPCION no administra cuentas. Deshabilitar una cuenta DEBE impedir nuevas operaciones protegidas con ella.
- **FR-005**: ADMIN DEBE poder realizar las operaciones de negocio y configuración de su gimnasio. RECEPCION DEBE poder operar clientes, pedidos y pagos y consultar los catálogos necesarios, sin configurar ni anular pagos.
- **FR-006**: El sistema DEBE impedir lecturas, escrituras y asociaciones entre gimnasios en todas las operaciones; los listados DEBEN contener exclusivamente datos propios y los identificadores enviados no deben cambiar el gimnasio efectivo.
- **FR-007**: Un recurso de otro gimnasio DEBE responder como inexistente (404); una operación prohibida por el rol sobre datos propios DEBE responder con permiso insuficiente (403), sin cambios de datos.
- **FR-008**: ADMIN y RECEPCION DEBEN poder registrar, editar, listar y consultar clientes propios con DNI, nombre, apellido, teléfono, canal de llegada, dirección, localidad y fecha de ingreso, conservando su relación con compras anteriores.
- **FR-009**: El DNI DEBE contener siete u ocho dígitos, conservarse sin interpretación numérica y ser único por gimnasio, tanto en altas como en ediciones. La identidad interna del cliente DEBE ser independiente de su DNI.
- **FR-010**: ADMIN DEBE poder mantener el catálogo de disciplinas y sus membresías, con nombre, precio, duración positiva y unidad DIAS, SEMANAS o MESES.
- **FR-011**: ADMIN DEBE poder configurar el catálogo de canales de llegada, las localidades utilizadas por sus clientes y la cantidad máxima permitida por compra en su gimnasio.
- **FR-012**: Los clientes, cuentas, direcciones, localidades, canales, disciplinas, membresías, pedidos y pagos del negocio DEBEN mantener pertenencia consistente al mismo gimnasio en sus relaciones.
- **FR-013**: ADMIN y RECEPCION DEBEN poder crear pedidos para clientes propios usando una membresía propia, con exactamente un detalle y una cantidad entera positiva que no exceda el máximo configurado.
- **FR-014**: El total del pedido DEBE calcularse como cantidad por precio unitario vigente al comprar; los importes proporcionados por quien solicita la compra no son una fuente autorizada del precio ni del total.
- **FR-015**: El detalle DEBE conservar el precio unitario de compra. Los cambios posteriores del catálogo NO DEBEN alterar ese precio, el total ni las fechas calculadas de compras anteriores.
- **FR-016**: La fecha de compra DEBE ser hoy por defecto; se DEBEN aceptar fechas pasadas explícitas y rechazar fechas futuras. Para una compra retroactiva se utiliza el precio vigente al registrarla, sin reconstruir precios históricos del catálogo.
- **FR-017**: La duración contratada DEBE calcularse como cantidad por duración explícita de la membresía, en su unidad configurada, sin depender de su nombre.
- **FR-018**: Una compra nueva DEBE calcular su vencimiento desde su fecha de compra. Al renovar, se DEBE elegir una compra previa del mismo gimnasio, cliente y membresía; si está vigente respecto de la fecha de compra registrada, la base del cálculo es su vencimiento anterior; si ya venció, la base es la fecha de compra registrada.
- **FR-019**: La suma de meses DEBE conservar el día cuando exista en el mes destino y usar su último día válido en caso contrario. Sumar semanas equivale a sumar períodos de siete días.
- **FR-020**: Para compras ya realizadas, el día de vencimiento DEBE estar incluido: la vigencia es VIGENTE hasta ese día y VENCIDA desde el siguiente. La fecha usada para evaluar la vigencia DEBE ser consistente en todas las consultas de una operación.
- **FR-021**: El estado de pago DEBE ser PENDIENTE si hay deuda y PAGADO si está saldado. La deuda DEBE calcularse como total menos suma de pagos aplicados no anulados.
- **FR-022**: Registrar, completar o anular un pago NO DEBE modificar la vigencia ni el vencimiento de la compra. Pago y vigencia DEBEN informarse como conceptos separados.
- **FR-023**: ADMIN y RECEPCION DEBEN poder registrar pagos de monto recibido mayor a cero, incluidos parciales, siempre sobre pedidos de su gimnasio.
- **FR-024**: Si el monto recibido excede la deuda, el pago DEBE registrar únicamente lo aplicado a la deuda e informar el vuelto por separado; los importes aplicados DEBEN ser positivos y la deuda no puede ser negativa.
- **FR-025**: Si la deuda es cero o el monto recibido no es positivo, el sistema DEBE rechazar el registro sin agregar pagos ni alterar la compra.
- **FR-026**: Solo ADMIN del gimnasio DEBE poder anular un pago propio. El pago NO DEBE borrarse; se DEBE conservar el importe original, su condición de anulado y un registro de quién lo anuló y cuándo.
- **FR-027**: Una anulación DEBE excluir ese pago del cálculo de deuda y estado de pago; repetir el intento NO DEBE aplicar dos veces su efecto económico.
- **FR-028**: Un pedido con pagos registrados NO DEBE poder editarse, incluso cuando sus pagos se hayan anulado. La edición de pedidos sin pagos no es una operación requerida en esta feature.
- **FR-029**: ADMIN y RECEPCION DEBEN poder consultar compras propias, filtrarlas por cliente y ver su membresía, cantidad, precio congelado, total, fecha de compra, vencimiento, estado de pago, deuda, vigencia e historial de pagos y anulaciones.
- **FR-030**: Las operaciones simultáneas y los fallos DEBEN preservar la exactitud de total, pagos, deuda y estado, sin duplicar efectos económicos ni dejar altas o movimientos parciales.
- **FR-031**: Los datos iniciales DEBEN incluir Poly, Funcional, Boxeo y Crossfit, membresías con precios y duraciones ficticios y los canales de llegada acordados, como datos reemplazables por gimnasio.
- **FR-032**: Todas las operaciones incluidas DEBEN poder realizarse por API REST; esta feature no requiere pantallas ni cuentas del cliente final.

### Constitutional Requirements *(mandatory)*

- **CR-001**: Ninguna operación DEBE exponer ni asociar datos ajenos. La aceptación DEBE demostrarlo con al menos dos gimnasios, incluyendo búsquedas por identificador, listados, altas, modificaciones y relaciones.
- **CR-002**: La matriz de permisos DEBE verificarse para SUPERADMIN, ADMIN y RECEPCION, incluyendo ausencia de identidad válida, cuentas deshabilitadas y superadmin sin acceso al negocio.
- **CR-003**: La oferta, cantidades máximas y canales DEBEN ser configurables por gimnasio; el seed inicial NO DEBE determinar las reglas del producto.
- **CR-004**: Los importes DEBEN conservar exactitud decimal de dos posiciones en cálculo, consulta y persistencia. Los precios congelados y el historial NO DEBEN reescribirse por cambios del catálogo.
- **CR-005**: Los errores de validación, negocio y autorización DEBEN seguir el contrato uniforme establecido por la constitución, sin secretos, trazas internas ni información de otros gimnasios.
- **CR-006**: La aceptación DEBE incluir pruebas de las reglas y límites monetarios y temporales, integración de aislamiento y permisos, y el mínimo de cobertura de lógica de negocio exigido por la constitución.

### Key Entities *(include if feature involves data)*

- **Gimnasio**: Tenant del producto; nombre, logo, color primario y configuración propia.
- **Cuenta de usuario**: Identidad autenticable, condición habilitada/deshabilitada y rol; pertenece a un gimnasio salvo SUPERADMIN.
- **Cliente**: Identidad propia, DNI único en su gimnasio, nombre, apellido, teléfono, fecha de ingreso, canal de llegada y dirección con localidad. No implica una cuenta de acceso del cliente final.
- **Canal de llegada**: Opción del catálogo configurable del gimnasio utilizada para registrar cómo llegó un cliente.
- **Dirección y localidad**: Datos de ubicación vinculados con el cliente, con pertenencia consistente al gimnasio.
- **Disciplina**: Servicio definido por el gimnasio, sin lista de nombres fija.
- **Membresía**: Plan de una disciplina con precio, duración y unidad explícitas.
- **Pedido y detalle único**: Compra de una membresía por un cliente, con cantidad, precio unitario congelado, total, fechas y, cuando se renueva, compra anterior elegida.
- **Pago**: Importe positivo aplicado a una compra y condición de anulado o no anulado; el vuelto no forma parte del importe aplicado.
- **Registro de anulación**: Evidencia de qué pago se anuló, quién realizó la operación y cuándo; preserva el movimiento original.
- **Estado de pago, deuda y vigencia**: Resultados calculados a partir de importes, pagos no anulados y fechas; no son estados intercambiables.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Con dos gimnasios preparados, el 100% de los intentos de leer, modificar o asociar recursos ajenos de los escenarios de aceptación se rechaza y se exponen cero registros de otro gimnasio.
- **SC-002**: SUPERADMIN puede completar el alta de un gimnasio y su primer administrador; ADMIN y RECEPCION completan el 100% de sus circuitos autorizados y no completan ninguna operación prohibida por su rol.
- **SC-003**: Un mismo DNI puede identificar clientes independientes en dos gimnasios, y el 100% de los intentos de duplicarlo dentro de un gimnasio se rechaza sin pérdida de datos.
- **SC-004**: Todas las compras de los ejemplos conservan exactamente su precio y total después de cambiar el catálogo, y cada una contiene una sola membresía.
- **SC-005**: El circuito de compra de 200,50, pago de 50,25, recepción de 200,00 con vuelto de 49,75 y anulación del pago aplicado de 150,25 produce deuda final 150,25, historial completo y ninguna diferencia monetaria.
- **SC-006**: El 100% de los casos de aceptación de vigencia conserva su vencimiento ante pagos y anulaciones, incluye el día de vencimiento y aplica la renovación elegida y las tres unidades de duración sin depender de nombres.
- **SC-007**: Dos gimnasios pueden operar con distintas disciplinas, planes, canales y cantidades máximas sin modificar el producto para esos nombres o valores.
- **SC-008**: Los ocho circuitos de usuario pueden demostrarse por operaciones de la API, con datos de ejemplo identificados como ficticios y sin pantallas ni importación del legacy.

## Assumptions

- Las reglas marcadas como decisión en DOMINIO.md y las respuestas de esta sesión rigen el alcance; los comportamientos legacy incompatibles no se heredan.
- Un responsable prepara el acceso inicial de SUPERADMIN. La forma de aprovisionamiento y los mecanismos de autenticación se concretan en el plan y no requieren contraseñas en los archivos del proyecto.
- La fecha de compra y la fecha de consulta son fechas de calendario; el criterio temporal uniforme de la instalación se concreta en el plan.
- Para compras retroactivas, el precio se congela al registrar la compra: no existe un catálogo histórico para reconstruir precios pasados en esta feature.
- El seed utiliza ejemplos ficticios, no una importación de clientes, precios ni credenciales del sistema anterior. Las pruebas dispondrán además de un segundo gimnasio independiente.
- El rol CLIENTE y su autoservicio quedan para la feature de reservas; esta feature gestiona registros de clientes y cuentas de personal.
- No se incluyen reservas, cupos, listado de próximos vencimientos, pantallas web, importación, cobros online, facturación fiscal, molinetes, app móvil, lista de espera ni demo pública o despliegue de producción.
- No se exige una función para editar pedidos sin pagos, eliminar compras o eliminar pagos. La prohibición de editar pedidos con pagos se mantiene ante cualquier intento de modificación.
- La evolución de datos, los contratos detallados, las versiones del stack, la cobertura y las herramientas de verificación se desarrollan en el plan conforme a la constitución.
- La aceptación de esta spec habilita clarify; plan y tasks requieren sus propias revisiones. El trabajo de esta feature permanece en su rama, sin integrar automáticamente en dev ni main.
