package com.gymplatform.service;

import com.gymplatform.controller.dto.AuthDtos;
import com.gymplatform.controller.dto.GimnasioDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.model.Gimnasio;
import com.gymplatform.repository.GimnasioRepository;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class GimnasioService {
    private final GymProvisioningExecutor provisioning;
    private final GimnasioRepository repository;
    private final PasswordEncoder encoder;
    private final int max;
    public GimnasioService(GymProvisioningExecutor provisioning, GimnasioRepository repository, PasswordEncoder encoder,
                           @Value("${app.default-gym-max-quantity}") int max) {
        if (max < 1) throw new IllegalArgumentException("Máximo inicial inválido");
        this.provisioning = provisioning; this.repository = repository; this.encoder = encoder; this.max = max;
    }
    public GimnasioDtos.Created create(GimnasioDtos.CreateRequest request) {
        String name = request.nombre().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Nombre inválido");
        URI logo = URI.create(request.logoUrl());
        if (logo.getHost() == null || !("http".equals(logo.getScheme()) || "https".equals(logo.getScheme())))
            throw new IllegalArgumentException("Logo inválido");
        var admin = request.administradorInicial();
        IdentityService.validateNewPassword(admin.password());
        String email = IdentityService.normalizeEmail(admin.email());
        UUID gymId = UUID.randomUUID(), adminId = UUID.randomUUID();
        provisioning.provision(gymId, name, request.logoUrl(), request.colorPrimario(), adminId, email, encoder.encode(admin.password()), max);
        return new GimnasioDtos.Created(new GimnasioDtos.Gym(gymId, name, request.logoUrl(), request.colorPrimario()),
                new AuthDtos.User(adminId, email, "ADMIN", true, gymId));
    }
    public GimnasioDtos.Gym find(UUID id) {
        return repository.find(id).map(GimnasioService::dto).orElseThrow(() -> new java.util.NoSuchElementException("Recurso no encontrado"));
    }
    public PageResponse<GimnasioDtos.Gym> list(Integer page, Integer size) {
        return PageResponse.of(repository.list(PageResponse.request(page, size)).map(GimnasioService::dto));
    }
    private static GimnasioDtos.Gym dto(Gimnasio gym) {
        return new GimnasioDtos.Gym(gym.getId(), gym.getNombre(), gym.getLogoUrl(), gym.getColorPrimario());
    }
}
