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
public class LocalidadDb extends Db{
    
    public ResultSet getAllLocalidad() throws SQLException{
    String sql = "SELECT * FROM Localidad";
    ResultSet res = ejecutarSelect(sql);
    
    return res;
    
    }
    
    

    @Override
    protected int ejecutarUpdateDeleteInsert(String sql, Object... params) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public ResultSet ejecutarSelect(String sql, Object... params) throws SQLException {
        return super.ejecutarSelect(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected int ejecutarInsertConDevolucionDeClavePrimariaGenerada(String sql, Object... params) throws SQLException {
        return super.ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }
    
}
