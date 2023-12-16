/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.Db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author mercadito
 */
public class ClienteDb extends Db {   ///***HEREDAMOS LOS METODOS PARA LAS COSNULTAS
  
    
  public int ingresarCLiente(int dni) throws SQLException{
  
      
   
   
   
  return 0;
  }
  
  public int modificarCliente(int dni ){

    
  
  return 0;
  }
  
  
  public int eliminarCliente(int dni ){

    
  
  return 0;
  }
    
  public ResultSet getAllCliente() throws SQLException{
   String sql = "SELECT * FROM Cliente";
      
     ResultSet res = ejecutarSelect(sql);
      
      
      
  return res;
  }  

    @Override
    protected int ejecutarUpdateDeleteInsert(String sql, Object... params) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected ResultSet ejecutarSelect(String sql, Object... params) throws SQLException {
        return super.ejecutarSelect(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected int ejecutarInsertConDevolucionDeClavePrimariaGenerada(String sql, Object... params) throws SQLException {
        return super.ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

   
    
   
    
    
    
    
    
    
}
