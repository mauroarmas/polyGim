package com.gymplatform.service;

import com.gymplatform.controller.dto.AuthDtos;
import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.UsuarioGimnasioRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ReceptionService {
    private final UsuarioGimnasioRepository repository;
    private final TenantBusinessExecutor executor;
    private final PasswordEncoder encoder;
    public ReceptionService(UsuarioGimnasioRepository repository, TenantBusinessExecutor executor, PasswordEncoder encoder) {
        this.repository = repository; this.executor = executor; this.encoder = encoder;
    }
    public AuthDtos.User create(String email, String password) {
        IdentityService.validateNewPassword(password);
        String normalized = IdentityService.normalizeEmail(email);
        String hash = encoder.encode(password);
        return executor.admin(() -> dto(repository.createReception(UUID.randomUUID(), normalized, hash)));
    }
    public AuthDtos.User disable(UUID id) {
        return executor.admin(() -> dto(repository.disableReception(id).orElseThrow(NoSuchElementException::new)));
    }
    private static AuthDtos.User dto(CuentaAcceso account) {
        return new AuthDtos.User(account.getId(), account.getEmail(), account.getRol(), account.isHabilitada(), account.getGimnasioId());
    }
}
