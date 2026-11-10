package com.gymplatform.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "cliente")
public class Cliente {
    @Id private UUID id;
    @TenantId @Column(name = "gimnasio_id", nullable = false, updatable = false) private UUID gimnasioId;
    @Column(nullable = false, length = 8) private String dni;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 100) private String apellido;
    @Column(length = 40) private String telefono;
    @Column(name = "canal_llegada_id") private UUID canalLlegadaId;
    @Embedded private Direccion direccion;
    @Column(name = "localidad_id") private UUID localidadId;
    @Column(name = "fecha_ingreso", nullable = false, updatable = false) private LocalDate fechaIngreso;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    protected Cliente() { }
    public Cliente(UUID id, LocalDate fechaIngreso, String dni, String nombre, String apellido,
                   String telefono, UUID canalLlegadaId, Direccion direccion, UUID localidadId) {
        this.id = id; this.fechaIngreso = fechaIngreso; update(dni, nombre, apellido, telefono, canalLlegadaId, direccion, localidadId);
    }
    public void update(String dni, String nombre, String apellido, String telefono, UUID canalLlegadaId, Direccion direccion, UUID localidadId) {
        this.dni = dni; this.nombre = nombre; this.apellido = apellido; this.telefono = telefono;
        this.canalLlegadaId = canalLlegadaId; this.direccion = direccion; this.localidadId = localidadId;
    }
    public UUID getId() { return id; }
    public UUID getGimnasioId() { return gimnasioId; }
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public UUID getCanalLlegadaId() { return canalLlegadaId; }
    public Direccion getDireccion() { return direccion; }
    public UUID getLocalidadId() { return localidadId; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
}
