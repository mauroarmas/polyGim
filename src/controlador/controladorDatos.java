/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;




import static controlador.controladorCliente.queryCliente;
import static controlador.controladorPedido.pedidoQuery;
import static controlador.controladorServicio.queryServicio;
import static controlador.controladorPago.queryPago;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;
import modelo.Db.DireccionDb;
import modelo.Db.LocalidadDb;

import modelo.Direccion;
import modelo.Localidad;
import modelo.Membresia;
import modelo.Pedido;
import modelo.Servicio;

/**
 *
 * @author mercadito
 */
public class controladorDatos {

    static ArrayList<Localidad> localidadRepositorio = new ArrayList<>();
    static ArrayList<Direccion> direccionRepositorio = new ArrayList<>();
    static ArrayList<Servicio> servicioRepositorio = new ArrayList<>();
    static ArrayList<Cliente> clientesRepositorio = new ArrayList<>();
    static ArrayList<Pedido> pedidosRepositorio = new ArrayList<>();
    static LocalidadDb localidadquery = new LocalidadDb();
    static DireccionDb direccionquery = new DireccionDb();

    public static void loadArrayList() throws SQLException {
        loadArrayLocalidadDireccion();
        loadArrayServicios();
        loadArrayCliente();
        loadArrayPedido();
        loadDetallesAndPagos();
        loadPedidosInCLiente();

    }

    private static void loadArrayServicios() throws SQLException {

        ResultSet resS = queryServicio.selectServicio();
        while (resS.next()) {

            servicioRepositorio.add(new Servicio(resS.getInt("idServicio"),
                    resS.getString("servicio")));

        }

        ResultSet resM = queryServicio.selectMembresia();
        while (resM.next()) {
            int idServico = getServiciById(resM.getInt("idServicio"));

            servicioRepositorio.get(idServico).agregarMembresias(resM.getInt("idMembresia"),
                    resM.getString("membresia"),
                    resM.getDouble("precio"));

        }

    }

    private static int getServiciById(int idServicio) {

        for (int i = 0; i < servicioRepositorio.size(); i++) {
            if (servicioRepositorio.get(i).getIdServicio() == idServicio) {
                return i; // Found the servicio, return its index
            }
        }
        return -1; // Servicio not found

    }

    private static void loadArrayLocalidadDireccion() throws SQLException {

        ResultSet resL = localidadquery.getAllLocalidad();

        while (resL.next()) {
            localidadRepositorio.add(new Localidad(resL.getInt("idLocalidad"),
                    resL.getString("localidad")));

        }

        ResultSet resD = direccionquery.getAllDireccion();
        while (resD.next()) {
            int index = getLocalidadById(resD.getInt("idLocalidad"));
            direccionRepositorio.add(new Direccion(resD.getInt("idDireccion"), resD.getString("calle"), resD.getInt("altura"), localidadRepositorio.get(index)));
        }

    }

    private static int getLocalidadById(int id) {
        for (int i = 0; i < localidadRepositorio.size(); i++) {
            if (localidadRepositorio.get(i).getIdLocalidad() == id) {
                return i;
            }
        }
        return -1;

    }

    private static void loadArrayCliente() throws SQLException {
        ResultSet resC = queryCliente.getAllCliente();
        while (resC.next()) {
            int indexDireccion = getDireccionById(resC.getInt("idDireccion"));
            clientesRepositorio.add(new Cliente(resC.getInt("dni"),
                    resC.getString("nombre"),
                    resC.getString("apellido"),
                    resC.getDate("fecha_ingreso").toLocalDate(),
                    resC.getString("telefono"),
                    resC.getString("como_llego"),
                    direccionRepositorio.get(indexDireccion)
            ));

        }
    }

    private static int getDireccionById(int id) {

        for (int i = 0; i < direccionRepositorio.size(); i++) {
            if (direccionRepositorio.get(i).getIdDireccion() == id) {
                return i;
            }
        }
        return -1;

    }

    private static void loadArrayPedido() throws SQLException {
        ResultSet resP = pedidoQuery.selectPedido();
        while (resP.next()) {
            int index = getClienteByDni(resP.getInt("clienteDni"));
            if (resP.getInt("estado") == 0) {
                pedidosRepositorio.add(new Pedido(resP.getInt("idPedido"),
                        resP.getDate("fecha_pedido").toLocalDate(),
                        resP.getDate("fecha_vencimiento").toLocalDate(),
                        false,
                        clientesRepositorio.get(index),
                        resP.getDouble("total")
                ));

            } else {

                pedidosRepositorio.add(new Pedido(resP.getInt("idPedido"),
                        resP.getDate("fecha_pedido").toLocalDate(),
                        resP.getDate("fecha_vencimiento").toLocalDate(),
                        true,
                        clientesRepositorio.get(index),
                        resP.getDouble("total")
                ));

            }
        }

    }

    private static int getClienteByDni(int dni) {
        for (int i = 0; i < clientesRepositorio.size(); i++) {
            if (clientesRepositorio.get(i).getDni() == dni) {
                return i;
            }
        }
        return -1;

    }

    private static void loadDetallesAndPagos() throws SQLException {
        ResultSet resD = pedidoQuery.selectDetalle();
        while (resD.next()) {
            Membresia mem = getMembresiaById(resD.getInt("idMembresia"));
            int index = getPedidoById(resD.getInt("idPedido"));
            pedidosRepositorio.get(index).agregarDetalles(resD.getInt("idDetalle"),
                    resD.getInt("cantidad"),
                    mem);

        }

        ResultSet resP = queryPago.traerPagos();
        while (resP.next()) {
            int index = getPedidoById(resP.getInt("idPedido"));
            pedidosRepositorio.get(index).agregarPago(resP.getInt("idPago"), resP.getDouble("montoPago"));
        }

    }

    private static Membresia getMembresiaById(int idMembresia) {

        for (int i = 0; i < servicioRepositorio.size(); i++) {
            ArrayList<Membresia> mem = servicioRepositorio.get(i).getMembresias();
            for (int j = 0; j < mem.size(); j++) {
                if (mem.get(j).getIdMembresia() == idMembresia) {
                    return mem.get(j);
                }
            }

        }
        return null;
    }

    private static int getPedidoById(int idPedido) {

        for (int i = 0; i < pedidosRepositorio.size(); i++) {
            if (pedidosRepositorio.get(i).getIdPedido() == idPedido) {
                return i;
            }
        }
        return -1;
    }

    private static void loadPedidosInCLiente() {
        for (int i = 0; i < pedidosRepositorio.size(); i++) {
            int dni = pedidosRepositorio.get(i).getCliente().getDni();
            int index = getClienteByDni(dni);
            clientesRepositorio.get(index).addPedido(pedidosRepositorio.get(i));
        }

    }

}
