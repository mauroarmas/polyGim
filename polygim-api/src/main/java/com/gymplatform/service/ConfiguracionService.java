package com.gymplatform.service;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.model.ConfiguracionGimnasio;
import com.gymplatform.repository.TenantRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionService {
    private final TenantRepository repository;
    private final TenantBusinessExecutor executor;
    public ConfiguracionService(TenantRepository repository, TenantBusinessExecutor executor) {
        this.repository = repository; this.executor = executor;
    }
    public static int validateMaximum(int maximum) {
        if (maximum < 1) throw new IllegalArgumentException("Cantidad máxima inválida");
        return maximum;
    }
    public CatalogoDtos.Configuration get() {
        return executor.business(() -> new CatalogoDtos.Configuration(current().getCantidadMaximaCompra()));
    }
    public CatalogoDtos.Configuration update(int maximum) {
        validateMaximum(maximum);
        return executor.admin(() -> { var config = current(); config.setCantidadMaximaCompra(maximum); return new CatalogoDtos.Configuration(maximum); });
    }
    private ConfiguracionGimnasio current() {
        return repository.list(ConfiguracionGimnasio.class, 0, 1).stream().findFirst().orElseThrow(NoSuchElementException::new);
    }
}
