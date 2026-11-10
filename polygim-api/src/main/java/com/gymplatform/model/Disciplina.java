package com.gymplatform.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
@Entity @Table(name = "disciplina")
public class Disciplina extends CatalogoNombrado {
    protected Disciplina() { }
    public Disciplina(UUID id, String nombre) { super(id, nombre); }
}
