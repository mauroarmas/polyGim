package com.gymplatform.controller;

import com.gymplatform.controller.dto.AuthDtos;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.security.JwtTokenService;
import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IdentityScopeExecutor scopes;
    private final IdentityService identity;
    private final JwtTokenService tokens;
    private final int expiresIn;
    public AuthController(IdentityScopeExecutor scopes, IdentityService identity, JwtTokenService tokens,
                          @Value("${app.security.jwt-ttl}") Duration ttl) {
        this.scopes = scopes; this.identity = identity; this.tokens = tokens; this.expiresIn = Math.toIntExact(ttl.toSeconds());
    }
    @PostMapping("/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return scopes.execute(() -> {
            var account = identity.authenticate(request.email(), request.password());
            var user = new AuthDtos.User(account.getId(), account.getEmail(), account.getRol(), account.isHabilitada(), account.getGimnasioId());
            return new AuthDtos.TokenResponse(tokens.issue(account.getId()), "Bearer", expiresIn, user);
        });
    }
    @GetMapping("/me")
    public AuthDtos.User currentUser(Authentication authentication) {
        var principal = (CurrentAccountPrincipal) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority().substring("ROLE_".length());
        return new AuthDtos.User(principal.accountId(), principal.email(), role, true, principal.gimnasioId());
    }
}
