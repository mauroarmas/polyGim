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

    
  public int ingresarCLiente(int dni) throws SQLException{
  
   String sql = "asdasd 1";
   
   sql=sql.replace("1", Integer.toString(dni));

  
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
