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
class ReceptionApiIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtTokenService tokens;
    @Autowired GymProvisioningExecutor provisioning;
    UUID gymA, gymB, adminA, adminB;
    String tokenA, tokenB;

    @BeforeEach
    void createGyms() {
        gymA = UUID.randomUUID(); gymB = UUID.randomUUID(); adminA = UUID.randomUUID(); adminB = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try { provision(gymA, adminA); provision(gymB, adminB); }
        finally { SecurityContextHolder.clearContext(); }
        tokenA = tokens.issue(adminA); tokenB = tokens.issue(adminB);
    }

    private void provision(UUID gym, UUID admin) {
        var hash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString());
        provisioning.provision(gym, "Gym " + gym, "https://example.org/logo", "#123456", admin, admin + "@example.org", hash, 12);
    }
    private String accountBody(String email) throws Exception {
        return mapper.writeValueAsString(Map.of("email", email, "password", UUID.randomUUID().toString()));
    }
    private UUID createReception(String adminToken, String email) throws Exception {
        var result = mvc.perform(post("/api/v1/usuarios-recepcion").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(accountBody(email)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("RECEPCION"))
                .andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).path("id").asText());
    }

    @Test
    void disableIsIdempotentAndRevokesAnExistingTokenWithoutReleasingEmail() throws Exception {
        String email = UUID.randomUUID() + "@example.org";
        UUID reception = createReception(tokenA, email);
        String receptionistToken = tokens.issue(reception);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + receptionistToken)).andExpect(status().isOk());
        for (int i = 0; i < 2; i++) {
            mvc.perform(put("/api/v1/usuarios-recepcion/" + reception + "/deshabilitacion").header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.habilitada").value(false));
        }
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + receptionistToken)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/usuarios-recepcion").header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON).content(accountBody(email)))
                .andExpect(status().isConflict());
    }

    @Test
    void foreignIdsAndAdminIdsAreNotFoundAndReceptionCannotManageAccounts() throws Exception {
        UUID reception = createReception(tokenA, UUID.randomUUID() + "@example.org");
        mvc.perform(put("/api/v1/usuarios-recepcion/" + reception + "/deshabilitacion").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/usuarios-recepcion/" + adminA + "/deshabilitacion").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
        String receptionistToken = tokens.issue(reception);
        mvc.perform(post("/api/v1/usuarios-recepcion").header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON).content(accountBody(UUID.randomUUID() + "@example.org")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/usuarios-recepcion/" + reception + "/deshabilitacion").header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }
}
