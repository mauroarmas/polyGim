/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author agust
 */
public class Detalle {
//
  // Fields
  //

  private int idDetalle;
  private int cantidad;
  private double subtotal;

  private Membresia m_mem;
  
  //
  // Constructors
  //
  public Detalle () { };
  
  //
  // Methods
  //


  //
  // Accessor methods
  //

  /**
   * Set the value of idDetalle
   * @param newVar the new value of idDetalle
   */
  public void setIdDetalle (int newVar) {
    idDetalle = newVar;
  }

  /**
   * Get the value of idDetalle
   * @return the value of idDetalle
   */
  public int getIdDetalle () {
    return idDetalle;
  }

  /**
   * Set the value of cantidad
   * @param newVar the new value of cantidad
   */
  public void setCantidad (int newVar) {
    cantidad = newVar;
  }

  /**
   * Get the value of cantidad
   * @return the value of cantidad
   */
  public int getCantidad () {
    return cantidad;
  }

  /**
   * Set the value of subtotal
   */
  public void setSubtotal () {
    subtotal = cantidad*m_mem.getPrecio();
  }

  /**
   * Get the value of subtotal
   * @return the value of subtotal
   */
  public double getSubtotal () {
    return subtotal;
  }

  /**
   * Set the value of m_mem
   * @param newVar the new value of m_mem
   */
  public void setMem (Membresia newVar) {
    m_mem = newVar;
  }

  /**
   * Get the value of m_mem
   * @return the value of m_mem
   */
  public Membresia getMem () {
    return m_mem;
  }

  //
  // Other methods
  //
    
}
