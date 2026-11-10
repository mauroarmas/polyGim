package com.gymplatform.repository;

import com.gymplatform.model.CanalLlegada;
import com.gymplatform.model.CatalogoNombrado;
import com.gymplatform.model.Disciplina;
import com.gymplatform.model.Localidad;
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
public class CatalogoRepository {
    public enum Type {
        DISCIPLINA(Disciplina.class), CANAL(CanalLlegada.class), LOCALIDAD(Localidad.class);
        final Class<? extends CatalogoNombrado> entity;
        Type(Class<? extends CatalogoNombrado> entity) { this.entity = entity; }
        public CatalogoNombrado create(UUID id, String name) {
            return switch (this) {
                case DISCIPLINA -> new Disciplina(id, name);
                case CANAL -> new CanalLlegada(id, name);
                case LOCALIDAD -> new Localidad(id, name);
            };
        }
    }
    @PersistenceContext private EntityManager entityManager;
    private final TenantGuard guard;
    public CatalogoRepository(TenantGuard guard) { this.guard = guard; }
    public Optional<CatalogoNombrado> find(Type type, UUID id) {
        guard.requireBusiness();
        return Optional.ofNullable(entityManager.find(type.entity, id));
    }
    public void persist(CatalogoNombrado item) { guard.requireAdmin(); entityManager.persist(item); }
    public Page<CatalogoNombrado> list(Type type, PageRequest page) {
        guard.requireBusiness();
        String entity = entityManager.getMetamodel().entity(type.entity).getName();
        var result = entityManager.createQuery("select e from " + entity + " e order by lower(e.nombre), e.id", CatalogoNombrado.class)
                .setFirstResult(Math.toIntExact(page.getOffset())).setMaxResults(page.getPageSize()).getResultList();
        long count = entityManager.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
        return new PageImpl<>(result, page, count);
    }
}
