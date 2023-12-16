/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.Db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author mercadito
 */
public class PedidoDb extends Db {

  
    

    public int ingresarPedido(LocalDate fechaI, boolean estado, LocalDate fechaV, int clienteDni, double total) throws SQLException {
        String sql = "INSERT INTO Pedido (fecha_pedido, estado, fecha_vencimiento, clienteDni, total) VALUES (?, ?, ?, ?, ?)";
       java.sql.Date fechaIsql = java.sql.Date.valueOf(fechaI);
        java.sql.Date fechaVsql = java.sql.Date.valueOf(fechaV);
int idGenerado = ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, fechaIsql, estado, fechaVsql, clienteDni, total);
        

         
        return idGenerado;
    }

    public int ingresarDetalle(int idPedido, int cantidad, int idMembresia, double subtotal) throws SQLException {
     String sql = "INSERT INTO Detalle (idPedido, cantidad, idMembresia, subtotal) VALUES (?, ?, ?, ?)";
     int idGenerado = ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, idPedido, cantidad, idMembresia, subtotal);  
     
     return idGenerado;
    }
    public ResultSet selectDetalle() throws SQLException{
    String sql = "SELECT * FROM Detalle";    
    ResultSet res = ejecutarSelect(sql);
    return res;
    }
    public ResultSet selectPedido() throws SQLException{
    
    String sql = "SELECT * FROM Pedido";
    ResultSet res = ejecutarSelect(sql);
    return res;
    }
    
    public ResultSet getPedidoByDni(int dni) throws SQLException{
    String sql = "SELECT * FROM Pedido WHERE clienteDni=?";
    ResultSet res = ejecutarSelect(sql,dni);
    return res;
    
    }
    public int modificarEstadoPedido(int idPedido) throws SQLException{
    String sql = "UPDATE Pedido SET estado=true WHERE idPedido=?";
    
    int filasAfectadas = ejecutarUpdateDeleteInsert(sql,idPedido);
    return filasAfectadas; 
    }
   
    
    
    
    ///*********************FUNCIONES HEREDADAS**************////////////////////

    @Override
    protected int ejecutarInsertConDevolucionDeClavePrimariaGenerada(String sql, Object... params) throws SQLException {
        return super.ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected ResultSet ejecutarSelect(String sql, Object... params) throws SQLException {
        return super.ejecutarSelect(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected int ejecutarUpdateDeleteInsert(String sql, Object... params) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

     
    


}
