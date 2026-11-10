package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import com.gymplatform.security.CurrentAccountPrincipal;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Matriz transversal; las fechas límite y el seed tienen también suites específicas. */
@SpringBootTest
@AutoConfigureMockMvc
class FeatureAcceptanceIT extends OrderApiSupport {
    @Test void twoGymsAndRolesCompleteTheFinancialCircuitWithoutCrossTenantAccess() throws Exception {
        UUID platform = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'SUPERADMIN',true,null,now(),now(),0)")) {
            statement.setObject(1, platform); statement.setString(2, platform + "@example.org");
            statement.setString(3, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString())); statement.executeUpdate();
        }
        String superToken = tokens.issue(platform);
        var gymBody = Map.of("nombre", "Tercer gimnasio", "logoUrl", "https://example.org/logo", "colorPrimario", "#123456",
                "administradorInicial", Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()));
        mvc.perform(post("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + superToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json(gymBody))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(gymBody))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clientes").header("Authorization", "Bearer " + superToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clientes/" + clientA).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/clientes").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("dni", "12345678", "nombre", "Duplicado", "apellido", "Ficticio"))))
                .andExpect(status().isConflict());
        UUID receptionist = create("usuarios-recepcion", adminA, Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()));
        String receptionToken = tokens.issue(receptionist);
        mvc.perform(put("/api/v1/configuracion").header("Authorization", "Bearer " + receptionToken).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("cantidadMaximaCompra", 1)))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + receptionToken).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("clienteId", clientB, "membresiaId", membershipA, "cantidad", 1)))).andExpect(status().isNotFound());
        UUID order = create("pedidos", receptionToken, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 2));
        var initial = mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.precioUnitario").value("100.25")).andExpect(jsonPath("$.total").value("200.50")).andReturn();
        String expiry = mapper.readTree(initial.getResponse().getContentAsString()).path("fechaVencimiento").asText();
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + receptionToken).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("montoRecibido", "50.25")))).andExpect(status().isCreated()).andExpect(jsonPath("$.deudaRestante").value("150.25"));
        var last = mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + receptionToken).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("montoRecibido", "200.00")))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.vuelto").value("49.75")).andReturn();
        UUID payment = UUID.fromString(mapper.readTree(last.getResponse().getContentAsString()).path("pago").path("id").asText());
        mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + receptionToken)).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + adminA)).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.deuda").value("150.25")).andExpect(jsonPath("$.fechaVencimiento").value(expiry));
        mvc.perform(get("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/pedidos?clienteId=" + clientB).header("Authorization", "Bearer " + adminA)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1, "gimnasioId", gymB))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/pedidos/" + order)).andExpect(status().isUnauthorized());
    }
}
