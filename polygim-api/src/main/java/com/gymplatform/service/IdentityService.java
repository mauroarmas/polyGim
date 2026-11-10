package com.gymplatform.service;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.identity.IdentityRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {
    private final IdentityRepository repository;
    private final PasswordEncoder encoder;
    public IdentityService(IdentityRepository repository, PasswordEncoder encoder) {
        this.repository = repository; this.encoder = encoder;
    }
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email requerido");
        return email.trim().toLowerCase(Locale.ROOT);
    }
    public static void validateNewPassword(String password) {
        if (password == null) throw new IllegalArgumentException("Contraseña inválida");
        int characters = password.codePointCount(0, password.length());
        if (characters < 12 || characters > 72 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Contraseña inválida");
    }
    public CuentaAcceso authenticate(String email, String password) {
        var account = repository.findByEmail(normalizeEmail(email)).orElseThrow(IdentityService::invalidCredentials);
        if (!account.isHabilitada() || password == null || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(password, account.getPasswordHash()))
            throw invalidCredentials();
        return account;
    }
    public CuentaAcceso requireEnabled(UUID id) {
        var account = repository.findById(id).orElseThrow(IdentityService::invalidCredentials);
        if (!account.isHabilitada()) throw invalidCredentials();
        return account;
    }
    private static BadCredentialsException invalidCredentials() { return new BadCredentialsException("Credenciales inválidas"); }
}
