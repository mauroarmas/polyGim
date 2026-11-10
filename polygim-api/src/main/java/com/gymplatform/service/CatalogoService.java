package com.gymplatform.service;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.model.CatalogoNombrado;
import com.gymplatform.repository.CatalogoRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CatalogoService {
    private final CatalogoRepository repository;
    private final TenantBusinessExecutor executor;
    public CatalogoService(CatalogoRepository repository, TenantBusinessExecutor executor) {
        this.repository = repository; this.executor = executor;
    }
    public static String normalizeName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 100) throw new IllegalArgumentException("Nombre inválido");
        return name.trim();
    }
    public CatalogoDtos.Item create(CatalogoRepository.Type type, String name) {
        String normalized = normalizeName(name);
        return executor.admin(() -> {
            var item = type.create(UUID.randomUUID(), normalized); repository.persist(item); return dto(item);
        });
    }
    public CatalogoDtos.Item update(CatalogoRepository.Type type, UUID id, String name) {
        String normalized = normalizeName(name);
        return executor.admin(() -> {
            var item = repository.find(type, id).orElseThrow(NoSuchElementException::new); item.rename(normalized); return dto(item);
        });
    }
    public CatalogoDtos.Item get(CatalogoRepository.Type type, UUID id) {
        return executor.business(() -> dto(repository.find(type, id).orElseThrow(NoSuchElementException::new)));
    }
    public PageResponse<CatalogoDtos.Item> list(CatalogoRepository.Type type, Integer page, Integer size) {
        var request = PageResponse.request(page, size);
        return executor.business(() -> PageResponse.of(repository.list(type, request).map(CatalogoService::dto)));
    }
    private static CatalogoDtos.Item dto(CatalogoNombrado item) { return new CatalogoDtos.Item(item.getId(), item.getNombre()); }
}
