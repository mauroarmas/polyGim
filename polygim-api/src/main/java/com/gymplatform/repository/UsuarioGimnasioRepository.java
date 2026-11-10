package com.gymplatform.repository;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.model.UsuarioGimnasio;
import com.gymplatform.tenant.TenantContext;
import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioGimnasioRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public UsuarioGimnasioRepository(TenantGuard guard) { this.guard = guard; }

    public CuentaAcceso createReception(UUID id, String email, String hash) {
        guard.requireAdmin();
        var account = new CuentaAcceso(id, email, hash, "RECEPCION", TenantContext.require().tenantId());
        entityManager.persist(account);
        entityManager.persist(new UsuarioGimnasio(id));
        return account;
    }

    public Optional<CuentaAcceso> disableReception(UUID id) {
        guard.requireAdmin();
        if (entityManager.find(UsuarioGimnasio.class, id) == null) return Optional.empty();
        var account = entityManager.find(CuentaAcceso.class, id);
        if (account == null || !"RECEPCION".equals(account.getRol())
                || !TenantContext.require().tenantId().equals(account.getGimnasioId())) return Optional.empty();
        account.deshabilitar();
        return Optional.of(account);
    }
}
