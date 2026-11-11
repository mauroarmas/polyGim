# Flujo funcional — núcleo de gestión

Con Compose iniciado, abrí **http://localhost:8081** para la configuración local
preparada; si cambiaste `APP_PORT`, usá ese valor. No hace falta Swagger ni copiar
tokens. Usá datos ficticios y un DNI nuevo para cada recorrido.

## 1. Acceso y navegación

Ingresá con el correo y contraseña de `SEED_ADMIN_EMAIL` y `SEED_ADMIN_PASSWORD`
en tu `.env` local. Debés ver Inicio, Clientes, Compras y pagos, Membresías,
Catálogos, Equipo y Configuración. Si los datos son incorrectos, aparece un mensaje
sin revelar si la cuenta existe. Cerrar sesión vuelve al formulario de acceso.

## 2. Registrar y editar un cliente

1. En **Clientes → Registrar cliente**, completá nombre, apellido y DNI de 7 u 8
   dígitos. Los demás campos son opcionales.
2. Guardá: se abre su ficha y aparece la confirmación.
3. Abrí **Editar datos**, agregá teléfono y guardá. Verificá el cambio en la ficha.
4. Volvé a Clientes y buscá el DNI completo: la lista se actualiza sin recargar
   toda la página. Un DNI incompleto pide completar los dígitos.
5. Intentá registrar otra persona con el mismo DNI: se rechaza sin duplicarla.
   Con Atrás del navegador podés corregir el formulario.

## 3. Comprar una membresía

Desde la ficha, abrí **Nueva compra**. Con el seed de ejemplo, elegí el plan con
precio **100.25**, duración **2 SEMANAS** y cantidad **2**. Registrá la compra.

Esperado: total **200.50**, deuda **200.50**, estado **PENDIENTE** y fecha de
vencimiento calculada sumando cuatro semanas a la fecha de compra. El día de
vencimiento está incluido. La fecha de compra puede ser hoy o una fecha pasada.

Cada envío exitoso crea una compra. Si una respuesta se pierde, consultá la ficha
antes de repetir la acción.

## 4. Cobrar y mostrar vuelto

En el detalle de compra:

1. Registrá **50.25**. Esperado: deuda **150.25**, estado **PENDIENTE**.
2. Registrá **200.00**. Esperado: monto aplicado **150.25**, vuelto **49.75** y
   deuda **0.00**, estado **PAGADO**.
3. La fecha de vencimiento debe ser la misma. El formulario de cobro deja de
   ofrecer otro pago cuando no hay deuda.

Usá punto y dos decimales; el sistema rechaza importes nulos o negativos. Ante una
interrupción de red, revisá el historial antes de repetir un cobro.

## 5. Anular y renovar

1. Como ADMIN, en el historial abrí **Anular** sobre el pago de **150.25**.
2. Leé la confirmación y elegí **Confirmar anulación**.
3. Esperado: el pago permanece como **ANULADO**, se conserva evidencia del actor y
   fecha, reaparece deuda **150.25** y el vencimiento no cambia.
4. Elegí **Renovar membresía** y registrá la nueva compra. Si la anterior sigue
   vigente, el nuevo vencimiento se calcula desde el vencimiento anterior.
5. Intentá renovar nuevamente la misma compra anterior: se rechaza; para continuar
   la cadena se renueva la compra sucesora.

## 6. Configuración y catálogos

- En **Catálogos**, agregá y renombrá una disciplina, un canal de llegada y una
  localidad. Las opciones aparecen en los formularios correspondientes.
- En **Membresías → Crear membresía**, elegí una disciplina, nombre, precio positivo
  y duración. Editala después: las compras previas conservan precio y duración.
- En **Configuración**, cambiá el máximo de unidades por compra. Las siguientes
  compras respetan el límite; las anteriores permanecen iguales.
- Los listados tienen paginación; los selectores incluyen todos los registros.

## 7. Probar recepción y revocación

1. Como ADMIN, en **Equipo**, creá una cuenta con un correo nuevo y contraseña de
   12 a 72 caracteres. Compartila solo de forma privada.
2. En otro perfil de navegador o una ventana privada, ingresá como recepción.
3. Debe poder trabajar con clientes, compras y cobros; no debe ver controles para
   configurar, administrar personal ni anular pagos.
4. Abrí directamente `/app/configuracion`: recibe acceso denegado.
5. Desde la sesión ADMIN, deshabilitá la cuenta en Equipo. La siguiente solicitud
   del navegador de recepción vuelve a pedir acceso, aunque ya hubiera iniciado
   sesión. No se implementó reactivación de cuentas en esta feature.

## 8. Superadmin y aislamiento

1. Cerrá sesión e ingresá con `BOOTSTRAP_SUPERADMIN_EMAIL` y su contraseña del `.env`.
2. Se muestra el listado de gimnasios y el formulario para crear uno junto con su
   primer ADMIN. Completá nombre, URL del logo, color y credenciales nuevas.
3. SUPERADMIN no tiene acceso a fichas, compras ni catálogos de los gimnasios.
4. Ingresá como ADMIN del segundo gimnasio. Su directorio está vacío. Puede
   registrar el mismo DNI del primero porque la unicidad es por gimnasio.
5. Copiá una URL de cliente o compra del primer gimnasio e intentá abrirla con el
   segundo ADMIN: se muestra **404**, sin revelar los datos ajenos.

## 9. Navegador móvil y persistencia

Probá con un ancho de 390 px: la navegación se desplaza horizontalmente, los
formularios se apilan y las tablas se desplazan dentro de su sección. El documento
completo no debe desbordarse horizontalmente.

Reiniciá solo la aplicación con `docker compose restart app`, volvé a ingresar y
verificá que la ficha, compras e historial sigan presentes. El seed no debe duplicar
catálogos ni reemplazar credenciales. `docker compose down` conserva el volumen;
no uses `down -v` para detener una instalación cuyos datos quieras mantener.

## Validación automatizada complementaria

`JAVA_HOME=/ruta/jdk21 ./mvnw clean verify` desde `polygim-api` ejecuta las pruebas
unitarias, integración con dos gimnasios, contratos y cobertura. `WebFlowIT` añade
sesión, CSRF, páginas, permisos, cobros, renovaciones y revocación. Las pruebas de
concurrencia de pagos y renovaciones continúan ejecutándose en la suite completa.
