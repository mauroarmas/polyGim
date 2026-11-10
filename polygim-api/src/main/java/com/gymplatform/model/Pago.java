package com.gymplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "pago")
public class Pago {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(name = "pedido_id", nullable = false, updatable = false) private UUID pedidoId;
    @Column(name = "monto_aplicado", nullable = false, precision = 19, scale = 2, updatable = false) private BigDecimal montoAplicado;
    @Column(name = "registrado_por_id", nullable = false, updatable = false) private UUID registradoPorId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected Pago() { }
    public Pago(UUID id, UUID pedidoId, BigDecimal montoAplicado, UUID registradoPorId, Instant createdAt) {
        this.id = id; this.pedidoId = pedidoId; this.montoAplicado = montoAplicado; this.registradoPorId = registradoPorId; this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public BigDecimal getMontoAplicado() { return montoAplicado; }
    public UUID getRegistradoPorId() { return registradoPorId; }
    public Instant getCreatedAt() { return createdAt; }
}
