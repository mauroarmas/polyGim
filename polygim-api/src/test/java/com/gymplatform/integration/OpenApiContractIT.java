package com.gymplatform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.integration.support.PostgresIntegrationSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-docs")
class OpenApiContractIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    private JsonNode approved;
    private JsonNode generated;

    @Test void allOperationsSchemasAndProblemsMatchTheApprovedContract() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        generated = mapper.readTree(json);
        try (var input = Files.newInputStream(Path.of("..", "specs", "001-gym-management-core", "contracts", "openapi.yaml"))) {
            approved = mapper.valueToTree(new Yaml(new SafeConstructor(new LoaderOptions())).load(input));
        }
        assertThat(generated.path("openapi").asText()).startsWith("3.0.");
        assertThat(names(generated.path("paths"))).isEqualTo(names(approved.path("paths")));
        assertThat(generated.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(generated.at("/paths/~1api~1v1~1auth~1login/post/security").isEmpty()).isTrue();
        int count = 0;
        for (var path : iterable(approved.path("paths").fieldNames())) {
            JsonNode expectedPath = approved.path("paths").path(path), actualPath = generated.path("paths").path(path);
            for (String method : List.of("get", "post", "put", "delete", "patch")) {
                if (!expectedPath.has(method)) { assertThat(actualPath.has(method)).as(method + " " + path).isFalse(); continue; }
                count++;
                JsonNode expected = expectedPath.path(method), actual = actualPath.path(method);
                String label = method.toUpperCase() + " " + path;
                assertThat(actual.path("operationId").asText()).as(label).isEqualTo(expected.path("operationId").asText());
                for (JsonNode reference : expected.path("parameters")) {
                    JsonNode parameter = resolve(approved, reference);
                    JsonNode actualParameter = null;
                    for (JsonNode candidate : actual.path("parameters"))
                        if (candidate.path("name").equals(parameter.path("name")) && candidate.path("in").equals(parameter.path("in"))) actualParameter = candidate;
                    assertThat(actualParameter).as(label + " parámetro " + parameter.path("name").asText()).isNotNull();
                    if (parameter.path("required").asBoolean(false)) assertThat(actualParameter.path("required").asBoolean()).as(label).isTrue();
                    compareSchema(parameter.path("schema"), actualParameter.path("schema"), label + " parámetro " + parameter.path("name").asText(), true);
                }
                if (expected.has("requestBody")) {
                    assertThat(actual.at("/requestBody/required").asBoolean()).as(label).isTrue();
                    compareSchema(expected.at("/requestBody/content/application~1json/schema"),
                            actual.at("/requestBody/content/application~1json/schema"), label + " request", true);
                }
                for (String code : iterable(expected.path("responses").fieldNames())) {
                    JsonNode expectedResponse = resolve(approved, expected.path("responses").path(code));
                    JsonNode actualResponse = actual.path("responses").path(code);
                    assertThat(actualResponse.isMissingNode()).as(label + " respuesta " + code).isFalse();
                    String media = code.startsWith("2") ? "application/json" : "application/problem+json";
                    compareSchema(expectedResponse.path("content").path(media).path("schema"),
                            actualResponse.path("content").path(media).path("schema"), label + " respuesta " + code, false);
                    if ("401".equals(code)) assertThat(actualResponse.path("headers").has("WWW-Authenticate")).as(label).isTrue();
                }
            }
        }
        assertThat(count).isEqualTo(36);
    }
    private void compareSchema(JsonNode expectedReference, JsonNode actualReference, String label, boolean request) {
        assertThat(actualReference.isMissingNode()).as(label + " schema").isFalse();
        JsonNode expected = resolve(approved, expectedReference), actual = resolve(generated, actualReference);
        for (String key : List.of("type", "format", "pattern", "enum", "minimum", "maximum", "minLength", "maxLength", "default"))
            if (expected.has(key)) assertThat(actual.path(key)).as(label + "." + key).isEqualTo(expected.path(key));
        if (expected.path("nullable").asBoolean(false))
            assertThat(actual.path("nullable").asBoolean(false) || actualReference.path("nullable").asBoolean(false)).as(label + ".nullable").isTrue();
        if (expected.has("additionalProperties") && expected.path("additionalProperties").isBoolean())
            assertThat(actual.path("additionalProperties")).as(label + ".additionalProperties").isEqualTo(expected.path("additionalProperties"));
        if (expected.has("required")) {
            Set<String> expectedRequired = values(expected.path("required")), actualRequired = values(actual.path("required"));
            if (request) assertThat(actualRequired).as(label + ".required").isEqualTo(expectedRequired);
            else assertThat(actualRequired).as(label + ".required").containsAll(expectedRequired);
        }
        if (expected.has("properties")) {
            assertThat(names(actual.path("properties"))).as(label + ".properties").isEqualTo(names(expected.path("properties")));
            for (String name : iterable(expected.path("properties").fieldNames()))
                compareSchema(expected.path("properties").path(name), actual.path("properties").path(name), label + "." + name, request);
        }
        if (expected.has("items")) compareSchema(expected.path("items"), actual.path("items"), label + "[]", request);
    }
    private JsonNode resolve(JsonNode document, JsonNode value) {
        while (value.has("$ref")) value = document.at(value.path("$ref").asText().substring(1));
        return value;
    }
    private static Set<String> names(JsonNode object) {
        Set<String> names = new HashSet<>(); object.fieldNames().forEachRemaining(names::add); return names;
    }
    private static Set<String> values(JsonNode array) {
        Set<String> values = new HashSet<>(); array.forEach(item -> values.add(item.asText())); return values;
    }
    private static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) { return () -> iterator; }
}
