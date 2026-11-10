package com.gymplatform.integration.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.security.JwtTokenService;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class OrderApiSupport extends PostgresIntegrationSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper mapper;
    @Autowired protected JwtTokenService tokens;
    @Autowired protected GymProvisioningExecutor provisioning;
    protected String adminA, adminB;
    protected UUID gymA, gymB, accountA, accountB, clientA, clientB, disciplineA, membershipA, membershipB;
    @BeforeEach void setUpOrderFixtures() throws Exception {
        gymA = UUID.randomUUID(); gymB = UUID.randomUUID(); accountA = UUID.randomUUID(); accountB = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try { provision(gymA, accountA); provision(gymB, accountB); } finally { SecurityContextHolder.clearContext(); }
        adminA = tokens.issue(accountA); adminB = tokens.issue(accountB);
        clientA = create("clientes", adminA, Map.of("dni", "12345678", "nombre", "Ana", "apellido", "Perez"));
        clientB = create("clientes", adminB, Map.of("dni", "12345678", "nombre", "Otra", "apellido", "Persona"));
        disciplineA = create("disciplinas", adminA, Map.of("nombre", "Actividad A"));
        UUID disciplineB = create("disciplinas", adminB, Map.of("nombre", "Actividad B"));
        membershipA = create("membresias", adminA, Map.of("disciplinaId", disciplineA, "nombre", "Plan A", "precio", "100.25", "duracion", 2, "unidadDuracion", "SEMANAS"));
        membershipB = create("membresias", adminB, Map.of("disciplinaId", disciplineB, "nombre", "Plan B", "precio", "10.00", "duracion", 1, "unidadDuracion", "MESES"));
    }
    private void provision(UUID gym, UUID account) {
        provisioning.provision(gym, "Gym " + gym, "https://example.org/logo", "#123456", account, account + "@example.org",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()), 12);
    }
    protected String json(Object value) throws Exception { return mapper.writeValueAsString(value); }
    protected UUID create(String path, String token, Map<String, ?> body) throws Exception {
        var result = mvc.perform(post("/api/v1/" + path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).path("id").asText());
    }
}
