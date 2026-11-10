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

## Datos de demostración locales

Copiá `.env.example` a `.env` y completá las variables de base, JWT y bootstrap con
valores propios. Para cargar los ejemplos ficticios, configurá también
`SEED_DEMO_ENABLED=true`, `SEED_ADMIN_EMAIL` y `SEED_ADMIN_PASSWORD`.
El administrador inicial y el superadmin se crean solo si no existen; en reinicios
el seed requiere las mismas credenciales del administrador y no las sobrescribe.

```bash
set -a
. ./.env
set +a
docker compose up -d db
cd polygim-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo,local-docs
```

Los ejemplos son datos de prueba, no una importación del sistema anterior. La guía
del circuito completo está en `specs/001-gym-management-core/quickstart.md`.
