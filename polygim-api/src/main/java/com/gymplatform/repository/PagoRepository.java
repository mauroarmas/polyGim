package com.gymplatform.repository;

import com.gymplatform.model.AnulacionPago;
import com.gymplatform.model.Pago;
import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class PagoRepository {
    public record View(Pago pago, AnulacionPago anulacion) { }
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public PagoRepository(TenantGuard guard) { this.guard = guard; }
    public void persist(Pago payment) { guard.requireBusiness(); entityManager.persist(payment); }
    public void persistAnnulment(AnulacionPago annulment) { guard.requireAdmin(); entityManager.persist(annulment); }
    public Optional<Pago> find(UUID id) { guard.requireBusiness(); return Optional.ofNullable(entityManager.find(Pago.class, id)); }
    public Optional<AnulacionPago> findAnnulment(UUID payment) {
        guard.requireBusiness();
        return entityManager.createQuery("select a from AnulacionPago a where a.pagoId = :payment", AnulacionPago.class)
                .setParameter("payment", payment).getResultStream().findFirst();
    }
    public BigDecimal sumValid(UUID orderId) {
        guard.requireBusiness();
        BigDecimal sum = entityManager.createQuery("select sum(p.montoAplicado) from Pago p where p.pedidoId = :id and not exists "
                + "(select a.id from AnulacionPago a where a.pagoId = p.id)", BigDecimal.class).setParameter("id", orderId).getSingleResult();
        return sum == null ? new BigDecimal("0.00") : sum;
    }
    public Map<UUID, BigDecimal> sumsValid(List<UUID> orderIds) {
        guard.requireBusiness();
        if (orderIds.isEmpty()) return Map.of();
        var rows = entityManager.createQuery("select p.pedidoId, sum(p.montoAplicado) from Pago p where p.pedidoId in :ids and not exists "
                + "(select a.id from AnulacionPago a where a.pagoId = p.id) group by p.pedidoId", Object[].class)
                .setParameter("ids", orderIds).getResultList();
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Object[] row : rows) result.put((UUID) row[0], (BigDecimal) row[1]);
        return result;
    }
    public Page<View> list(UUID orderId, PageRequest page) {
        guard.requireBusiness();
        var rows = entityManager.createQuery("select p, a from Pago p left join AnulacionPago a on a.pagoId = p.id "
                        + "where p.pedidoId = :orderId order by p.createdAt, p.id", Object[].class)
                .setParameter("orderId", orderId).setFirstResult(Math.toIntExact(page.getOffset())).setMaxResults(page.getPageSize()).getResultList();
        long count = entityManager.createQuery("select count(p) from Pago p where p.pedidoId = :orderId", Long.class)
                .setParameter("orderId", orderId).getSingleResult();
        return new PageImpl<>(rows.stream().map(row -> new View((Pago) row[0], (AnulacionPago) row[1])).toList(), page, count);
    }
}
