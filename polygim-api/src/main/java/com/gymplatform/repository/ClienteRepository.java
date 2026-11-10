package com.gymplatform.repository;

import com.gymplatform.model.Cliente;
import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public ClienteRepository(TenantGuard guard) { this.guard = guard; }
    public void persist(Cliente client) { guard.requireBusiness(); entityManager.persist(client); }
    public Optional<Cliente> find(UUID id) { guard.requireBusiness(); return Optional.ofNullable(entityManager.find(Cliente.class, id)); }
    public Page<Cliente> list(String dni, PageRequest page) {
        guard.requireBusiness();
        String where = dni == null ? "" : " where c.dni = :dni";
        var items = entityManager.createQuery("select c from Cliente c" + where + " order by c.apellido, c.id", Cliente.class);
        var count = entityManager.createQuery("select count(c) from Cliente c" + where, Long.class);
        if (dni != null) { items.setParameter("dni", dni); count.setParameter("dni", dni); }
        return new PageImpl<>(items.setFirstResult(Math.toIntExact(page.getOffset())).setMaxResults(page.getPageSize()).getResultList(), page, count.getSingleResult());
    }
}
