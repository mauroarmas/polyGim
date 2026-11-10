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
