/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author agust
 */
public class Membresia {

    //
    // Fields
    //
    private int idMembresia;
    private String membresia;
    private double precio;

    //
    // Constructors
    //
    public Membresia() {
    }

    ;
  

  public Membresia(int idMembresia, String membresia, double precio) {
        this.idMembresia = idMembresia;
        this.membresia = membresia;
        this.precio = precio;

    }

    //
    // Accessor methods
    //
    /**
     * Set the value of idMembresia
     *
     * @param newVar the new value of idMembresia
     */
    public void setIdMembresia(int newVar) {
        idMembresia = newVar;
    }

    /**
     * Get the value of idMembresia
     *
     * @return the value of idMembresia
     */
    public int getIdMembresia() {
        return idMembresia;
    }

    /**
     * Set the value of membresia
     *
     * @param newVar the new value of membresia
     */
    public void setMembresia(String newVar) {
        membresia = newVar;
    }

    /**
     * Get the value of membresia
     *
     * @return the value of membresia
     */
    public String getMembresia() {
        return membresia;
    }

    /**
     * Set the value of precio
     *
     * @param newVar the new value of precio
     */
    public void setPrecio(double newVar) {
        precio = newVar;
    }

    /**
     * Get the value of precio
     *
     * @return the value of precio
     */
    public double getPrecio() {
        return precio;
    }

    //
    // Other methods
    //
}
