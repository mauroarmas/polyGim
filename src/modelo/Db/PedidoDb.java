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
public class PedidoDb extends Db{

    @Override
    protected int ejecutarUpdateDeleteInsert(String sql) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected ResultSet ejecutarSelect(String sql) throws SQLException {
        return super.ejecutarSelect(sql); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }
    
    
    
}
