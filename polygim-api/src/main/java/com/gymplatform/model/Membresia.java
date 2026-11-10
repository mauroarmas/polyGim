package com.gymplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "membresia")
public class Membresia {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(name = "disciplina_id", nullable = false, updatable = false) private UUID disciplinaId;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal precio;
    @Column(nullable = false) private int duracion;
    @Column(name = "unidad_duracion", nullable = false, length = 10) private String unidadDuracion;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected Membresia() { }
    public Membresia(UUID id, UUID disciplinaId, String nombre, BigDecimal precio, int duracion, String unidadDuracion) {
        this.id = id; this.disciplinaId = disciplinaId; update(nombre, precio, duracion, unidadDuracion);
    }
    public void update(String nombre, BigDecimal precio, int duracion, String unidadDuracion) {
        this.nombre = nombre; this.precio = precio; this.duracion = duracion; this.unidadDuracion = unidadDuracion;
    }
    public UUID getId() { return id; }
    public UUID getGimnasioId() { return gimnasioId; }
    public UUID getDisciplinaId() { return disciplinaId; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getDuracion() { return duracion; }
    public String getUnidadDuracion() { return unidadDuracion; }
}
