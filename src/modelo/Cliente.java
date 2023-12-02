/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author agust
 */
public class Cliente {
    private int dni;
    private String apellido;
    private String nombre;
    private String telefono;
    private LocalDate  fechaIngreso; 
    private String comoLlego;

    public Cliente() {
    }

    public Cliente(int dni, String apellido, String nombre, String telefono, LocalDate fechaIngreso, String comoLlego) {
        this.dni = dni;
        this.apellido = apellido;
        this.nombre = nombre;
        this.telefono = telefono;
        this.fechaIngreso = fechaIngreso;
        this.comoLlego = comoLlego;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

   

    public String getComoLlego() {
        return comoLlego;
    }

    public void setComoLlego(String comoLlego) {
        this.comoLlego = comoLlego;
    }

    @Override
    public String toString() {
        return "Cliente{" + "dni=" + dni + ", apellido=" + apellido + ", nombre=" + nombre + ", telefono=" + telefono + ", fechaIngreso=" + fechaIngreso + ", comoLlego=" + comoLlego + '}';
    }
    
    
    
    
}
