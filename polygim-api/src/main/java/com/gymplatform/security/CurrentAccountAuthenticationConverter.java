package com.gymplatform.security;

import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.List;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

public class CurrentAccountAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final IdentityScopeExecutor scopes;
    private final IdentityService identity;
    public CurrentAccountAuthenticationConverter(IdentityScopeExecutor scopes, IdentityService identity) {
        this.scopes = scopes; this.identity = identity;
    }
    @Override public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            UUID id = UUID.fromString(jwt.getSubject());
            return scopes.execute(() -> {
                var account = identity.requireEnabled(id);
                var principal = new CurrentAccountPrincipal(account.getId(), account.getGimnasioId(), account.getEmail());
                return new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + account.getRol())));
            });
        } catch (IllegalArgumentException | org.springframework.security.core.AuthenticationException ex) {
            throw new OAuth2AuthenticationException("invalid_token");
        }
    }
}
