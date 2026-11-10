package com.gymplatform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.integration.support.PostgresIntegrationSupport;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.security.JwtTokenService;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogoApiIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtTokenService tokens;
    @Autowired GymProvisioningExecutor provisioning;
    String adminA, adminB, reception;
    UUID gymA, gymB;
    @BeforeEach
    void setup() throws Exception {
        gymA = UUID.randomUUID(); gymB = UUID.randomUUID();
        UUID accountA = UUID.randomUUID(), accountB = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try { provision(gymA, accountA); provision(gymB, accountB); }
        finally { SecurityContextHolder.clearContext(); }
        adminA = tokens.issue(accountA); adminB = tokens.issue(accountB);
        var response = mvc.perform(post("/api/v1/usuarios-recepcion").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()))))
                .andExpect(status().isCreated()).andReturn();
        reception = tokens.issue(UUID.fromString(mapper.readTree(response.getResponse().getContentAsString()).path("id").asText()));
    }
    private void provision(UUID gym, UUID admin) {
        String hash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString());
        provisioning.provision(gym, "Gym " + gym, "https://example.org/logo", "#123456", admin, admin + "@example.org", hash, 12);
    }
    private String json(Object value) throws Exception { return mapper.writeValueAsString(value); }
    private UUID create(String path, String token, Map<String, ?> body) throws Exception {
        var response = mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(response.getResponse().getContentAsString()).path("id").asText());
    }
    @Test
    void threeCatalogsSupportOwnCrudAndIsolateIdsNamesAndPages() throws Exception {
        for (String path : List.of("disciplinas", "canales-llegada", "localidades")) {
            UUID a = create(path, adminA, Map.of("nombre", "Ejemplo"));
            create(path, adminB, Map.of("nombre", "Ejemplo"));
            mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("nombre", "  eJeMpLo  ")))).andExpect(status().isConflict());
            mvc.perform(get("/api/v1/" + path + "/" + a).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk());
            mvc.perform(get("/api/v1/" + path + "/" + a).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/" + path).header("Authorization", "Bearer " + reception))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
            mvc.perform(put("/api/v1/" + path + "/" + a).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("nombre", "Nuevo")))).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Nuevo"));
            mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + reception).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("nombre", "No permitido")))).andExpect(status().isForbidden());
            mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("nombre", "Otro", "email", "x@example.org")))).andExpect(status().isBadRequest());
        }
    }
    @Test
    void membershipsAndConfigurationRespectOwnershipPriceAndRole() throws Exception {
        UUID disciplineA = create("disciplinas", adminA, Map.of("nombre", "A"));
        UUID disciplineB = create("disciplinas", adminB, Map.of("nombre", "B"));
        Map<String, Object> body = Map.of("disciplinaId", disciplineA.toString(), "nombre", "Plan", "precio", "100.25", "duracion", 2, "unidadDuracion", "SEMANAS");
        UUID member = create("membresias", adminA, body);
        create("membresias", adminB, Map.of("disciplinaId", disciplineB.toString(), "nombre", "Otro plan", "precio", "35.50", "duracion", 3, "unidadDuracion", "MESES"));
        mvc.perform(get("/api/v1/membresias/" + member).header("Authorization", "Bearer " + reception))
                .andExpect(status().isOk()).andExpect(jsonPath("$.precio").value("100.25"));
        mvc.perform(get("/api/v1/membresias/" + member).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/membresias?disciplinaId=" + disciplineA).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/membresias?disciplinaId=" + disciplineA).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(post("/api/v1/membresias").header("Authorization", "Bearer " + adminB).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/membresias/" + member).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("disciplinaId", disciplineB.toString(), "nombre", "Plan", "precio", "10.00", "duracion", 1, "unidadDuracion", "DIAS"))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/membresias/" + member).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Plan nuevo", "precio", "20.00", "duracion", 1, "unidadDuracion", "MESES"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.disciplinaId").value(disciplineA.toString()));
        for (String invalid : List.of("0.00", "1.001", "-1.00")) {
            mvc.perform(post("/api/v1/membresias").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("disciplinaId", disciplineA.toString(), "nombre", "Plan 2", "precio", invalid, "duracion", 1, "unidadDuracion", "DIAS"))))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/v1/membresias").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("disciplinaId", disciplineA.toString(), "nombre", "Plan 3", "precio", new java.math.BigDecimal("1.00"), "duracion", 1, "unidadDuracion", "DIAS"))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/configuracion").header("Authorization", "Bearer " + reception)).andExpect(status().isOk()).andExpect(jsonPath("$.cantidadMaximaCompra").value(12));
        mvc.perform(put("/api/v1/configuracion").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("cantidadMaximaCompra", 50)))).andExpect(status().isOk()).andExpect(jsonPath("$.cantidadMaximaCompra").value(50));
        mvc.perform(get("/api/v1/configuracion").header("Authorization", "Bearer " + adminB)).andExpect(status().isOk()).andExpect(jsonPath("$.cantidadMaximaCompra").value(12));
        mvc.perform(put("/api/v1/configuracion").header("Authorization", "Bearer " + reception).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("cantidadMaximaCompra", 5)))).andExpect(status().isForbidden());
    }
}
