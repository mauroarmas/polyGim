package com.gymplatform.security;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class WebAuthenticationProvider implements AuthenticationProvider {
    private final IdentityScopeExecutor scopes;
    private final IdentityService identity;
    public WebAuthenticationProvider(IdentityScopeExecutor scopes, IdentityService identity) {
        this.scopes = scopes; this.identity = identity;
    }
    @Override public Authentication authenticate(Authentication authentication) {
        try {
            return scopes.execute(() -> current(identity.authenticate(authentication.getName(),
                    authentication.getCredentials() == null ? null : authentication.getCredentials().toString())));
        } catch (IllegalArgumentException ex) { throw new BadCredentialsException("Credenciales inválidas"); }
    }
    static Authentication current(CuentaAcceso account) {
        return new UsernamePasswordAuthenticationToken(new CurrentAccountPrincipal(account.getId(), account.getGimnasioId(), account.getEmail()),
                null, List.of(new SimpleGrantedAuthority("ROLE_" + account.getRol())));
    }
    @Override public boolean supports(Class<?> type) { return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type); }
}
