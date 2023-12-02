/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;

/**
 *
 * @author agust
 */
public class Localidad {
    private int idLocalidad;
    private String localidad;
    ArrayList<Direccion> direcciones = new ArrayList<>();
    
    public Localidad() {
    }

    public Localidad(String localidad) {
        this.localidad = localidad;
    }

    public int getIdLocalidad() {
        return idLocalidad;
    }

    public void setIdLocalidad(int idLocalidad) {
        this.idLocalidad = idLocalidad;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }


    public void addDirecciones(String calle, int altura, Cliente c){
     Direccion d = new Direccion();
     d.setAltura(altura);
     d.setCalle(calle);
     d.setC(c);
     direcciones.add(d);
    
    }
    
}
