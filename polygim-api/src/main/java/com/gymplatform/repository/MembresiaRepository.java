package com.gymplatform.repository;

import com.gymplatform.model.Membresia;
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
public class MembresiaRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public MembresiaRepository(TenantGuard guard) { this.guard = guard; }
    public void persist(Membresia membership) { guard.requireAdmin(); entityManager.persist(membership); }
    public Optional<Membresia> find(UUID id) { guard.requireBusiness(); return Optional.ofNullable(entityManager.find(Membresia.class, id)); }
    public Page<Membresia> list(UUID disciplina, PageRequest page) {
        guard.requireBusiness();
        String where = disciplina == null ? "" : " where m.disciplinaId = :disciplina";
        var items = entityManager.createQuery("select m from Membresia m" + where + " order by lower(m.nombre), m.id", Membresia.class);
        var count = entityManager.createQuery("select count(m) from Membresia m" + where, Long.class);
        if (disciplina != null) { items.setParameter("disciplina", disciplina); count.setParameter("disciplina", disciplina); }
        return new PageImpl<>(items.setFirstResult(Math.toIntExact(page.getOffset())).setMaxResults(page.getPageSize()).getResultList(), page, count.getSingleResult());
    }
}
