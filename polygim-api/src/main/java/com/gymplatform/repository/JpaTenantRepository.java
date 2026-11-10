package com.gymplatform.repository;

import com.gymplatform.tenant.TenantGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.TenantId;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTenantRepository implements TenantRepository {
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public JpaTenantRepository(TenantGuard guard) { this.guard = guard; }
    private void verify(Class<?> type) {
        guard.requireBusiness();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (Arrays.stream(current.getDeclaredFields()).anyMatch(field -> field.isAnnotationPresent(TenantId.class))) return;
        }
        throw new IllegalArgumentException("Tipo no perteneciente a un gimnasio");
    }
    @Override public <T> void persist(T entity) { verify(entity.getClass()); entityManager.persist(entity); }
    @Override public <T> Optional<T> find(Class<T> type, UUID id) { verify(type); return Optional.ofNullable(entityManager.find(type, id)); }
    @Override public <T> List<T> list(Class<T> type, int offset, int limit) {
        verify(type);
        String name = entityManager.getMetamodel().entity(type).getName();
        return entityManager.createQuery("select e from " + name + " e order by e.id", type)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
    }
    @Override public <T> long count(Class<T> type) {
        verify(type);
        String name = entityManager.getMetamodel().entity(type).getName();
        return entityManager.createQuery("select count(e) from " + name + " e", Long.class).getSingleResult();
    }
}
