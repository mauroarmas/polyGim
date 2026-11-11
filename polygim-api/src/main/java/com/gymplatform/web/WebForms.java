package com.gymplatform.web;

import com.gymplatform.controller.dto.ClienteDtos;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public final class WebForms {
    private WebForms() { }
    public record Client(@NotBlank @Pattern(regexp="[0-9]{7,8}") String dni,
                         @NotBlank @Size(max=100) String nombre, @NotBlank @Size(max=100) String apellido,
                         @Size(max=40) String telefono, UUID canalLlegadaId,
                         @Size(max=160) String calle, @Size(max=20) String altura, UUID localidadId) {
        ClienteDtos.Write request() { return new ClienteDtos.Write(dni,nombre,apellido,telefono,canalLlegadaId,new ClienteDtos.Address(calle,altura),localidadId); }
    }
    public record Purchase(@NotNull UUID membresiaId, @NotNull @Min(1) Integer cantidad,
                           @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCompra, UUID pedidoAnteriorId) { }
    public record Membership(UUID disciplinaId, @NotBlank @Size(max=100) String nombre,
                             @NotBlank String precio, @NotNull @Min(1) Integer duracion,
                             @NotBlank @Pattern(regexp="DIAS|SEMANAS|MESES") String unidadDuracion) { }
    public record Name(@NotBlank @Size(max=100) String nombre) { }
    public record Payment(@NotBlank String montoRecibido) { }
    public record Reception(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=12,max=72) String password) { }
    public record Configuration(@NotNull @Min(1) Integer cantidadMaximaCompra) { }
    public record Gym(@NotBlank @Size(max=100) String nombre, @NotBlank @Size(max=2048) String logoUrl,
                      @NotBlank @Pattern(regexp="#[0-9a-fA-F]{6}") String colorPrimario,
                      @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=12,max=72) String password) { }
}
