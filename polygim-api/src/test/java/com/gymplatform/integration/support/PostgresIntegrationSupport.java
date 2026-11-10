package com.gymplatform.integration.support;

import java.util.UUID;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresIntegrationSupport {
    protected static final String JWT_KEY = randomKey();
    private static final DockerImageName POSTGRES = DockerImageName.parse(
            "postgres:17.11@sha256:ff87138949ad9a46f32ff23bd6852b52ce024a13dffe9b4087be98292cdd3b4f")
            .asCompatibleSubstituteFor("postgres");

    protected static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>(POSTGRES)
            .withDatabaseName("gym_test")
            .withUsername("test_" + UUID.randomUUID().toString().replace("-", ""))
            .withPassword(UUID.randomUUID().toString());

    static { DATABASE.start(); }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("app.time-zone", () -> "UTC");
        registry.add("app.default-gym-max-quantity", () -> "12");
        registry.add("app.security.jwt-secret-base64", () -> JWT_KEY);
        registry.add("app.security.jwt-issuer", () -> "test-issuer");
        registry.add("app.security.jwt-audience", () -> "test-audience");
    }

    private static String randomKey() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
