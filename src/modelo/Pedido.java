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
public class Pedido {
    private int idPedido;
    private LocalDate fechaPedido;
    private LocalDate fechaVencimiento;
    private boolean estado;
    private Cliente cliente;

    public Pedido() {
    }

    public Pedido(int idPedido, LocalDate fechaPedido, LocalDate fechaVencimiento, boolean estado, Cliente cliente) {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.cliente = cliente;
    }

    
    
    
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

  
    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

 
    
    
}
