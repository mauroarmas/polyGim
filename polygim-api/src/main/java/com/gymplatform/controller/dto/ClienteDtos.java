package com.gymplatform.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public final class ClienteDtos {
    private ClienteDtos() { }
    @Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Address(@Size(max = 160) String calle, @Size(max = 20) String altura) { }
    @Schema(name = "ClientWrite", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Write(@NotBlank @Pattern(regexp = "[0-9]{7,8}") @JsonDeserialize(using = MoneyCodec.StrictString.class) String dni,
                        @NotBlank @Size(max = 100) String nombre, @NotBlank @Size(max = 100) String apellido,
                        @Size(max = 40) String telefono, UUID canalLlegadaId, @Valid Address direccion, UUID localidadId) { }
    @Schema(requiredProperties = {"id", "dni", "nombre", "apellido", "fechaIngreso"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Client(UUID id, String dni, String nombre, String apellido, String telefono, UUID canalLlegadaId,
                         Address direccion, UUID localidadId, LocalDate fechaIngreso) { }
}
