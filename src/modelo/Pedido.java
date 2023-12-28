/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author agust
 */
public class Pedido {

    //
    // Fields
    //
    private Integer idPedido;
    private LocalDate fechaPedido;
    private LocalDate fechaVencimiento;
    private boolean estado;
    private ArrayList<Detalle> detalles;
    private ArrayList<Pago> pagos;
    private Cliente m_cliente;
    private double total;

    //
    // Constructors
    //
    public Pedido() {
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();

    }

    ;

    public Pedido(Integer idPedido, LocalDate fechaPedido, LocalDate fechaVencimiento, boolean estado, Cliente m_cliente, double total) {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.m_cliente = m_cliente;
        this.total = total;
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();
    }

    //
    // Methods
    //
    //
    // Accessor methods
    //
    /**
     * Set the value of idPedido
     *
     * @param newVar the new value of idPedido
     */
    public void setIdPedido(Integer newVar) {
        idPedido = newVar;
    }

    /**
     * Get the value of idPedido
     *
     * @return the value of idPedido
     */
    public Integer getIdPedido() {
        return idPedido;
    }

    /**
     * Set the value of fechaPedido
     *
     * @param newVar the new value of fechaPedido
     */
    public void setFechaPedido(LocalDate newVar) {
        fechaPedido = newVar;
    }

    /**
     * Get the value of fechaPedido
     *
     * @return the value of fechaPedido
     */
    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    /**
     * Set the value of fechaVencimiento
     *
     * @param newVar the new value of fechaVencimiento
     */
    public void setFechaVencimiento(LocalDate newVar) {
        fechaVencimiento = newVar;
    }

    /**
     * Get the value of fechaVencimiento
     *
     * @return the value of fechaVencimiento
     */
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    /**
     * Set the value of estado
     *
     * @param newVar the new value of estado
     */
    public void setEstado(boolean newVar) {
        estado = newVar;
    }

    /**
     * Get the value of estado
     *
     * @return the value of estado
     */
    public boolean getEstado() {
        return estado;
    }

    
    /**
     * Set the value of pagos
     *
     * @param idDetalle
     * @param cantidad
     * @param mem
     */
    public void agregarDetalle(int cantidad, Membresia mem) {
        Detalle detalle = new Detalle();
        //detalle.setIdDetalle(idDetalle);
        detalle.setCantidad(cantidad);
        detalle.setMem(mem);
        detalle.setSubtotal();
        detalles.add(detalle);

    }

    public ArrayList<Detalle> getDetalles() {
        return detalles;
    }

    public void agregarIdDetalle(int index, int idDetalle) {
        detalles.get(index).setIdDetalle(idDetalle);

    }

    public void agregarDetalles(Integer idDetalle, int cantidad, Membresia mem) {
        Detalle detalle = new Detalle();
        detalle.setIdDetalle(idDetalle);
        detalle.setCantidad(cantidad);
        detalle.setMem(mem);
        detalle.setSubtotal();
        detalles.add(detalle);
    }
   public void modificarArrayDetalle(int index,int cantidad, Membresia mem){
       detalles.get(index).setCantidad(cantidad);
        detalles.get(index).setMem(mem);
        detalles.get(index).setSubtotal();
        
       
   
   }
    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void calcularSumaSubtotales() {
        total=0;
        for (Detalle detalle : detalles) {
            total = total + detalle.getSubtotal();
        }

    }

    public void agregarPago(int idPago, double monto) {
        Pago pago = new Pago();
        pago.setIdPago(idPago);
        pago.setMonto(monto);
        pagos.add(pago);

    }

    public ArrayList<Pago> getPagos() {
        return pagos;
    }

    /**
     * Set the value of m_cliente
     *
     * @param newVar the new value of m_cliente
     */
    public void setCliente(Cliente newVar) {
        m_cliente = newVar;
    }

    /**
     * Get the value of m_cliente
     *
     * @return the value of m_cliente
     */
    public Cliente getCliente() {
        return m_cliente;
    }

    //
    // Other methods
    //
    public void obtenerFechaVen(LocalDate fechaPedido, String nombreMembresia, String cantidad) {

        int cantidadM = Integer.parseInt(cantidad);
        fechaVencimiento = null;
        if (nombreMembresia.equals("Mensual")) {

            fechaVencimiento = fechaPedido.plusMonths(cantidadM);

        } else {

            fechaVencimiento = fechaPedido.plusWeeks(cantidadM);

        }

    }

    @Override
    public String toString() {
        return "Pedido{" + "idPedido=" + idPedido + ", fechaPedido=" + fechaPedido + ", fechaVencimiento=" + fechaVencimiento + ", estado=" + estado + ", dni_cliente=" + m_cliente.getDni() + ", total=" + total + '}';
    }

    
    public void mostrarDetalles(){
    
           for(int i=0;i<detalles.size();i++){
           
               System.out.println(detalles.get(i).getIdDetalle());
               System.out.println(detalles.get(i).getCantidad());
               System.out.println(detalles.get(i).getSubtotal());
               System.out.println(detalles.get(i).getMem().getMembresia());
               System.out.println(detalles.get(i).getMem().getPrecio());
           
           }
    
    }
    
    public void mostrarPagos(){
    
           for(int i=0;i<pagos.size();i++){
           
               System.out.println(pagos.get(i).getIdPago());
               System.out.println(pagos.get(i).getMonto());
               
           
           }
    
    }
    
    
}
