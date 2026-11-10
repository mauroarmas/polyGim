package com.gymplatform.service;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.model.Membresia;
import com.gymplatform.repository.CatalogoRepository;
import com.gymplatform.repository.MembresiaRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MembresiaService {
    private final MembresiaRepository repository;
    private final CatalogoRepository catalogs;
    private final TenantBusinessExecutor executor;
    public MembresiaService(MembresiaRepository repository, CatalogoRepository catalogs, TenantBusinessExecutor executor) {
        this.repository = repository; this.catalogs = catalogs; this.executor = executor;
    }
    public static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.scale() != 2 || price.precision() > 19 || price.signum() <= 0)
            throw new IllegalArgumentException("Precio inválido");
        return price;
    }
    public static void validateDuration(int duration, String unit) {
        if (duration < 1 || unit == null || !Set.of("DIAS", "SEMANAS", "MESES").contains(unit))
            throw new IllegalArgumentException("Duración inválida");
    }
    public CatalogoDtos.Membership create(UUID discipline, String name, BigDecimal price, int duration, String unit) {
        String normalized = CatalogoService.normalizeName(name); validatePrice(price); validateDuration(duration, unit);
        return executor.admin(() -> {
            catalogs.find(CatalogoRepository.Type.DISCIPLINA, discipline).orElseThrow(NoSuchElementException::new);
            var membership = new Membresia(UUID.randomUUID(), discipline, normalized, price, duration, unit);
            repository.persist(membership); return dto(membership);
        });
    }
    public CatalogoDtos.Membership update(UUID id, String name, BigDecimal price, int duration, String unit) {
        String normalized = CatalogoService.normalizeName(name); validatePrice(price); validateDuration(duration, unit);
        return executor.admin(() -> {
            var membership = repository.find(id).orElseThrow(NoSuchElementException::new);
            membership.update(normalized, price, duration, unit); return dto(membership);
        });
    }
    public CatalogoDtos.Membership get(UUID id) {
        return executor.business(() -> dto(repository.find(id).orElseThrow(NoSuchElementException::new)));
    }
    public PageResponse<CatalogoDtos.Membership> list(UUID discipline, Integer page, Integer size) {
        var request = PageResponse.request(page, size);
        return executor.business(() -> {
            if (discipline != null) catalogs.find(CatalogoRepository.Type.DISCIPLINA, discipline).orElseThrow(NoSuchElementException::new);
            return PageResponse.of(repository.list(discipline, request).map(MembresiaService::dto));
        });
    }
    private static CatalogoDtos.Membership dto(Membresia item) {
        return new CatalogoDtos.Membership(item.getId(), item.getDisciplinaId(), item.getNombre(),
                item.getPrecio().toPlainString(), item.getDuracion(), item.getUnidadDuracion());
    }
}
