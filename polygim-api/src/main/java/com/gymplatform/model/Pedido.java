package com.gymplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(name = "cliente_id", nullable = false, updatable = false) private UUID clienteId;
    @Embedded private DetallePedido detalle;
    @Column(nullable = false, precision = 19, scale = 2, updatable = false) private BigDecimal total;
    @Column(name = "fecha_compra", nullable = false, updatable = false) private LocalDate fechaCompra;
    @Column(name = "fecha_base_vencimiento", nullable = false, updatable = false) private LocalDate fechaBaseVencimiento;
    @Column(name = "fecha_vencimiento", nullable = false, updatable = false) private LocalDate fechaVencimiento;
    @Column(name = "pedido_anterior_id", updatable = false) private UUID pedidoAnteriorId;
    @Column(name = "registrado_por_id", nullable = false, updatable = false) private UUID registradoPorId;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected Pedido() { }
    public Pedido(UUID id, UUID clienteId, DetallePedido detalle, BigDecimal total, LocalDate fechaCompra,
                  LocalDate fechaBaseVencimiento, LocalDate fechaVencimiento, UUID pedidoAnteriorId, UUID registradoPorId) {
        this.id = id; this.clienteId = clienteId; this.detalle = detalle; this.total = total; this.fechaCompra = fechaCompra;
        this.fechaBaseVencimiento = fechaBaseVencimiento; this.fechaVencimiento = fechaVencimiento;
        this.pedidoAnteriorId = pedidoAnteriorId; this.registradoPorId = registradoPorId;
    }
    public UUID getId() { return id; }
    public UUID getGimnasioId() { return gimnasioId; }
    public UUID getClienteId() { return clienteId; }
    public DetallePedido getDetalle() { return detalle; }
    public BigDecimal getTotal() { return total; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public LocalDate getFechaBaseVencimiento() { return fechaBaseVencimiento; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public UUID getPedidoAnteriorId() { return pedidoAnteriorId; }
}
