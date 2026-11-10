package com.gymplatform.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
@Entity @Table(name = "localidad")
public class Localidad extends CatalogoNombrado {
    protected Localidad() { }
    public Localidad(UUID id, String nombre) { super(id, nombre); }
}
