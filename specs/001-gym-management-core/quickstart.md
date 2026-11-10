# Quickstart de validación — Feature 1

**Estado:** guía de ejecución local. El 2026-11-10 se reprodujo el circuito de
arranque, seed, compra, cobros, anulación, aislamiento y reinicio contra una base
PostgreSQL temporal. Las pruebas automatizadas complementan esa comprobación manual.

Referencias: [plan](plan.md), [modelo](data-model.md), [REST](contracts/api.md),
[OpenAPI](contracts/openapi.yaml) y [criterios SC-001–SC-008](spec.md#measurable-outcomes).

## Prerrequisitos

- Java 21 seleccionado en JAVA_HOME/PATH, curl y jq. Maven lo proveerá el Wrapper 3.9.11.
- Docker operativo y accesible por el usuario, incluido el arranque de contenedores
  de pruebas. Testcontainers 1.21.4 conserva el BOM y contempla Docker 29; no configurar
  DOCKER_API_VERSION como solución automática.
- Rama de la spec con implementación aprobada; comandos desde la raíz del repo salvo
  indicación. No se integra ni publica automáticamente en dev/main.
- El Java global observado en el equipo es 17: seleccionar Java 21 para este
  proyecto antes de ejecutar Maven. El build de imagen no sustituye los tests de integración.

```bash
java -version
docker info
```

Resultado requerido: Java 21 y daemon accesible. No ejecutar pruebas con un JDK inferior.

## Configuración local

`.env.example` no contiene secretos reales. Crear `.env` local y completar sus
valores; no versionarlo, imprimirlo en logs ni copiar credenciales del legacy.

| Variable | Uso |
|---|---|
| DB_HOST, DB_PORT, DB_NAME | Conexión PostgreSQL; host local al ejecutar Maven, db dentro de Compose |
| DB_USERNAME, DB_PASSWORD | Credenciales locales de la base para datasource/Flyway |
| JWT_SECRET_BASE64 | Clave aleatoria de al menos 256 bits, no una contraseña reutilizada |
| JWT_ISSUER, JWT_AUDIENCE | Emisor y destinatario esperados del token |
| JWT_TTL | Duración inicial PT15M |
| APP_TIME_ZONE | Zona de calendario de instalación; ejemplo America/Argentina/Tucuman |
| DEFAULT_GYM_MAX_QUANTITY | Máximo inicial por gimnasio; positivo, al menos 2 para el ejemplo de abajo |
| BOOTSTRAP_SUPERADMIN_EMAIL, BOOTSTRAP_SUPERADMIN_PASSWORD | Crear la primera cuenta global si no existe |
| SEED_DEMO_ENABLED | Opt-in del seed ficticio, false fuera de la demostración |
| SEED_ADMIN_EMAIL, SEED_ADMIN_PASSWORD | Administrador inicial de los datos de demostración |

Email y contraseñas se suministran por entorno. Contraseñas nuevas: política definida
en el contrato. El seed no cambia hashes ni reactiva cuentas existentes al reiniciar.
La imagen PostgreSQL será 17.11 fijada por digest, igual en Compose y tests.

```bash
cp .env.example .env
# Completar .env en un editor local; usar sintaxis compatible con export de shell.
set -a
. ./.env
set +a
docker compose up -d db
```

No ejecutar `docker compose down -v` sobre una base que se desee conservar.
Las pruebas usan bases efímeras separadas; nunca la DB de una instalación real.

## Verificación automatizada

```bash
cd polygim-api
./mvnw -version
./mvnw clean verify
```

Esperado: compilación Java 21, tests unitarios, integración PostgreSQL/Testcontainers,
Flyway desde vacío y actualización, y JaCoCo con al menos 80% de líneas del conjunto
de lógica de negocio. Consultar reportes Surefire, Failsafe y target/site/jacoco/.
No se excluye lógica del denominador para alcanzar el porcentaje.

Para ejecutar específicamente el test de aislamiento:

```bash
./mvnw -Dit.test=TenantIsolationIT verify
```

Debe comprobar persist/list/count/find por ID, tenants forzados, asociaciones, scopes
AUTH/PLATFORM, ausencia de tenant, SUPERADMIN sin negocio, alternancia A/B, limpieza
tras excepciones. `SecurityIsolationIT` complementa esta prueba con permisos y
cuentas deshabilitadas; el `verify` completo ejecuta ambas y las pruebas posteriores.

Si Testcontainers falla al localizar Docker, revisar daemon, permisos y reporte antes
de cambiar versiones. La investigación de compatibilidad está en research.md R10.
No se considera aprobado M1 por desactivar o saltar estos tests.

## Arranque de demostración local

Con configuración y migraciones completas, arrancar en una terminal:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo,local-docs
```

El arranque aplica Flyway y valida el esquema, sin DDL de Hibernate. El perfil demo
requiere SEED_DEMO_ENABLED=true. El seed crea Poly con Funcional, Boxeo y Crossfit y
los canales acordados; nombres y precios aparecen solo como datos ficticios.
Uno de los planes ficticios de Funcional usará precio 100.25 y duración 2 SEMANAS
para reproducir el circuito monetario. Las otras disciplinas también tienen al
menos una membresía ficticia de precio y duración positivos. No es una demo pública.

En otra terminal, cargar el mismo entorno local y configurar la URL:

```bash
set -a
. ./.env
set +a
BASE_URL=http://localhost:8080/api/v1
ADMIN_TOKEN=$(jq -n --arg email "$SEED_ADMIN_EMAIL" --arg password "$SEED_ADMIN_PASSWORD" '{email:$email,password:$password}' |
  curl --fail-with-body -sS "$BASE_URL/auth/login" -H 'Content-Type: application/json' --data-binary @- |
  jq -er '.accessToken')
```

No usar curl -v ni imprimir los tokens. `/auth/me` debe mostrar ADMIN y su gimnasio;
no se pide un identificador de tenant para iniciar sesión. OpenAPI se expone en
/v3/api-docs y Swagger UI solo en el perfil local de documentación, no en producción
sin una decisión explícita en la feature de despliegue.

## Circuito funcional reproducible

### 1. Alta mínima de cliente

```bash
CLIENT_ID=$(curl --fail-with-body -sS "$BASE_URL/clientes"   -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json'   --data '{"dni":"12345678","nombre":"Cliente","apellido":"Ficticio"}' | jq -er '.id')
```

Esperado: 201, sin teléfono/canal/dirección/localidad obligatorios. Una repetición de
ese DNI en el mismo gimnasio produce 409 y no sobrescribe al cliente. Si ya existe
por una ejecución anterior, recuperarlo con GET clientes?dni=12345678 en lugar de
repetir el alta. Nombres, DNI e importes de esta guía son ejemplos ficticios.

### 2. Seleccionar membresía y registrar compra

```bash
MEMBERSHIP_ID=$(curl --fail-with-body -sS "$BASE_URL/membresias?size=100"   -H "Authorization: Bearer $ADMIN_TOKEN" |
  jq -er '.content[] | select(.precio == "100.25" and .duracion == 2 and .unidadDuracion == "SEMANAS") | .id' | head -n 1)
ORDER_ID=$(jq -n --arg cliente "$CLIENT_ID" --arg membresia "$MEMBERSHIP_ID"   '{clienteId:$cliente,membresiaId:$membresia,cantidad:2}' |
  curl --fail-with-body -sS "$BASE_URL/pedidos" -H "Authorization: Bearer $ADMIN_TOKEN"     -H 'Content-Type: application/json' --data-binary @- | jq -er '.id')
```

Esperado: un detalle, precio 100.25, total 200.50, deuda 200.50, PENDIENTE y vencimiento
hoy + cuatro semanas. Modificar precio/duración del catálogo no altera esa compra.
Volver el catálogo al ejemplo antes de repetir la demostración.

### 3. Cobrar parcial y luego con exceso

```bash
curl --fail-with-body -sS "$BASE_URL/pedidos/$ORDER_ID/pagos"   -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json'   --data '{"montoRecibido":"50.25"}' | jq .
LAST_PAYMENT=$(curl --fail-with-body -sS "$BASE_URL/pedidos/$ORDER_ID/pagos"   -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json'   --data '{"montoRecibido":"200.00"}')
LAST_PAYMENT_ID=$(printf '%s' "$LAST_PAYMENT" | jq -er '.pago.id')
printf '%s' "$LAST_PAYMENT" | jq '{vuelto,deudaRestante,estadoPago}'
```

Esperado después del primer pago: deuda 150.25/PENDIENTE. Después del segundo:
aplicado 150.25, vuelto 49.75, deuda cero/PAGADO. Ninguno cambia el vencimiento.
Si una respuesta de POST se pierde, consultar el historial antes de repetir el cobro.

### 4. Anular conservando evidencia

```bash
curl --fail-with-body -sS -X PUT "$BASE_URL/pagos/$LAST_PAYMENT_ID/anulacion"   -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
curl --fail-with-body -sS "$BASE_URL/pedidos/$ORDER_ID"   -H "Authorization: Bearer $ADMIN_TOKEN" | jq '{deuda,estadoPago,vigencia,fechaVencimiento}'
curl --fail-with-body -sS "$BASE_URL/pedidos/$ORDER_ID/pagos"   -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

Esperado: deuda 150.25/PENDIENTE, ambos pagos visibles, el segundo ANULADO con actor
ADMIN y timestamp. Repetir PUT devuelve 200 con la misma evidencia y no suma deuda.

### 5. Renovación y calendario

Crear otro pedido con los mismos cliente/membresía/cantidad y pedidoAnteriorId=ORDER_ID.
Si el antecedente está vigente a la fecha comercial, su vencimiento es la base.
Intentar de nuevo desde ese mismo antecedente debe dar 409; continuar desde el
sucesor. Las pruebas fijan Clock para validar 2026-11-10 + 6 semanas = 2026-12-22,
cierre de mes, bisiestos, día de vencimiento incluido y rechazo de fechas futuras.
La renovación conserva las compras anteriores y no depende de cuánto se pagó.

## Aislamiento y permisos

Estas comprobaciones se ejecutan con dos gimnasios por las pruebas y pueden repetirse
por API después de crearlos con SUPERADMIN. Login de SUPERADMIN usa sus variables de
bootstrap; no puede consultar negocio ni actuar como tenant root.

| Escenario | Resultado esperado |
|---|---|
| SUPERADMIN crea gimnasio B y primer ADMIN | 201, ambos o ninguno |
| B registra DNI 12345678 ya usado en A | 201, identidades independientes |
| ADMIN de A consulta/modifica un ID de B | 404 genérico, sin datos de B |
| A crea pedido con cliente/membresía de B | 404, sin pedido parcial |
| A registra pago de una compra de B | 404, sin pago |
| SUPERADMIN consulta clientes/pedidos/pagos | 403, sin datos |
| RECEPCION crea/edita cliente, compra y cobra | Permitido, solo dentro del propio gimnasio |
| RECEPCION configura o anula un pago | 403 |
| ADMIN crea recepción con email global ya usado | 409 genérico |
| Cuenta deshabilitada usa JWT no vencido | 401 en nueva solicitud |
| Usuario fuerza gimnasioId/total/rol en un request de negocio | 400 por propiedad no admitida, sin cambio de contexto |
| DNI inválido, precio o pago ≤0, escala monetaria incorrecta | 400 application/problem+json |
| Dos renovaciones del mismo antecedente | Un alta y un 409, un único sucesor |
| Cobros concurrentes del mismo saldo | Aplicados suman como máximo el total; deuda consistente |

Verificar que los errores incluyan type/title/status/detail/instance y que 404 ajeno
sea indistinguible del inexistente. No se inspecciona la pertenencia de un recurso
para responder a un rol que ni siquiera tiene permiso para esa operación.

## Cierre y evidencia

Antes de declarar terminada la feature: repetir verify/CI, registrar conteo real de
tests y cobertura en el plan de producto, revisar OpenAPI, pasos de README y que no
se hayan añadido pantallas/reservas ni credenciales. El ejemplo manual no sustituye
las pruebas de aislamiento, constraints y concurrencia.

Comprobar migraciones desde vacío y desde la anterior versión aplicable. Reiniciar
con el seed activado no debe duplicar datos ni cambiar contraseñas o movimientos.
Los riesgos de soporte de Spring Boot 3 y la exposición pública se revisan antes de
la feature 4; este documento no afirma un despliegue ni soporte OSS vigente.
