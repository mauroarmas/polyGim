package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class CatalogoNombrado {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(nullable = false, length = 100) private String nombre;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected CatalogoNombrado() { }
    protected CatalogoNombrado(UUID id, String nombre) { this.id = id; this.nombre = nombre; }
    public UUID getId() { return id; }
    public UUID getGimnasioId() { return gimnasioId; }
    public String getNombre() { return nombre; }
    public void rename(String nombre) { this.nombre = nombre; }
}
