package com.gymplatform.tenant;

import com.gymplatform.security.CurrentAccountPrincipal;
import java.util.function.Supplier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PlatformScopeExecutor {
    private final TransactionTemplate transaction;
    public PlatformScopeExecutor(PlatformTransactionManager manager) { transaction = new TransactionTemplate(manager); }
    public <T> T read(Supplier<T> action) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CurrentAccountPrincipal principal)
                || principal.gimnasioId() != null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")))
            throw new AccessDeniedException("Acceso denegado");
        try (var ignored = TenantContext.open(TenantContext.Scope.PLATFORM, TenantContext.SENTINEL)) {
            return transaction.execute(status -> action.get());
        }
    }
}
