/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author agust
 */
public class Cliente {
    //
    // Fields
    //

    private int dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaIngreso;
    private String telefono;
    private String comoLlego;
    private ArrayList<Pedido> pedidos;
    private Direccion m_direccion;

    //
    // Constructors
    //
    public Cliente() {
        this.pedidos = new ArrayList<>();
    }

    ;
  
    public Cliente(int dni, String nombre, String apellido, LocalDate fechaIngreso, String telefono, String comoLlego, Direccion m_direccion) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaIngreso = fechaIngreso;
        this.telefono = telefono;
        this.comoLlego = comoLlego;
        this.m_direccion = m_direccion;
        this.pedidos = new ArrayList<>();
    }

    //
    // Accessor methods
    //
    /**
     * Set the value of dni
     *
     * @param newVar the new value of dni
     */
    public void setDni(int newVar) {
        dni = newVar;
    }

    /**
     * Get the value of dni
     *
     * @return the value of dni
     */
    public int getDni() {
        return dni;
    }

    /**
     * Set the value of nombre
     *
     * @param newVar the new value of nombre
     */
    public void setNombre(String newVar) {
        nombre = newVar;
    }

    /**
     * Get the value of nombre
     *
     * @return the value of nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Set the value of apellido
     *
     * @param newVar the new value of apellido
     */
    public void setApellido(String newVar) {
        apellido = newVar;
    }

    /**
     * Get the value of apellido
     *
     * @return the value of apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Set the value of fechaIngreso
     *
     * @param newVar the new value of fechaIngreso
     */
    public void setFechaIngreso(LocalDate newVar) {
        fechaIngreso = newVar;
    }

    /**
     * Get the value of fechaIngreso
     *
     * @return the value of fechaIngreso
     */
    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    /**
     * Set the value of telefono
     *
     * @param newVar the new value of telefono
     */
    public void setTelefono(String newVar) {
        telefono = newVar;
    }

    /**
     * Get the value of telefono
     *
     * @return the value of telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Set the value of comoLlego
     *
     * @param newVar the new value of comoLlego
     */
    public void setComoLlego(String newVar) {
        comoLlego = newVar;
    }

    /**
     * Get the value of comoLlego
     *
     * @return the value of comoLlego
     */
    public String getComoLlego() {
        return comoLlego;
    }

    /**
     * Set the value of m_direccion
     *
     * @param newVar the new value of m_direccion
     */
    private void setDireccion(Direccion newVar) {
        m_direccion = newVar;
    }

    /**
     * Get the value of m_direccion
     *
     * @return the value of m_direccion
     */
    private Direccion getDireccion() {
        return m_direccion;
    }

    /**
     * Add a Pedido object to the pedidoVector List
     */
    public void addPedido(Pedido new_object) {
        pedidos.add(new_object);
    }

    /**
     * Remove a Pedido object from pedidoVector List
     */
    public void removePedido(Pedido new_object) {
        pedidos.remove(new_object);
    }

    /**
     * Get the List of Pedido objects held by pedidoVector
     *
     * @return List of Pedido objects held by pedidoVector
     */
    public ArrayList getPedidoList() {
        return pedidos;
    }

    //
    // Other methods
    //
    @Override
    public String toString() {
        return "Cliente{" + "dni=" + dni + ", nombre=" + nombre + ", apellido=" + apellido + ", fechaIngreso=" + fechaIngreso + ", telefono=" + telefono + ", comoLlego=" + comoLlego + ", calle=" + m_direccion.getCalle() + ", altura=" + m_direccion.getAltura()
                + "Localidad=" + m_direccion.getLocalidad().getLocalidad() + '}';
    }

    public void mostrarPedidos() {
        for (int i = 0; i < pedidos.size(); i++) {
            //System.out.println("PEDIDOS NRO "+ (i+1));
            System.out.println(pedidos.get(i).getIdPedido());
            System.out.println(pedidos.get(i).getFechaPedido());
            System.out.println(pedidos.get(i).getFechaVencimiento());
            System.out.println(pedidos.get(i).getEstado());
            System.out.println(pedidos.get(i).getTotal());

        }
    }

}
