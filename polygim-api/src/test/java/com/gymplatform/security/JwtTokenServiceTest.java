package com.gymplatform.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.assertj.core.api.Assertions.*;

class JwtTokenServiceTest {
    @Test
    void issuesSignedHs256TokenWithIssuerAudienceSubjectAndExpiry() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String encoded = Base64.getEncoder().encodeToString(key);
        UUID account = UUID.randomUUID();
        var service = new JwtTokenService(encoded, "https://issuer.example.org", "audience-test", Duration.ofMinutes(15), Clock.systemUTC());
        String token = service.issue(account);
        var decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        var jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo(account.toString());
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://issuer.example.org");
        assertThat(jwt.getAudience()).containsExactly("audience-test");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        byte[] otherKey = new byte[32];
        new SecureRandom().nextBytes(otherKey);
        var otherDecoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(otherKey, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        assertThatThrownBy(() -> otherDecoder.decode(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void refusesShortKey() {
        var key = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new JwtTokenService(key, "issuer-test", "audience-test", Duration.ofMinutes(15), Clock.systemUTC()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
