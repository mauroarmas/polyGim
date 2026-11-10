package com.gymplatform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.integration.support.PostgresIntegrationSupport;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoSeedEnabledIT extends PostgresIntegrationSupport {
    private static final String PLATFORM_EMAIL = UUID.randomUUID() + "@example.org";
    private static final String PLATFORM_PASSWORD = UUID.randomUUID().toString();
    private static final String ADMIN_EMAIL = UUID.randomUUID() + "@example.org";
    private static final String ADMIN_PASSWORD = UUID.randomUUID().toString();
    private static final String DEMO_SCHEMA = "demo_" + UUID.randomUUID().toString().replace("-", "");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @DynamicPropertySource
    static void demoProperties(DynamicPropertyRegistry properties) {
        properties.add("app.seed.enabled", () -> "true");
        properties.add("app.seed.admin-email", () -> ADMIN_EMAIL);
        properties.add("app.seed.admin-password", () -> ADMIN_PASSWORD);
        properties.add("app.bootstrap.superadmin-email", () -> PLATFORM_EMAIL);
        properties.add("app.bootstrap.superadmin-password", () -> PLATFORM_PASSWORD);
        properties.add("spring.flyway.schemas", () -> DEMO_SCHEMA);
        properties.add("spring.flyway.default-schema", () -> DEMO_SCHEMA);
        properties.add("spring.jpa.properties.hibernate.default_schema", () -> DEMO_SCHEMA);
    }
    @Test void enabledProfileLoadsDataAndAdminUsingEnvironmentProperties() throws Exception {
        var login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD))))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(login.getResponse().getContentAsString()).path("accessToken").asText();
        mvc.perform(get("/api/v1/disciplinas").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/v1/canales-llegada").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
        mvc.perform(get("/api/v1/membresias").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
