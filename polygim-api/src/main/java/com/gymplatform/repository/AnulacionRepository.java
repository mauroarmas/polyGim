package com.gymplatform.repository;

import com.gymplatform.model.AnulacionPago;
import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AnulacionRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public AnulacionRepository(TenantGuard guard) { this.guard = guard; }
    public Optional<AnulacionPago> findByPayment(UUID payment) {
        guard.requireBusiness();
        return entityManager.createQuery("select a from AnulacionPago a where a.pagoId = :payment", AnulacionPago.class)
                .setParameter("payment", payment).getResultStream().findFirst();
    }
    public void persist(AnulacionPago annulment) { guard.requireAdmin(); entityManager.persist(annulment); }
}
