package com.gymplatform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.integration.support.PostgresIntegrationSupport;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.security.JwtTokenService;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class GimnasioApiIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtTokenService tokens;
    @Autowired GymProvisioningExecutor provisioning;
    UUID platform;
    String platformToken;

    @BeforeEach
    void setupPlatformIdentity() throws Exception {
        platform = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'SUPERADMIN',true,null,now(),now(),0)")) {
            statement.setObject(1, platform);
            statement.setString(2, platform + "@example.org");
            statement.setString(3, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()));
            statement.executeUpdate();
        }
        platformToken = tokens.issue(platform);
    }

    private String request(String name, String email, String password) throws Exception {
        return mapper.writeValueAsString(Map.of("nombre", name, "logoUrl", "https://example.org/logo", "colorPrimario", "#123456",
                "administradorInicial", Map.of("email", email, "password", password)));
    }

    @Test
    void superadminCreatesAndReadsGymAndAdminWithoutExposingCredentials() throws Exception {
        String password = UUID.randomUUID().toString();
        String email = UUID.randomUUID() + "@example.org";
        var response = mvc.perform(post("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + platformToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request("Gimnasio " + UUID.randomUUID(), email, password)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.administradorInicial.rol").value("ADMIN"))
                .andExpect(jsonPath("$.administradorInicial.password").doesNotExist())
                .andExpect(jsonPath("$.administradorInicial.passwordHash").doesNotExist()).andReturn().getResponse();
        assertThat(response.getContentAsString()).doesNotContain(password);
        String id = mapper.readTree(response.getContentAsString()).path("gimnasio").path("id").asText();
        mvc.perform(get("/api/v1/plataforma/gimnasios/" + id).header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/v1/clientes").header("Authorization", "Bearer " + platformToken)).andExpect(status().isForbidden());
    }

    @Test
    void duplicateEmailRollsBackTheWholeGym(CapturedOutput output) throws Exception {
        String email = UUID.randomUUID() + "@example.org";
        mvc.perform(post("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + platformToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request("Primero " + UUID.randomUUID(), email, UUID.randomUUID().toString())))
                .andExpect(status().isCreated());
        String second = "Rollback " + UUID.randomUUID();
        mvc.perform(post("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + platformToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request(second, email, UUID.randomUUID().toString())))
                .andExpect(status().isConflict()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("select count(*) from gimnasio where nombre = ?")) {
            statement.setString(1, second);
            try (var result = statement.executeQuery()) { result.next(); assertThat(result.getLong(1)).isZero(); }
        }
        assertThat(output.getAll()).doesNotContain(email);
    }

    @Test
    void gymAdminCannotUsePlatformOperations() throws Exception {
        UUID gym = UUID.randomUUID(), admin = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(platform, null, "platform@example.org"), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try {
            provisioning.provision(gym, "Gym", "https://example.org/logo", "#123456", admin,
                    admin + "@example.org", PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()), 12);
        } finally { SecurityContextHolder.clearContext(); }
        mvc.perform(get("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + tokens.issue(admin)))
                .andExpect(status().isForbidden());
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("update cuenta_acceso set rol = 'RECEPCION' where id = ?")) {
            statement.setObject(1, admin); statement.executeUpdate();
        }
        mvc.perform(get("/api/v1/plataforma/gimnasios").header("Authorization", "Bearer " + tokens.issue(admin)))
                .andExpect(status().isForbidden());
    }
}
