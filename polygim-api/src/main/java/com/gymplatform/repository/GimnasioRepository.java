package com.gymplatform.repository;

import com.gymplatform.model.Gimnasio;
import com.gymplatform.tenant.PlatformScopeExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class GimnasioRepository {
    @PersistenceContext private EntityManager entityManager;
    private final PlatformScopeExecutor platform;
    public GimnasioRepository(PlatformScopeExecutor platform) { this.platform = platform; }
    public Optional<Gimnasio> find(UUID id) {
        return platform.read(() -> Optional.ofNullable(entityManager.find(Gimnasio.class, id)));
    }
    public Page<Gimnasio> list(PageRequest request) {
        return platform.read(() -> {
            var content = entityManager.createQuery("select g from Gimnasio g order by g.nombre, g.id", Gimnasio.class)
                    .setFirstResult(Math.toIntExact(request.getOffset())).setMaxResults(request.getPageSize()).getResultList();
            long count = entityManager.createQuery("select count(g) from Gimnasio g", Long.class).getSingleResult();
            return new PageImpl<>(content, request, count);
        });
    }
}
