package com.gymplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
public class DetallePedido {
    @Column(name = "membresia_id", nullable = false, updatable = false) private UUID membresiaId;
    @Column(nullable = false, updatable = false) private int cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2, updatable = false) private BigDecimal precioUnitario;
    @Column(name = "duracion_membresia", nullable = false, updatable = false) private int duracionMembresia;
    @Column(name = "unidad_duracion", nullable = false, length = 10, updatable = false) private String unidadDuracion;
    protected DetallePedido() { }
    public DetallePedido(UUID membresiaId, int cantidad, BigDecimal precioUnitario, int duracionMembresia, String unidadDuracion) {
        this.membresiaId = membresiaId; this.cantidad = cantidad; this.precioUnitario = precioUnitario;
        this.duracionMembresia = duracionMembresia; this.unidadDuracion = unidadDuracion;
    }
    public UUID getMembresiaId() { return membresiaId; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getDuracionMembresia() { return duracionMembresia; }
    public String getUnidadDuracion() { return unidadDuracion; }
}
