package com.gymplatform.security;

import com.gymplatform.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantRequestFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentAccountPrincipal principal) || principal.gimnasioId() == null) {
            chain.doFilter(request, response);
            return;
        }
        try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, principal.gimnasioId())) {
            chain.doFilter(request, response);
        }
    }
}
