package com.gymplatform.tenant;

import com.gymplatform.security.CurrentAccountPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantGuard {
    public void requireBusiness() {
        var current = TenantContext.require();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (current.scope() != TenantContext.Scope.TENANT || auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CurrentAccountPrincipal principal)
                || !current.tenantId().equals(principal.gimnasioId())
                || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPCION"))) {
            throw new AccessDeniedException("Acceso denegado");
        }
    }
    public void requireAdmin() {
        requireBusiness();
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")))
            throw new AccessDeniedException("Acceso denegado");
    }
}
