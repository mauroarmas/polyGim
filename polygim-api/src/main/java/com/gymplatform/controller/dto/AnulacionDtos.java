package com.gymplatform.controller.dto;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
public final class AnulacionDtos {
    private AnulacionDtos() { }
    @Schema(name = "Annulment", requiredProperties = {"id", "pagoId", "anuladoPorId", "fechaAnulacion"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Evidence(UUID id, UUID pagoId, UUID anuladoPorId, Instant fechaAnulacion) { }
}
