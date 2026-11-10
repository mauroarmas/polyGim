package com.gymplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.headers.Header;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local-docs")
public class OpenApiConfig {
    @Bean OpenAPI gymOpenApi() {
        return new OpenAPI().info(new Info().title("API de gestión multi-gimnasio").version("1.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }
    @Bean OpenApiCustomizer publicLogin() {
        return api -> {
            var path = api.getPaths().get("/api/v1/auth/login");
            if (path != null && path.getPost() != null) path.getPost().setSecurity(List.of());
            var fieldError = new ObjectSchema().addProperty("field", new StringSchema()).addProperty("message", new StringSchema());
            fieldError.setRequired(List.of("field", "message")); fieldError.setAdditionalProperties(false);
            var problem = new ObjectSchema().addProperty("type", new StringSchema().format("uri"))
                    .addProperty("title", new StringSchema()).addProperty("status", new IntegerSchema())
                    .addProperty("detail", new StringSchema()).addProperty("instance", new StringSchema().format("uri"))
                    .addProperty("code", new StringSchema().maxLength(100)).addProperty("errors", new ArraySchema().items(fieldError));
            problem.setRequired(List.of("type", "title", "status", "detail", "instance", "code"));
            problem.setAdditionalProperties(false);
            api.getComponents().addSchemas("Problem", problem);
            api.getComponents().getSchemas().forEach((name, schema) -> {
                if (name.startsWith("PageResponse")) {
                    schema.setRequired(List.of("content", "page", "size", "totalElements", "totalPages"));
                    schema.setAdditionalProperties(false);
                    bounds(schema, "page", 0, Integer.MAX_VALUE);
                    bounds(schema, "size", 1, 100);
                    bounds(schema, "totalElements", 0, null);
                    bounds(schema, "totalPages", 0, Integer.MAX_VALUE);
                }
            });
            describeConstraints(api);
            api.getPaths().forEach((url, item) -> item.readOperationsMap().forEach((method, operation) -> {
                var responses = operation.getResponses();
                ApiResponse success = responses.get("200");
                if (success != null && success.getContent() != null && success.getContent().containsKey("*/*")) {
                    var json = success.getContent().remove("*/*"); success.getContent().addMediaType("application/json", json);
                }
                if (method == PathItem.HttpMethod.POST && !url.equals("/api/v1/auth/login") && success != null) {
                    responses.remove("200"); responses.addApiResponse("201", success.description("Creado"));
                }
                if (method == PathItem.HttpMethod.PUT && url.equals("/api/v1/pagos/{id}/anulacion") && success != null)
                    responses.addApiResponse("201", new ApiResponse().description("Anulación creada").content(success.getContent()));
                var codes = url.equals("/api/v1/auth/login") ? List.of("400", "401", "500")
                        : List.of("400", "401", "403", "404", "409", "405", "415", "500");
                for (String code : codes) {
                    var response = new ApiResponse().description("Error de la solicitud")
                            .content(new Content().addMediaType("application/problem+json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/Problem"))));
                    if ("401".equals(code)) response.addHeaderObject("WWW-Authenticate", new Header().description("Desafío Bearer cuando corresponda").schema(new StringSchema()));
                    responses.addApiResponse(code, response);
                }
                if (operation.getParameters() != null) operation.getParameters().forEach(parameter -> {
                    if ("page".equals(parameter.getName())) parameter.setSchema(new IntegerSchema()._default(0).minimum(java.math.BigDecimal.ZERO));
                    if ("size".equals(parameter.getName())) parameter.setSchema(new IntegerSchema()._default(20).minimum(java.math.BigDecimal.ONE).maximum(java.math.BigDecimal.valueOf(100)));
                    if ("dni".equals(parameter.getName())) parameter.setSchema(new StringSchema().pattern("^[0-9]{7,8}$"));
                });
            }));
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void describeConstraints(OpenAPI api) {
        var schemas = api.getComponents().getSchemas();
        for (String name : List.of("GymCreateRequest", "CatalogWrite", "MembershipCreate", "MembershipUpdate", "ClientWrite", "Gym", "CatalogItem", "Membership", "Client")) {
            for (String field : name.equals("ClientWrite") || name.equals("Client") ? List.of("nombre", "apellido") : List.of("nombre")) {
                Schema property = (Schema) schemas.get(name).getProperties().get(field);
                property.setMinLength(1); property.setMaxLength(100);
            }
        }
        ((Schema) schemas.get("LoginRequest").getProperties().get("password")).setMinLength(1);
        Schema email = (Schema) schemas.get("User").getProperties().get("email"); email.setFormat("email"); email.setMaxLength(254);
        for (String name : List.of("GymCreateRequest", "Gym")) {
            Schema logo = (Schema) schemas.get(name).getProperties().get("logoUrl");
            logo.setFormat("uri"); logo.setPattern("^https?://"); logo.setMaxLength(2048);
            ((Schema) schemas.get(name).getProperties().get("colorPrimario")).setPattern("^#[0-9A-Fa-f]{6}$");
        }
        for (String name : List.of("MembershipCreate", "MembershipUpdate", "Membership")) {
            ((Schema) schemas.get(name).getProperties().get("unidadDuracion")).setEnum(List.of("DIAS", "SEMANAS", "MESES"));
            bounds(schemas.get(name), "duracion", 1, Integer.MAX_VALUE);
        }
        bounds(schemas.get("Configuration"), "cantidadMaximaCompra", 1, Integer.MAX_VALUE);
        bounds(schemas.get("ConfigurationWrite"), "cantidadMaximaCompra", 1, Integer.MAX_VALUE);
        bounds(schemas.get("OrderCreate"), "cantidad", 1, Integer.MAX_VALUE);
        bounds(schemas.get("Order"), "cantidad", 1, Integer.MAX_VALUE);
        bounds(schemas.get("Order"), "duracionMembresia", 1, Integer.MAX_VALUE);
        bounds(schemas.get("TokenResponse"), "expiresIn", 1, Integer.MAX_VALUE);
        for (String name : List.of("ClientWrite", "Client")) {
            ((Schema) schemas.get(name).getProperties().get("dni")).setPattern("^[0-9]{7,8}$");
            ((Schema) schemas.get(name).getProperties().get("telefono")).setMaxLength(40);
            for (String field : List.of("telefono", "canalLlegadaId", "direccion", "localidadId"))
                ((Schema) schemas.get(name).getProperties().get(field)).setNullable(true);
        }
        schemas.get("Address").setNullable(true);
        ((Schema) schemas.get("Address").getProperties().get("calle")).setNullable(true);
        ((Schema) schemas.get("Address").getProperties().get("altura")).setNullable(true);
        ((Schema) schemas.get("OrderCreate").getProperties().get("pedidoAnteriorId")).setNullable(true);
        ((Schema) schemas.get("Order").getProperties().get("pedidoAnteriorId")).setNullable(true);
    }

    @SuppressWarnings("rawtypes")
    private static void bounds(Schema schema, String field, Integer min, Integer max) {
        Schema property = (Schema) schema.getProperties().get(field);
        if (min != null) property.setMinimum(java.math.BigDecimal.valueOf(min));
        if (max != null) property.setMaximum(java.math.BigDecimal.valueOf(max));
    }
}
