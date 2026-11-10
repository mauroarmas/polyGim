package com.gymplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Direccion {
    @Column(length = 160) private String calle;
    @Column(length = 20) private String altura;
    protected Direccion() { }
    public Direccion(String calle, String altura) { this.calle = calle; this.altura = altura; }
    public String getCalle() { return calle; }
    public String getAltura() { return altura; }
}
