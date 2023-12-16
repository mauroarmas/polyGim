/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;

/**
 *
 * @author mercadito
 */
public class Servicio {

    private int idServicio;
    private String servicio;
    private ArrayList<Membresia> membresias;

    public Servicio() {
        membresias = new ArrayList<>();

    }

    public Servicio(int idServicio, String servicio) {
        this.idServicio = idServicio;
        this.servicio = servicio;
        membresias = new ArrayList<>();
    }

    public void agregarMembresias(int idMembresia, String membresia, double precio) {
        Membresia a = new Membresia();
        a.setIdMembresia(idMembresia);
        a.setMembresia(membresia);
        a.setPrecio(precio);
        membresias.add(a);

    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public ArrayList<Membresia> getMembresias() {
        return membresias;
    }

    public void agregarIdMembresia(int index, int idMembresias) {
        membresias.get(index).setIdMembresia(idMembresias);

    }

    @Override
    public String toString() {
        return "Servicio{" + "idServicio=" + idServicio + ", servicio=" + servicio + '}';
    }

    public void mostrarMembresia() {

        for (int i = 0; i < membresias.size(); i++) {

            System.out.println(membresias.get(i).getIdMembresia());
            System.out.println(membresias.get(i).getMembresia());
            System.out.println(membresias.get(i).getPrecio());

        }

    }

}
