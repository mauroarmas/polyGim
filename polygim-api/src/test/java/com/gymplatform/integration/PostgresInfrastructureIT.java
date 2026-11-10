package com.gymplatform.integration;

import com.gymplatform.integration.support.PostgresIntegrationSupport;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresInfrastructureIT extends PostgresIntegrationSupport {
    @Test
    void migratesAnEmptyPostgresUsingFlyway() {
        String schema = "infra_" + java.util.UUID.randomUUID().toString().replace("-", "");
        var flyway = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target("1").load();
        flyway.migrate();
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("1");
    }
}
