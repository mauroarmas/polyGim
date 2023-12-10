/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.Db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author mercadito
 */
public class ClienteDb extends Db {   ///***HEREDAMOS LOS METODOS PARA LAS COSNULTAS
  private String tabla="Cliente";
    
  public int ingresarCLiente(int dni) throws SQLException{
  
      
      
   String sql = "INSERT tabla INTO (dni) VALUES (?)";
   
   sql=sql.replace("?", Integer.toString(dni));
     sql=sql.replace("tabla", tabla);

  
   int filaAfectadas = ejecutarUpdateDeleteInsert(sql);
   
   
   
  return filaAfectadas;
  }
  
  public int modificarCliente(int dni ){

    
  
  return 0;
  }
  
  
  public int eliminarCliente(int dni ){

    
  
  return 0;
  }
    
  public ResultSet obtenerClientes(){
   
      
      
      
      
      
  return null;
  }  
    
    //// SELECT idPedido, fecha_pedido,estado,fecha_vencimiento FROM Pedido WHERE dni=? ;
    
    
    
    
    
    
    
    //********+********METODOS PARA LAS COSNULTAS*************************//
    
    @Override
    protected int ejecutarUpdateDeleteInsert(String sql) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected ResultSet ejecutarSelect(String sql) throws SQLException {
        return super.ejecutarSelect(sql); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }
    
       //********+********METODOS PARA LAS COSNULTAS*************************//
    
    
    
    
    
    
    
}
