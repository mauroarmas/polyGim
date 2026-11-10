package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "cuenta_acceso")
public class CuentaAcceso {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 254) private String email;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(nullable = false, length = 20) private String rol;
    @Column(nullable = false) private boolean habilitada;
    @Column(name = "gimnasio_id", updatable = false) private UUID gimnasioId;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected CuentaAcceso() { }
    public CuentaAcceso(UUID id, String email, String passwordHash, String rol, UUID gimnasioId) {
        this.id = id; this.email = email; this.passwordHash = passwordHash; this.rol = rol;
        this.gimnasioId = gimnasioId; this.habilitada = true;
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public UUID getGimnasioId() { return gimnasioId; }
    public boolean isHabilitada() { return habilitada; }
    public void deshabilitar() { habilitada = false; }
}
