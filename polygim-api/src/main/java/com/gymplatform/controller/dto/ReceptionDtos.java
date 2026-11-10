package com.gymplatform.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public final class ReceptionDtos {
    private ReceptionDtos() { }
    @Schema(name = "NewAccountRequest", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record CreateRequest(@NotBlank @Email @Size(max = 254) String email,
                                @NotBlank @Size(min = 12, max = 72) @Schema(accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
        public CreateRequest { if (email != null) email = email.trim(); }
    }
}
