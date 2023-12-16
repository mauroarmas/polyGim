/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author agust
 */
public class Localidad {

    //
    // Fields
    //
    private int idLocalidad;
    private String localidad;

    //
    // Constructors
    //
    public Localidad() {
    }

    ;

  public Localidad(int idLocalidad, String localidad) {
        this.idLocalidad = idLocalidad;
        this.localidad = localidad;
    }

    //
    // Accessor methods
    //
    /**
     * Set the value of idLocalidad
     *
     * @param newVar the new value of idLocalidad
     */
    public void setIdLocalidad(int newVar) {
        idLocalidad = newVar;
    }

    /**
     * Get the value of idLocalidad
     *
     * @return the value of idLocalidad
     */
    public int getIdLocalidad() {
        return idLocalidad;
    }

    /**
     * Set the value of localidad
     *
     * @param newVar the new value of localidad
     */
    public void setLocalidad(String newVar) {
        localidad = newVar;
    }

    /**
     * Get the value of localidad
     *
     * @return the value of localidad
     */
    public String getLocalidad() {
        return localidad;
    }

    //
    // Other methods
    //
    @Override
    public String toString() {
        return "Localidad{" + "idLocalidad=" + idLocalidad + ", localidad=" + localidad + '}';
    }

}
