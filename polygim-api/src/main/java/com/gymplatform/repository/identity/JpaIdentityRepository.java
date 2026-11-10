package com.gymplatform.repository.identity;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaIdentityRepository implements IdentityRepository {
    @PersistenceContext private EntityManager entityManager;
    private void requireIdentityScope() {
        var scope = TenantContext.require().scope();
        if (scope != TenantContext.Scope.AUTH && scope != TenantContext.Scope.PROVISIONING)
            throw new IllegalStateException("Se requiere un scope interno de identidad");
    }
    @Override public Optional<CuentaAcceso> findByEmail(String email) {
        requireIdentityScope();
        return entityManager.createQuery("select a from CuentaAcceso a where a.email = :email", CuentaAcceso.class)
                .setParameter("email", email).getResultStream().findFirst();
    }
    @Override public Optional<CuentaAcceso> findById(UUID id) {
        requireIdentityScope();
        return Optional.ofNullable(entityManager.find(CuentaAcceso.class, id));
    }
    @Override public void persist(CuentaAcceso account) { requireIdentityScope(); entityManager.persist(account); }
    @Override public boolean superadminExists() {
        requireIdentityScope();
        return entityManager.createQuery("select count(a) from CuentaAcceso a where a.rol = :role", Long.class)
                .setParameter("role", "SUPERADMIN").getSingleResult() > 0;
    }
    @Override public Optional<CuentaAcceso> findFirstEnabledSuperadmin() {
        requireIdentityScope();
        return entityManager.createQuery("select a from CuentaAcceso a where a.rol = :role and a.habilitada = true order by a.createdAt, a.id", CuentaAcceso.class)
                .setParameter("role", "SUPERADMIN").setMaxResults(1).getResultStream().findFirst();
    }
}
