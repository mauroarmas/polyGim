package com.gymplatform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.integration.support.PostgresIntegrationSupport;
import com.gymplatform.seed.DemoSeedLoader;
import com.gymplatform.seed.SeedDefinition;
import com.gymplatform.seed.DemoSeedConfiguration;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.security.JwtTokenService;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DemoSeedIT extends PostgresIntegrationSupport {
    @Autowired DemoSeedLoader loader;
    @Autowired JwtTokenService tokens;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ApplicationContext context;
    @Autowired GymProvisioningExecutor provisioning;
    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    private void asPlatform() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
    }
    private SeedDefinition.Gym gym(UUID id, UUID admin, UUID discipline, UUID membership) {
        return new SeedDefinition.Gym(id, admin, "Gimnasio de prueba", "https://example.org/logo", "#123456",
                List.of(new SeedDefinition.Named(discipline, "Actividad")), List.of(new SeedDefinition.Named(UUID.randomUUID(), "Canal")),
                List.of(new SeedDefinition.Membership(membership, discipline, "Plan", "100.25", 2, "SEMANAS")));
    }
    private String json(Object body) throws Exception { return mapper.writeValueAsString(body); }
    private UUID create(String path, String token, Object body) throws Exception {
        var response = mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andExpect(status().isCreated()).andReturn().getResponse();
        return UUID.fromString(mapper.readTree(response.getContentAsString()).path("id").asText());
    }
    @Test void demoRunnerIsAbsentByDefault() {
        assertThat(context.getBeansOfType(DemoSeedConfiguration.class)).isEmpty();
    }
    @Test void seedIsOptInIdempotentAndDoesNotOverwritePurchasesPricesOrCredentials() throws Exception {
        UUID gym = UUID.randomUUID(), admin = UUID.randomUUID(), discipline = UUID.randomUUID(), membership = UUID.randomUUID();
        String email = UUID.randomUUID() + "@example.org", password = UUID.randomUUID().toString();
        var definition = new SeedDefinition(List.of(gym(gym, admin, discipline, membership)));
        asPlatform(); loader.load(definition, email, password, 12);
        String originalHash = passwordHash(admin);
        asPlatform(); assertThatThrownBy(() -> loader.load(definition, email, UUID.randomUUID().toString(), 12))
                .isInstanceOf(IllegalStateException.class);
        assertThat(passwordHash(admin)).isEqualTo(originalHash);
        String token = tokens.issue(admin);
        mvc.perform(put("/api/v1/membresias/" + membership).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Modificado", "precio", "55.00", "duracion", 1, "unidadDuracion", "DIAS"))))
                .andExpect(status().isOk());
        UUID client = create("clientes", token, Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Perez"));
        UUID order = create("pedidos", token, Map.of("clienteId", client, "membresiaId", membership, "cantidad", 1));
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("montoRecibido", "5.00")))).andExpect(status().isCreated());
        asPlatform(); loader.load(definition, email, password, 12);
        assertThat(passwordHash(admin)).isEqualTo(originalHash);
        mvc.perform(get("/api/v1/membresias/" + membership).header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value("55.00"));
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.deuda").value("50.00"));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/disciplinas").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        UUID secondGym = UUID.randomUUID(), secondAdmin = UUID.randomUUID();
        asPlatform();
        provisioning.provision(secondGym, "Segundo", "https://example.org/logo", "#654321", secondAdmin,
                secondAdmin + "@example.org", org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()), 4);
        String secondToken = tokens.issue(secondAdmin);
        UUID secondDiscipline = create("disciplinas", secondToken, Map.of("nombre", "Otra actividad"));
        create("membresias", secondToken, Map.of("disciplinaId", secondDiscipline, "nombre", "Otro plan", "precio", "25.00", "duracion", 5, "unidadDuracion", "DIAS"));
        mvc.perform(get("/api/v1/disciplinas/" + secondDiscipline).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/configuracion").header("Authorization", "Bearer " + secondToken)).andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadMaximaCompra").value(4));
    }
    private String passwordHash(UUID account) throws Exception {
        try (var connection = java.sql.DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("select password_hash from cuenta_acceso where id = ?")) {
            statement.setObject(1, account);
            try (var result = statement.executeQuery()) { result.next(); return result.getString(1); }
        }
    }
    @Test void conflictingRowsRollBackTheTenantDataBatch() throws Exception {
        UUID gym = UUID.randomUUID(), admin = UUID.randomUUID(), discipline = UUID.randomUUID(), membership = UUID.randomUUID();
        String email = UUID.randomUUID() + "@example.org", password = UUID.randomUUID().toString();
        asPlatform(); loader.load(new SeedDefinition(List.of(gym(gym, admin, discipline, membership))), email, password, 12);
        var conflicting = new SeedDefinition.Gym(gym, admin, "Gym", "https://example.org/logo", "#123456",
                List.of(new SeedDefinition.Named(UUID.randomUUID(), "Nueva"), new SeedDefinition.Named(UUID.randomUUID(), "Actividad")), List.of(), List.of());
        asPlatform(); assertThatThrownBy(() -> loader.load(new SeedDefinition(List.of(conflicting)), email, password, 12)).isInstanceOf(RuntimeException.class);
        mvc.perform(get("/api/v1/disciplinas").header("Authorization", "Bearer " + tokens.issue(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }
}
