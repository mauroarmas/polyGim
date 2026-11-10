package com.gymplatform.integration;

import com.gymplatform.integration.support.PostgresIntegrationSupport;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaMigrationIT extends PostgresIntegrationSupport {
    @Test
    void migratesFromBaselineToTenantIdentity() throws Exception {
        String schema = "migration_" + UUID.randomUUID().toString().replace("-", "");
        var initial = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target(MigrationVersion.fromVersion("1")).load();
        initial.migrate();
        assertThat(initial.info().current().getVersion().toString()).isEqualTo("1");

        var current = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target("2").load();
        current.migrate();
        assertThat(current.info().current().getVersion().toString()).isEqualTo("2");

        UUID gymA = UUID.randomUUID(), gymB = UUID.randomUUID(), account = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())) {
            connection.setSchema(schema);
            try (var statement = connection.prepareStatement(
                    "insert into gimnasio (id,nombre,logo_url,color_primario,created_at,updated_at,version) values (?,?,'https://example.org/logo','#123456',now(),now(),0)")) {
                for (UUID gym : new UUID[] {gymA, gymB}) {
                    statement.setObject(1, gym); statement.setString(2, "Fixture " + gym); statement.executeUpdate();
                }
            }
            String email = UUID.randomUUID() + "@example.org";
            try (var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'ADMIN',true,?,now(),now(),0)")) {
                statement.setObject(1, account); statement.setString(2, email); statement.setString(3, UUID.randomUUID().toString()); statement.setObject(4, gymA); statement.executeUpdate();
                statement.setObject(1, UUID.randomUUID());
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
            }
            try (var statement = connection.prepareStatement("insert into usuario_gimnasio (id,gimnasio_id,created_at) values (?,?,now())")) {
                statement.setObject(1, account); statement.setObject(2, gymB);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
                statement.setObject(2, gymA); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'SUPERADMIN',true,?,now(),now(),0)")) {
                statement.setObject(1, UUID.randomUUID()); statement.setString(2, UUID.randomUUID() + "@example.org");
                statement.setString(3, UUID.randomUUID().toString()); statement.setObject(4, gymA);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
            }
        }
        var catalogs = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target("3").load();
        catalogs.migrate();
        assertThat(catalogs.info().current().getVersion().toString()).isEqualTo("3");
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())) {
            connection.setSchema(schema);
            UUID discipline = UUID.randomUUID();
            try (var statement = connection.prepareStatement("insert into disciplina (id,gimnasio_id,nombre,created_at,updated_at,version) values (?,?,'A',now(),now(),0)")) {
                statement.setObject(1, discipline); statement.setObject(2, gymA); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("insert into membresia (id,gimnasio_id,disciplina_id,nombre,precio,duracion,unidad_duracion,created_at,updated_at,version) values (?,?,?,'Plan',?,?, 'DIAS',now(),now(),0)")) {
                statement.setObject(1, UUID.randomUUID()); statement.setObject(2, gymA); statement.setObject(3, discipline);
                statement.setBigDecimal(4, new java.math.BigDecimal("0.00")); statement.setInt(5, 1);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
                statement.setBigDecimal(4, new java.math.BigDecimal("1.00")); statement.setInt(5, 0);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
                statement.setInt(5, 1); statement.setObject(2, gymB);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
            }
        }
        var clients = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target("4").load();
        clients.migrate();
        assertThat(clients.info().current().getVersion().toString()).isEqualTo("4");
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())) {
            connection.setSchema(schema);
            UUID channel = UUID.randomUUID();
            try (var statement = connection.prepareStatement("insert into canal_llegada (id,gimnasio_id,nombre,created_at,updated_at,version) values (?,?,'Canal',now(),now(),0)")) {
                statement.setObject(1, channel); statement.setObject(2, gymA); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("insert into cliente (id,gimnasio_id,dni,nombre,apellido,canal_llegada_id,fecha_ingreso,created_at,updated_at,version) values (?,?,?,'A','B',?,current_date,now(),now(),0)")) {
                statement.setObject(1, UUID.randomUUID()); statement.setObject(2, gymA); statement.setString(3, "0123456"); statement.setObject(4, channel); statement.executeUpdate();
                statement.setObject(1, UUID.randomUUID());
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
                statement.setObject(2, gymB); statement.setObject(4, null); statement.executeUpdate();
                statement.setObject(1, UUID.randomUUID()); statement.setString(3, "12345678"); statement.setObject(4, channel);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
            }
        }
        var orders = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
                .schemas(schema).target("5").load();
        orders.migrate();
        assertThat(orders.info().current().getVersion().toString()).isEqualTo("5");
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())) {
            connection.setSchema(schema);
            UUID discipline = UUID.randomUUID(), membership = UUID.randomUUID(), client;
            try (var statement = connection.prepareStatement("insert into disciplina (id,gimnasio_id,nombre,created_at,updated_at,version) values (?,?,'Otra',now(),now(),0)")) {
                statement.setObject(1, discipline); statement.setObject(2, gymA); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("insert into membresia (id,gimnasio_id,disciplina_id,nombre,precio,duracion,unidad_duracion,created_at,updated_at,version) values (?,?,?,'Plan',1.00,1,'DIAS',now(),now(),0)")) {
                statement.setObject(1, membership); statement.setObject(2, gymA); statement.setObject(3, discipline); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("select id from cliente where gimnasio_id = ?")) {
                statement.setObject(1, gymA); try (var result = statement.executeQuery()) { result.next(); client = result.getObject(1, UUID.class); }
            }
            UUID previous = UUID.randomUUID();
            try (var statement = connection.prepareStatement("insert into pedido (id,gimnasio_id,cliente_id,membresia_id,cantidad,precio_unitario,duracion_membresia,unidad_duracion,total,fecha_compra,fecha_base_vencimiento,fecha_vencimiento,pedido_anterior_id,registrado_por_id,created_at) values (?,?,?,?,1,1.00,1,'DIAS',1.00,current_date,current_date,current_date+1,?,?,now())")) {
                statement.setObject(1, previous); statement.setObject(2, gymA); statement.setObject(3, client); statement.setObject(4, membership);
                statement.setObject(5, null); statement.setObject(6, account); statement.executeUpdate();
                statement.setObject(1, UUID.randomUUID()); statement.setObject(5, previous); statement.executeUpdate();
                statement.setObject(1, UUID.randomUUID());
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(SQLException.class);
            }
        }
    }
}
