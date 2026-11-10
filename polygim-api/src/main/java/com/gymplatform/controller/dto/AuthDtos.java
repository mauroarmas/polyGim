package com.gymplatform.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() { }
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank @Size(max = 72) String password) {
        public LoginRequest { if (email != null) email = email.trim(); }
    }
    public record User(UUID id, String email, String rol, boolean habilitada, UUID gimnasioId) { }
    public record TokenResponse(String accessToken, String tokenType, int expiresIn, User usuario) { }
}
