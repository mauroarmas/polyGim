package com.gymplatform.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class GimnasioDtos {
    private GimnasioDtos() { }
    public record NewAccount(@NotBlank @Email @Size(max = 254) String email,
                             @NotBlank @Size(min = 12, max = 72) String password) {
        public NewAccount { if (email != null) email = email.trim(); }
    }
    public record CreateRequest(@NotBlank @Size(max = 100) String nombre,
                                @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://.+") String logoUrl,
                                @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorPrimario,
                                @NotNull @Valid NewAccount administradorInicial) { }
    public record Gym(UUID id, String nombre, String logoUrl, String colorPrimario) { }
    public record Created(Gym gimnasio, AuthDtos.User administradorInicial) { }
}
