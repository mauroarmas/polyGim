package com.gymplatform.config;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.identity.IdentityRepository;
import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SuperadminBootstrap implements ApplicationRunner {
    private final IdentityScopeExecutor scopes;
    private final IdentityRepository accounts;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    public SuperadminBootstrap(IdentityScopeExecutor scopes, IdentityRepository accounts, PasswordEncoder encoder,
                               @Value("${app.bootstrap.superadmin-email:}") String email,
                               @Value("${app.bootstrap.superadmin-password:}") String password) {
        this.scopes = scopes; this.accounts = accounts; this.encoder = encoder;
        this.email = email; this.password = password;
    }
    @Override public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) throw new IllegalStateException("Configuración de bootstrap incompleta");
        String normalized = IdentityService.normalizeEmail(email);
        IdentityService.validateNewPassword(password);
        scopes.execute(() -> {
            if (!accounts.superadminExists())
                accounts.persist(new CuentaAcceso(UUID.randomUUID(), normalized, encoder.encode(password), "SUPERADMIN", null));
            return null;
        });
    }
}
