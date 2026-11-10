package com.gymplatform.tenant;

import com.gymplatform.model.ConfiguracionGimnasio;
import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.model.Gimnasio;
import com.gymplatform.model.UsuarioGimnasio;
import com.gymplatform.security.CurrentAccountPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class GymProvisioningExecutor {
    @PersistenceContext private EntityManager entityManager;
    private final TransactionTemplate transaction;
    public GymProvisioningExecutor(PlatformTransactionManager manager) { this.transaction = new TransactionTemplate(manager); }

    public void provision(UUID gym, String nombre, String logo, String color, UUID adminId,
                          String email, String hash, int cantidadMaxima) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CurrentAccountPrincipal principal)
                || principal.gimnasioId() != null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")))
            throw new AccessDeniedException("Acceso denegado");
        try (var ignored = TenantContext.open(TenantContext.Scope.PROVISIONING, gym)) {
            transaction.executeWithoutResult(status -> {
                if (entityManager.find(Gimnasio.class, gym) != null) throw new IllegalStateException("Gimnasio existente");
                entityManager.persist(new Gimnasio(gym, nombre, logo, color));
                entityManager.persist(new CuentaAcceso(adminId, email, hash, "ADMIN", gym));
                entityManager.persist(new UsuarioGimnasio(adminId));
                entityManager.persist(new ConfiguracionGimnasio(UUID.randomUUID(), cantidadMaxima));
            });
        }
    }
}
