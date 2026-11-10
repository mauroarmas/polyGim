package com.gymplatform.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ReceptionDtos {
    private ReceptionDtos() { }
    public record CreateRequest(@NotBlank @Email @Size(max = 254) String email,
                                @NotBlank @Size(min = 12, max = 72) String password) {
        public CreateRequest { if (email != null) email = email.trim(); }
    }
}
