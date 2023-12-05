/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author mercadito
 */
public class Pago {
    //
  // Fields
  //

  private int idPago;
  private double monto;
  
  //
  // Constructors
  //
  public Pago () { };
  
  //
  // Methods
  //


  //
  // Accessor methods
  //

  /**
   * Set the value of idPago
   * @param newVar the new value of idPago
   */
  public void setIdPago (int newVar) {
    idPago = newVar;
  }

  /**
   * Get the value of idPago
   * @return the value of idPago
   */
  public int getIdPago () {
    return idPago;
  }

  /**
   * Set the value of monto
   * @param newVar the new value of monto
   */
  public void setMonto (double newVar) {
    monto = newVar;
  }

  /**
   * Get the value of monto
   * @return the value of monto
   */
  public double getMonto () {
    return monto;
  }

  //
  // Other methods
  //
}
