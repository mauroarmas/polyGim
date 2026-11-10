package com.gymplatform.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public final class AuthDtos {
    private AuthDtos() { }
    @Schema(name = "LoginRequest", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank @Size(max = 72) @Schema(accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
        public LoginRequest { if (email != null) email = email.trim(); }
    }
    @Schema(requiredProperties = {"id", "email", "rol", "habilitada", "gimnasioId"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record User(UUID id, String email, @Schema(allowableValues = {"SUPERADMIN", "ADMIN", "RECEPCION"}) String rol,
                       boolean habilitada, @Schema(nullable = true) UUID gimnasioId) { }
    @Schema(requiredProperties = {"accessToken", "tokenType", "expiresIn", "usuario"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record TokenResponse(String accessToken, @Schema(allowableValues = {"Bearer"}) String tokenType, int expiresIn, User usuario) { }
}
