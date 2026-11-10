package com.gymplatform.repository;

import com.gymplatform.model.Pedido;
import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public PedidoRepository(TenantGuard guard) { this.guard = guard; }
    public void persist(Pedido order) { guard.requireBusiness(); entityManager.persist(order); }
    public Optional<Pedido> find(UUID id) { guard.requireBusiness(); return Optional.ofNullable(entityManager.find(Pedido.class, id)); }
    public Optional<Pedido> lock(UUID id) { guard.requireBusiness(); return Optional.ofNullable(entityManager.find(Pedido.class, id, LockModeType.PESSIMISTIC_WRITE)); }
    public boolean hasSuccessor(UUID id) {
        guard.requireBusiness();
        return entityManager.createQuery("select count(p) from Pedido p where p.pedidoAnteriorId = :id", Long.class)
                .setParameter("id", id).getSingleResult() > 0;
    }
    public Page<Pedido> list(UUID clientId, PageRequest page) {
        guard.requireBusiness();
        String where = clientId == null ? "" : " where p.clienteId = :client";
        var items = entityManager.createQuery("select p from Pedido p" + where + " order by p.createdAt desc, p.id desc", Pedido.class);
        var count = entityManager.createQuery("select count(p) from Pedido p" + where, Long.class);
        if (clientId != null) { items.setParameter("client", clientId); count.setParameter("client", clientId); }
        return new PageImpl<>(items.setFirstResult(Math.toIntExact(page.getOffset())).setMaxResults(page.getPageSize()).getResultList(), page, count.getSingleResult());
    }
}
