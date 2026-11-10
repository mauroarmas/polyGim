package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "anulacion_pago")
public class AnulacionPago {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(name = "pago_id", nullable = false, updatable = false) private UUID pagoId;
    @Column(name = "anulado_por_id", nullable = false, updatable = false) private UUID anuladoPorId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected AnulacionPago() { }
    public AnulacionPago(UUID id, UUID pagoId, UUID anuladoPorId, Instant createdAt) {
        this.id = id; this.pagoId = pagoId; this.anuladoPorId = anuladoPorId; this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public UUID getPagoId() { return pagoId; }
    public UUID getAnuladoPorId() { return anuladoPorId; }
    public Instant getCreatedAt() { return createdAt; }
}
