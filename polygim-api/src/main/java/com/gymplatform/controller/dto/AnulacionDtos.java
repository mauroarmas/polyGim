package com.gymplatform.controller.dto;
import java.time.Instant;
import java.util.UUID;
public final class AnulacionDtos {
    private AnulacionDtos() { }
    public record Evidence(UUID id, UUID pagoId, UUID anuladoPorId, Instant fechaAnulacion) { }
}
