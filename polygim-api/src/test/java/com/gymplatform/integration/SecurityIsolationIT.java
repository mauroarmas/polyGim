package com.gymplatform.integration;

import com.gymplatform.integration.support.PostgresIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.gymplatform.tenant.GymProvisioningExecutor;
import com.gymplatform.security.CurrentAccountPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIsolationIT extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired GymProvisioningExecutor provisioning;

    @Test
    void anonymousAndMalformedBearerAreRejectedAsProblems() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void loginDoesNotRequireTenantAndRejectsInvalidCredentialsGenerically() throws Exception {
        var email = java.util.UUID.randomUUID() + "@example.org";
        var password = java.util.UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registeredUserLogsInUsingEmailAndPasswordWithoutGymSelection() throws Exception {
        UUID gym = UUID.randomUUID(), account = UUID.randomUUID();
        String password = provision(gym, account);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + account + "@example.org\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void disabledAccountLosesAccessEvenWithUnexpiredToken() throws Exception {
        UUID gym = UUID.randomUUID(), account = UUID.randomUUID();
        provision(gym, account);
        String jwt = signedToken(account, Instant.now().plusSeconds(600));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jwt)).andExpect(status().isOk());
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("update cuenta_acceso set habilitada = false where id = ?")) {
            statement.setObject(1, account);
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void signedExpiredTokenIsRejected() throws Exception {
        UUID gym = UUID.randomUUID(), account = UUID.randomUUID();
        provision(gym, account);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + signedToken(account, Instant.now().minusSeconds(120))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signedTokensWithWrongIssuerOrAudienceAreRejected() throws Exception {
        UUID gym = UUID.randomUUID(), account = UUID.randomUUID();
        provision(gym, account);
        for (String jwt : new String[] {
                signedToken(account, Instant.now().plusSeconds(600), "wrong-issuer", "test-audience"),
                signedToken(account, Instant.now().plusSeconds(600), "test-issuer", "wrong-audience")}) {
            mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void unknownResourceUsesProblemNotFoundForAuthenticatedGymUser() throws Exception {
        UUID gym = UUID.randomUUID(), account = UUID.randomUUID();
        provision(gym, account);
        mvc.perform(get("/api/v1/nonexistent/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + signedToken(account, Instant.now().plusSeconds(600))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void superadminCannotAccessBusinessResources() throws Exception {
        UUID account = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'SUPERADMIN',true,null,now(),now(),0)")) {
            statement.setObject(1, account);
            statement.setString(2, account + "@example.org");
            statement.setString(3, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()));
            statement.executeUpdate();
        }
        String jwt = signedToken(account, Instant.now().plusSeconds(600));
        mvc.perform(get("/api/v1/clientes").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private String provision(UUID gym, UUID account) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try {
            var password = UUID.randomUUID().toString();
            var hash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
            provisioning.provision(gym, "Gimnasio " + gym, "https://example.org/logo", "#123456",
                    account, account + "@example.org", hash, 12);
            return password;
        } finally { SecurityContextHolder.clearContext(); }
    }

    private String signedToken(UUID subject, Instant expires) throws Exception {
        return signedToken(subject, expires, "test-issuer", "test-audience");
    }

    private String signedToken(UUID subject, Instant expires, String issuer, String audience) throws Exception {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String claims = "{\"sub\":\"" + subject + "\",\"iss\":\"" + issuer + "\",\"aud\":[\"" + audience + "\"],\"iat\":"
                + expires.minusSeconds(900).getEpochSecond() + ",\"exp\":" + expires.getEpochSecond() + "}";
        String payload = encoder.encodeToString(claims.getBytes(StandardCharsets.UTF_8));
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(JWT_KEY), "HmacSHA256"));
        return header + "." + payload + "." + encoder.encodeToString(mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8)));
    }
}
