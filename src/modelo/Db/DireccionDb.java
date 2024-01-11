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
public class DireccionDb extends Db {

    public ResultSet getAllDireccion() throws SQLException {
        String sql = "SELECT * FROM Direccion";

        ResultSet res = ejecutarSelect(sql);
        return res;
    }

    @Override
    public int ejecutarUpdateDeleteInsert(String sql, Object... params) throws SQLException {
        return super.ejecutarUpdateDeleteInsert(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public ResultSet ejecutarSelect(String sql, Object... params) throws SQLException {
        return super.ejecutarSelect(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public int ejecutarInsertConDevolucionDeClavePrimariaGenerada(String sql, Object... params) throws SQLException {
        return super.ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, params); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

}
