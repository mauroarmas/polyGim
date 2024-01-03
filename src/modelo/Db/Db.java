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
    private static final String USUARIO = "agustin";
    private static final String PASSWORD = "luciaAgustin261201";
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
    
    

    
     protected int ejecutarInsertConDevolucionDeClavePrimariaGenerada(String sql, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int idGenerado = -1;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Establecer los parámetros en la consulta preparada
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
                System.out.println(params[i]);
            }

            int filasAfectadas = preparedStatement.executeUpdate();
               System.out.println(filasAfectadas);
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                         System.out.println(idGenerado);
                    }
                }
            }
        } finally {
            closeResources(connection, preparedStatement);
        }

        return idGenerado;
    }
    
     protected ResultSet ejecutarSelect(String sql, Object... params) throws SQLException {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
        connection = getConnection();
        preparedStatement = connection.prepareStatement(sql);

        // Verificar si hay parámetros antes de establecerlos en la consulta preparada
        if (params != null && params.length > 0) {
            // Establecer los parámetros en la consulta preparada
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        }

        preparedStatement.execute();
        resultSet = preparedStatement.getResultSet();
    } finally {
        closeResources(connection, preparedStatement);
    }

    return resultSet;
}
    
      protected int ejecutarUpdateDeleteInsert(String sql, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int filasAfectadas = 0;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);

            // Establecer los parámetros en la consulta preparada
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            filasAfectadas = preparedStatement.executeUpdate();
        } finally {
            closeResources(connection, preparedStatement);
        }

        return filasAfectadas;
    }
    
    
    
    
   
  private void closeResources(Connection connection, Statement statement) {
    try {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        // Manejar la excepción de manera adecuada, como lanzar una excepción personalizada o registrarla.
    }
}
    
}

   