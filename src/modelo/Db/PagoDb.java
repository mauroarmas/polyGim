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
public class PagoDb extends Db {

    public ResultSet traerDatosDePago(int idPedido) throws SQLException {
        String sql = "SELECT c.dni ,s.servicio , m.membresia ,p.fecha_pedido ,p.fecha_vencimiento ,d.cantidad ,p.total "
                + "	FROM Cliente AS c ,Pedido AS p ,Detalle AS d ,Membresia AS m ,Servicio AS s"
                + "		WHERE c.dni = p.clienteDni  "
                + "			AND p.idPedido =d.idPedido  "
                + "				AND d.idMembresia =m.idMembresia"
                + "					AND m.idServicio =s.idServicio 	"
                + "						AND p.idPedido =?";

        ResultSet res = ejecutarSelect(sql, idPedido);

        return res;

    }

    public int registrarPago(double monto, int idPedido) throws SQLException {
        String sql = "INSERT INTO Pago (montoPago,idPedido) VALUES (?,?)";
        int idGenerado = ejecutarInsertConDevolucionDeClavePrimariaGenerada(sql, monto, idPedido);

        return idGenerado;
    }

    public ResultSet traerSumaPagos(int idPedido) throws SQLException {
        String sql = "SELECT SUM(montoPago) AS SumaMontos FROM Pago AS p WHERE p.idPedido=? GROUP BY idPedido";

        ResultSet res = ejecutarSelect(sql, idPedido);
        return res;
    }
   public ResultSet traerPagos() throws SQLException{
   String sql = "SELECT * FROM Pago";
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
