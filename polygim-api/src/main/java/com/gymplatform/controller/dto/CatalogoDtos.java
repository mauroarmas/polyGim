package com.gymplatform.controller.dto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
public final class CatalogoDtos {
    private CatalogoDtos() { }
    @Schema(name = "CatalogItem", requiredProperties = {"id", "nombre"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE) public record Item(UUID id, String nombre) { }
    @Schema(requiredProperties = {"id", "disciplinaId", "nombre", "precio", "duracion", "unidadDuracion"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Membership(UUID id, UUID disciplinaId, String nombre,
                             @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String precio,
                             int duracion, @Schema(allowableValues = {"DIAS", "SEMANAS", "MESES"}) String unidadDuracion) { }
    @Schema(requiredProperties = {"cantidadMaximaCompra"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Configuration(int cantidadMaximaCompra) { }
    @Schema(name = "CatalogWrite", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record NameWrite(@NotBlank @Size(max = 100) String nombre) { }
    @Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record ConfigurationWrite(@NotNull @Min(1) Integer cantidadMaximaCompra) { }
    @Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record MembershipCreate(@NotNull UUID disciplinaId, @NotBlank @Size(max = 100) String nombre,
                                   @NotNull @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") @JsonDeserialize(using = MoneyCodec.StrictString.class) String precio,
                                   @NotNull @Min(1) Integer duracion,
                                   @NotBlank @Pattern(regexp = "DIAS|SEMANAS|MESES") String unidadDuracion) { }
    @Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record MembershipUpdate(@NotBlank @Size(max = 100) String nombre,
                                   @NotNull @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") @JsonDeserialize(using = MoneyCodec.StrictString.class) String precio,
                                   @NotNull @Min(1) Integer duracion,
                                   @NotBlank @Pattern(regexp = "DIAS|SEMANAS|MESES") String unidadDuracion) { }
}
