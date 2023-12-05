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

  private int idPedido;
  private LocalDate fechaPedido;
  private LocalDate fechaVencimiento;
  private boolean estado=false;
  //private Detalle detalles;
  private ArrayList<Detalle> detalles ;
 // private Pago pagos;
  private ArrayList<Pago> pagos;

  protected Cliente m_cliente;
  
  private double total;
  
  //
  // Constructors
  //
  public Pedido () { 
     this.detalles= new ArrayList<>();
     this.pagos = new ArrayList<>();
  
  };
  
  //
  // Methods
  //


  //
  // Accessor methods
  //

  /**
   * Set the value of idPedido
   * @param newVar the new value of idPedido
   */
  public void setIdPedido (int newVar) {
    idPedido = newVar;
  }

  /**
   * Get the value of idPedido
   * @return the value of idPedido
   */
  public int getIdPedido () {
    return idPedido;
  }

  /**
   * Set the value of fechaPedido
   * @param newVar the new value of fechaPedido
   */
  public void setFechaPedido (LocalDate newVar) {
    fechaPedido = newVar;
  }

  /**
   * Get the value of fechaPedido
   * @return the value of fechaPedido
   */
  public LocalDate getFechaPedido () {
    return fechaPedido;
  }

  /**
   * Set the value of fechaVencimiento
   * @param newVar the new value of fechaVencimiento
   */
  public void setFechaVencimiento (LocalDate newVar) {
    fechaVencimiento = newVar;
  }

  /**
   * Get the value of fechaVencimiento
   * @return the value of fechaVencimiento
   */
  public LocalDate getFechaVencimiento () {
    return fechaVencimiento;
  }

  /**
   * Set the value of estado
   * @param newVar the new value of estado
   */
  public void setEstado (boolean newVar) {
    estado = newVar;
  }

  /**
   * Get the value of estado
   * @return the value of estado
   */
  public boolean getEstado () {
    return estado;
  }
/*
  /**
   * Set the value of detalles
   * @param newVar the new value of detalles
   */
  //public void setDetalles (Detalle newVar) {
    //detalles = newVar;
 // }

  /**
   * Get the value of detalles
   * @return the value of detalles
   */
  //public Detalle getDetalles () {
    //return detalles;
 // }

  /**
   * Set the value of pagos
     * @param idDetalle  
     * @param cantidad  
     * @param mem  
  */
    public void agregarDetalle(int idDetalle,int cantidad,Membresia mem){
        Detalle detalle = new Detalle();
        detalle.setIdDetalle(idDetalle);
        detalle.setCantidad(cantidad);
        detalle.setMem(mem);
        detalle.setSubtotal();
        detalles.add(detalle);
    
    }

    public ArrayList<Detalle> getDetalles() {
        return detalles;
    }
    
   
    public void agregarPago(int idPago , double monto){
    Pago pago = new Pago();
    pago.setIdPago(idPago);
    pago.setMonto(monto);
    pagos.add(pago);
    
    }
    
    
  
    
   /* 
  public void setPagos (Pago newVar) {
    pagos = newVar;
  }

  /**
   * Get the value of pagos
   * @return the value of pagos
   */
  //public Pago getPagos () {
  //  return pagos;
//  }

    public ArrayList<Pago> getPagos() {
        return pagos;
    }
    
    
    
    

  /**
   * Set the value of m_cliente
   * @param newVar the new value of m_cliente
   */
  protected void setCliente (Cliente newVar) {
    m_cliente = newVar;
  }

  /**
   * Get the value of m_cliente
   * @return the value of m_cliente
   */
  protected Cliente getCliente () {
    return m_cliente;
  }

  //
  // Other methods
  //

 
    
    
}
