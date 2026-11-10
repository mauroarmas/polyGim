# Plataforma de gestión de gimnasios

API multi-gimnasio en construcción. El proyecto nuevo está en `polygim-api/`.

## Entorno de desarrollo

Se requiere Java 21 y Docker. Maven se proporciona mediante el wrapper del proyecto.
Seleccioná un JDK 21 para la terminal del proyecto, sin cambiar el JDK global:

```bash
export JAVA_HOME=/ruta/a/tu/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
java -version
cd polygim-api
./mvnw -version
```

El desarrollo y sus verificaciones se registran en `docs/PLAN_DE_TRABAJO.md`.
La configuración sensible se suministra mediante variables de entorno. Partí de
`.env.example` para preparar tu entorno local; `.env` está excluido de Git. No se
utilizan las credenciales del sistema legacy.

Cada POST de compra o pago aceptado registra una operación nueva. Si se pierde la
respuesta, consultá el historial antes de decidir si corresponde volver a enviarlo;
el cliente no debe reintentar automáticamente un cobro de resultado desconocido.

## Levantar la aplicación con Docker Compose

Copiá `.env.example` a `.env` y completá las variables de base, JWT y bootstrap con
valores propios. Generá una clave JWT aleatoria de al menos 256 bits, por ejemplo con
`openssl rand -base64 32`. Para cargar los ejemplos ficticios, configurá también
`SPRING_PROFILES_ACTIVE=demo,local-docs`, `SEED_DEMO_ENABLED=true`, `SEED_ADMIN_EMAIL`
y `SEED_ADMIN_PASSWORD`. Las contraseñas de las cuentas deben tener al menos 12 caracteres.
El administrador inicial y el superadmin se crean solo si no existen; en reinicios
el seed requiere las mismas credenciales del administrador y no las sobrescribe.

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

La aplicación escucha en `http://localhost:8080` (ajustable con `APP_PORT`); Compose
espera a que PostgreSQL esté disponible y Flyway aplica las migraciones al arrancar.
`docker compose down` detiene los servicios y conserva la base en su volumen. Los
ejemplos son datos de prueba, no una importación del sistema anterior. La guía del
circuito completo está en `specs/001-gym-management-core/quickstart.md`.

Con un token de ADMIN o RECEPCION en `ADMIN_TOKEN`, una consulta de ejemplo es:

```bash
curl -sS http://localhost:8080/api/v1/configuracion \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Una respuesta posible es `{"cantidadMaximaCompra":12}`. Con el perfil `local-docs`
activo, la descripción de la API está en `/v3/api-docs` y la interfaz interactiva en
`/swagger-ui/index.html`.
