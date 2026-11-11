package com.gymplatform.security;

import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
    @Bean JwtTokenService jwtTokenService(@Value("${app.security.jwt-secret-base64}") String key,
            @Value("${app.security.jwt-issuer}") String issuer, @Value("${app.security.jwt-audience}") String audience,
            @Value("${app.security.jwt-ttl}") Duration ttl) {
        return new JwtTokenService(key, issuer, audience, ttl, Clock.systemUTC());
    }
    @Bean JwtDecoder jwtDecoder(@Value("${app.security.jwt-secret-base64}") String key,
            @Value("${app.security.jwt-issuer}") String issuer, @Value("${app.security.jwt-audience}") String audience) {
        byte[] bytes = Base64.getDecoder().decode(key);
        if (bytes.length < 32 || issuer.isBlank() || audience.isBlank()) throw new IllegalArgumentException("Configuración JWT inválida");
        var decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(bytes, "HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience() != null && jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        OAuth2TokenValidator<Jwt> expirationRequired = jwt -> jwt.getExpiresAt() != null
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), audienceValidator, expirationRequired));
        return decoder;
    }
    @Bean @org.springframework.core.annotation.Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http, IdentityService identity, IdentityScopeExecutor scopes, Environment environment,
            ProblemAuthenticationEntryPoint authenticationEntryPoint, ProblemAccessDeniedHandler accessDeniedHandler) throws Exception {
        var converter = new CurrentAccountAuthenticationConverter(scopes, identity);
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
                .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable()).logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    if (environment.acceptsProfiles(Profiles.of("local-docs")))
                        auth.requestMatchers("/v3/api-docs/**", "/v3/api-docs", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/plataforma/**").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/usuarios-recepcion/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/pagos/*/anulacion").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/disciplinas/**", "/api/v1/membresias/**",
                                "/api/v1/canales-llegada/**", "/api/v1/localidades/**", "/api/v1/configuracion/**").hasAnyRole("ADMIN", "RECEPCION")
                        .requestMatchers("/api/v1/disciplinas/**", "/api/v1/membresias/**",
                                "/api/v1/canales-llegada/**", "/api/v1/localidades/**", "/api/v1/configuracion/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/clientes/**", "/api/v1/pedidos/**", "/api/v1/pagos/**").hasAnyRole("ADMIN", "RECEPCION")
                        .anyRequest().authenticated();
                })
                .oauth2ResourceServer(resource -> resource.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler).jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .addFilterAfter(new TenantRequestFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
