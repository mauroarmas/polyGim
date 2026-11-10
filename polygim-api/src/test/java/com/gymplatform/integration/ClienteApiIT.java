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
class ClienteApiIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtTokenService tokens;
    @Autowired GymProvisioningExecutor provisioning;
    String adminA, adminB, reception;
    @BeforeEach void setup() throws Exception {
        UUID gymA = UUID.randomUUID(), gymB = UUID.randomUUID(), accountA = UUID.randomUUID(), accountB = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try { provision(gymA, accountA); provision(gymB, accountB); } finally { SecurityContextHolder.clearContext(); }
        adminA = tokens.issue(accountA); adminB = tokens.issue(accountB);
        var result = mvc.perform(post("/api/v1/usuarios-recepcion").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()))))
                .andExpect(status().isCreated()).andReturn();
        reception = tokens.issue(UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).path("id").asText()));
    }
    private void provision(UUID gym, UUID admin) {
        provisioning.provision(gym, "Gym " + gym, "https://example.org/logo", "#123456", admin, admin + "@example.org",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()), 12);
    }
    private String json(Object value) throws Exception { return mapper.writeValueAsString(value); }
    private UUID create(String path, String token, Map<String, ?> body) throws Exception {
        var response = mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(response.getResponse().getContentAsString()).path("id").asText());
    }
    @Test void minimalClientAndDniUniquenessAreScopedToGym() throws Exception {
        var body = Map.of("dni", "0123456", "nombre", "Ana", "apellido", "Perez");
        UUID clientA = create("clientes", reception, body);
        create("clientes", adminB, body);
        mvc.perform(post("/api/v1/clientes").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isConflict());
        UUID other = create("clientes", adminA, Map.of("dni", "7654321", "nombre", "Otra", "apellido", "Persona"));
        mvc.perform(put("/api/v1/clientes/" + other).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(body))).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/clientes/" + clientA).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/clientes/" + clientA).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dni").value("0123456")).andExpect(jsonPath("$.fechaIngreso").exists());
        mvc.perform(get("/api/v1/clientes?dni=0123456&page=0&size=20").header("Authorization", "Bearer " + reception))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/clientes?size=101").header("Authorization", "Bearer " + adminA)).andExpect(status().isBadRequest());
    }
    @Test void updatesPreserveIdentityAndClearOmittedOptionalFields() throws Exception {
        UUID channel = create("canales-llegada", adminA, Map.of("nombre", "Canal"));
        UUID city = create("localidades", adminA, Map.of("nombre", "Ciudad"));
        UUID client = create("clientes", adminA, Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Perez"));
        var full = Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Perez", "telefono", "123", "canalLlegadaId", channel.toString(),
                "localidadId", city.toString(), "direccion", Map.of("calle", "Calle", "altura", "10"));
        var updated = mvc.perform(put("/api/v1/clientes/" + client).header("Authorization", "Bearer " + reception)
                        .contentType(MediaType.APPLICATION_JSON).content(json(full)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(client.toString())).andExpect(jsonPath("$.direccion.calle").value("Calle"))
                .andReturn();
        String date = mapper.readTree(updated.getResponse().getContentAsString()).path("fechaIngreso").asText();
        mvc.perform(put("/api/v1/clientes/" + client).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Nuevo"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fechaIngreso").value(date))
                .andExpect(jsonPath("$.telefono").doesNotExist()).andExpect(jsonPath("$.direccion").doesNotExist());
    }
    @Test void foreignOptionalReferencesAndInvalidDniAreRejected() throws Exception {
        UUID foreignChannel = create("canales-llegada", adminB, Map.of("nombre", "Otro"));
        UUID foreignCity = create("localidades", adminB, Map.of("nombre", "Otro"));
        for (String field : List.of("canalLlegadaId", "localidadId")) {
            UUID foreign = field.equals("canalLlegadaId") ? foreignChannel : foreignCity;
            mvc.perform(post("/api/v1/clientes").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Perez", field, foreign.toString()))))
                    .andExpect(status().isNotFound());
        }
        for (Object invalid : List.of("123456", "123456789", 12345678)) {
            mvc.perform(post("/api/v1/clientes").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("dni", invalid, "nombre", "Ana", "apellido", "Perez"))))
                    .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
    }
}
