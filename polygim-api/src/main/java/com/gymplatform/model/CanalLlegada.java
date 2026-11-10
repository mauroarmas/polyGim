package com.gymplatform.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
@Entity @Table(name = "canal_llegada")
public class CanalLlegada extends CatalogoNombrado {
    protected CanalLlegada() { }
    public CanalLlegada(UUID id, String nombre) { super(id, nombre); }
}
