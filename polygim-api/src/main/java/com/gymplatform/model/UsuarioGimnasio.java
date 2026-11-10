package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "usuario_gimnasio")
public class UsuarioGimnasio {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected UsuarioGimnasio() { }
    public UsuarioGimnasio(UUID id) { this.id = id; }
    public UUID getGimnasioId() { return gimnasioId; }
    public UUID getId() { return id; }
}
