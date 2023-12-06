/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.Db;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author agust
 */
public class Db {

    private static final String DRIVER = "org.mariadb.jdbc.Driver";
    private static final String HOST = "localhost";
    private static final String USUARIO = "mercado";
    private static final String PASSWORD = "agugu261201";
    private static final int PORT = 3306;
    private static final String database = "POLLYGYM";
    private static final String URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + database;

/////***************CARGAMOS EL  DRIVER PARA LLA CONEXION***************//////    
    static {

        try {

            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {

            JOptionPane.showMessageDialog(null, "ERROR EN EL DRIVER: " + e);

        }

    }
/////***************************************************//////



/////***************CONEXION A LA BASE DE DATO***************//////
    
    public Connection getConnection() {

        Connection con = null;

        try {

            con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            //JOptionPane.showMessageDialog(null,"");
        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Conexion erronea: " + e);

        }

        return con;

    }
    
    /////******************************//////
    
    
    
    
  /////**************FUNCION PARA HACER UNA CONSULTA SELECT****************//////
    protected ResultSet ejecutarSelect(String sql) throws SQLException {
        Connection c = getConnection();
        ResultSet res = null;
        try {
            Statement statement = c.createStatement();
            statement.execute(sql);
            res = statement.getResultSet();

        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return res;
    }

    /////************************************************************+************////// 
    
    
    
     /////**************FUNCION PARA HACER UNA CONSULTA UPDATE,DELETE O INSERT****************//////
    protected int ejecutarUpdateDeleteInsert(String sql) throws SQLException {
        Connection c = getConnection();
        int filasAfectadas = 0;
        try {
            Statement statement = c.createStatement();
            filasAfectadas = statement.executeUpdate(sql);
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            if (c != null) {
                c.close();
            }

        }
        return filasAfectadas;

    }
    /////************************************************************+************////// 
   
   
    
}
