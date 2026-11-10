package com.gymplatform.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

public class JwtTokenService {
    private final NimbusJwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final Duration ttl;
    private final Clock clock;
    public JwtTokenService(String base64Key, String issuer, String audience, Duration ttl, Clock clock) {
        byte[] key;
        try { key = Base64.getDecoder().decode(base64Key); }
        catch (IllegalArgumentException | NullPointerException ex) { throw new IllegalArgumentException("Clave JWT inválida"); }
        if (key.length < 32 || issuer == null || issuer.isBlank() || audience == null || audience.isBlank()
                || ttl == null || ttl.isNegative() || ttl.isZero() || clock == null)
            throw new IllegalArgumentException("Configuración JWT inválida");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(key, "HmacSHA256")));
        this.issuer = issuer; this.audience = audience; this.ttl = ttl; this.clock = clock;
    }
    public String issue(UUID accountId) {
        Instant now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(issuer).audience(java.util.List.of(audience))
                .subject(accountId.toString()).issuedAt(now).expiresAt(now.plus(ttl)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
