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
public class Direccion {

//
    // Fields
    //
    private int idDireccion;
    private String calle;
    private int altura;
    private Localidad m_localidad;

    //
    // Constructors
    //
    public Direccion() {
    }

    ;
  
  //
  // Methods
  ////
  // Methods
  //

  public Direccion(int idDireccion, String calle, int altura, Localidad m_localidad) {
        this.idDireccion = idDireccion;
        this.calle = calle;
        this.altura = altura;
        this.m_localidad = m_localidad;
    }

    //
    // Accessor methods
    //
    /**
     * Set the value of idDireccion
     *
     * @param newVar the new value of idDireccion
     */
    public void setIdDireccion(int newVar) {
        idDireccion = newVar;
    }

    /**
     * Get the value of idDireccion
     *
     * @return the value of idDireccion
     */
    public int getIdDireccion() {
        return idDireccion;
    }

    /**
     * Set the value of calle
     *
     * @param newVar the new value of calle
     */
    public void setCalle(String newVar) {
        calle = newVar;
    }

    /**
     * Get the value of calle
     *
     * @return the value of calle
     */
    public String getCalle() {
        return calle;
    }

    /**
     * Set the value of altura
     *
     * @param newVar the new value of altura
     */
    public void setAltura(int newVar) {
        altura = newVar;
    }

    /**
     * Get the value of altura
     *
     * @return the value of altura
     */
    public int getAltura() {
        return altura;
    }

    /**
     * Set the value of m_localidad
     *
     * @param newVar the new value of m_localidad
     */
    public void setLocalidad(Localidad newVar) {
        m_localidad = newVar;
    }

    /**
     * Get the value of m_localidad
     *
     * @return the value of m_localidad
     */
    public Localidad getLocalidad() {
        return m_localidad;
    }

    //
    // Other methods
    //
    @Override
    public String toString() {
        return "Direccion{" + "idDireccion=" + idDireccion + ", calle=" + calle + ", altura=" + altura + ", localidad=" + m_localidad.getLocalidad() + '}';
    }

}
