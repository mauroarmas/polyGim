package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "configuracion_gimnasio")
public class ConfiguracionGimnasio {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(name = "cantidad_maxima_compra", nullable = false) private int cantidadMaximaCompra;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected ConfiguracionGimnasio() { }
    public ConfiguracionGimnasio(UUID id, int cantidadMaximaCompra) { this.id = id; this.cantidadMaximaCompra = cantidadMaximaCompra; }
    public UUID getGimnasioId() { return gimnasioId; }
    public int getCantidadMaximaCompra() { return cantidadMaximaCompra; }
}
