package com.gymplatform.controller.dto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public final class CatalogoDtos {
    private CatalogoDtos() { }
    public record Item(UUID id, String nombre) { }
    public record Membership(UUID id, UUID disciplinaId, String nombre, String precio, int duracion, String unidadDuracion) { }
    public record Configuration(int cantidadMaximaCompra) { }
    public record NameWrite(@NotBlank @Size(max = 100) String nombre) { }
    public record ConfigurationWrite(@NotNull @Min(1) Integer cantidadMaximaCompra) { }
    public record MembershipCreate(@NotNull UUID disciplinaId, @NotBlank @Size(max = 100) String nombre,
                                   @NotNull @JsonDeserialize(using = MoneyCodec.StrictString.class) String precio,
                                   @NotNull @Min(1) Integer duracion,
                                   @NotBlank @Pattern(regexp = "DIAS|SEMANAS|MESES") String unidadDuracion) { }
    public record MembershipUpdate(@NotBlank @Size(max = 100) String nombre,
                                   @NotNull @JsonDeserialize(using = MoneyCodec.StrictString.class) String precio,
                                   @NotNull @Min(1) Integer duracion,
                                   @NotBlank @Pattern(regexp = "DIAS|SEMANAS|MESES") String unidadDuracion) { }
}
