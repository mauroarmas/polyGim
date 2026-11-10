package com.gymplatform.integration;

import com.gymplatform.integration.support.PostgresIntegrationSupport;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresInfrastructureIT extends PostgresIntegrationSupport {
    @Test
    void migratesAnEmptyPostgresUsingFlyway() {
        var flyway = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .target("1").load();
        flyway.migrate();
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("1");
    }
}
