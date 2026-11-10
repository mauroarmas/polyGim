package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "gimnasio")
public class Gimnasio {
    @Id private UUID id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(name = "logo_url", nullable = false, length = 2048) private String logoUrl;
    @JdbcTypeCode(java.sql.Types.CHAR)
    @Column(name = "color_primario", nullable = false, length = 7) private String colorPrimario;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected Gimnasio() { }
    public Gimnasio(UUID id, String nombre, String logoUrl, String colorPrimario) {
        this.id = id; this.nombre = nombre; this.logoUrl = logoUrl; this.colorPrimario = colorPrimario;
    }
    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLogoUrl() { return logoUrl; }
    public String getColorPrimario() { return colorPrimario; }
}
