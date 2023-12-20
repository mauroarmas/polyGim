/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import View.PagoJF;
import static controlador.controladorDatos.pedidosRepositorio;
import static controlador.controladorDatos.clientesRepositorio;
import static controlador.controladorNavegacion.pedidojf;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Db.PedidoDb;
import modelo.Detalle;
import modelo.Membresia;
import modelo.Pedido;
import modelo.Servicio;
import static controlador.controladorDatos.servicioRepositorio;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import modelo.Pago;

/**
 *
 * @author mercadito
 */
public class controladorPedido {

    static PedidoDb pedidoQuery = new PedidoDb();
    

    public static void estadoInicialPedido() throws SQLException {
         pedidojf.setVisible(true);
        pedidojf.btnActualizarPedido.setEnabled(false);
        pedidojf.btnAñadirPago.setEnabled(false);
        pedidojf.btnModificarPedidoTabla.setEnabled(false);
        pedidojf.btnVerDetalle.setEnabled(false);
        pedidojf.btnBuscarPedido.setEnabled(true);

        pedidojf.comoServicio.setEnabled(false);
        cargarTatable();
        cargarServicio();
        pedidojf.tablaPedido.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                pedidojf.btnAñadirPago.setEnabled(true);
                pedidojf.btnModificarPedidoTabla.setEnabled(true);
                pedidojf.btnVerDetalle.setEnabled(true);
            }

        });

        pedidojf.getContentPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pedidojf.tablaPedido.clearSelection();
                pedidojf.btnAñadirPago.setEnabled(false);
                pedidojf.btnModificarPedidoTabla.setEnabled(false);
                pedidojf.btnVerDetalle.setEnabled(false);
            }
        });
         
        pedidojf.txtDniBuscarPedido.getDocument().addDocumentListener(new DocumentListener(){
             @Override
             public void insertUpdate(DocumentEvent e) {
                 System.out.println("aa");
             }

             @Override
             public void removeUpdate(DocumentEvent e) {
               System.out.println("bb");
             }

             @Override
             public void changedUpdate(DocumentEvent e) {
                System.out.println("cc");
             }
        
        
        
        
        });
        
        
        
    }

    ///***********FUNCIONES PARA EL EVENTO DE LOS BOTONES******/////////////
    ///**********FUNCION PARA EL REGISTRO DE PEDIDOS**********////
    public static void registrarPedido() {

        try {

            String dni = pedidojf.txtDniPedido.getText();
            boolean checkDni = chequearDni(dni);
            boolean checkCLiente = chequearCliente(Integer.parseInt(dni));

            if (checkDni == false) {

                JOptionPane.showMessageDialog(null, "El formato del dni no es valido", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                pedidojf.txtDniPedido.setText("");
                return;
            }

            if (checkCLiente == false) {

                JOptionPane.showMessageDialog(null, "El cliente no esta registrado en el sistema", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                pedidojf.txtDniPedido.setText("");
                return;
            }
            String servicio = (String) pedidojf.comoServicio.getSelectedItem();
            String nombreMembresia = (String) pedidojf.comboTiempoPedido.getSelectedItem();
            String cantidadMem = (String) pedidojf.comboCantidad.getSelectedItem();

            if (nombreMembresia.equals("...") || (cantidadMem.isEmpty() || cantidadMem.equals("..."))) {
                JOptionPane.showMessageDialog(null, "Seleccione adecuadamente los datos de la memebresia", "Adevertencia", JOptionPane.WARNING_MESSAGE);
              
                return;
            }

            Membresia mem = buscarMembresia(servicio, nombreMembresia);
            int indexCliente = buscarCliente(Integer.parseInt(dni));
            LocalDate fechaI = LocalDate.now();
            Pedido p = new Pedido();

            p.setFechaPedido(fechaI);
            p.obtenerFechaVen(fechaI, nombreMembresia, cantidadMem);

            p.setCliente(clientesRepositorio.get(indexCliente));
            p.agregarDetalle(Integer.parseInt(cantidadMem), mem);
            p.calcularSumaSubtotales();
            clientesRepositorio.get(indexCliente).addPedido(p); 
            int idPedido = pedidoQuery.ingresarPedido(p.getFechaPedido(), p.getEstado(), p.getFechaVencimiento(), p.getCliente().getDni(), p.getTotal());
            if (idPedido == -1) {
                JOptionPane.showMessageDialog(null, "Hubo Problemas en la db", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                p.setIdPedido(idPedido);
                pedidosRepositorio.add(p);
            }
            ArrayList<Detalle> d = p.getDetalles();
            for (int i = 0; i < d.size(); i++) {
                int idDetalle = -1;
                idDetalle = pedidoQuery.ingresarDetalle(idPedido, d.get(i).getCantidad(), d.get(i).getMem().getIdMembresia(), d.get(i).getSubtotal());
                if (idDetalle == -1) {
                    JOptionPane.showMessageDialog(null, "Hubo Problemas en la db", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                p.agregarIdDetalle(i, idDetalle);
            }

            Pago pago = controladorPago.iniciarPago(idPedido);
            System.out.println(pago.getIdPago());
            if (pago.getIdPago() == null) {
               
                pedidosRepositorio.add(p);
                System.out.println("AAAAAAAAAAA");
            } else {
                p.agregarPago(pago.getIdPago(), pago.getMonto());
                pedidosRepositorio.add(p);
                 System.out.println("BBBBBBBB");
            }

            cargarTatable();

        } catch (Exception e) {
            System.out.println(e);
        }

    }
    ///***************************************************************************////

    ////******FUNCION PARA AÑADIR EL PAGO*******************/////////////////////////
    public static void añadirPago() throws SQLException {
        Object[] ped = getSelectRowData();
        int idPedido = (int) ped[0];
        System.out.println(idPedido + "idPedido");
        for(int i=0;i<5;i++){
        Pago pago = controladorPago.iniciarPago(idPedido);
        System.out.println(pago.getIdPago());
        if(pago.getIdPago() == null){
             
             pedidojf.tablaPedido.clearSelection();
             
        }else{
            int index = buscarPedidoPoridPedido(idPedido);
             System.out.println(index + "indice de pedido");
             pedidosRepositorio.get(index).agregarPago(pago.getIdPago(), pago.getMonto());
          pedidojf.tablaPedido.clearSelection();
        }
        }
        
        
        
        
    }

    ///***************************************************************************////
    ////******FUNCION PARA ACTUALIZAR TABLA*******************/////////////////////////
    
    public static void actualizarTabla() {
    }
   
    ///***************************************************************************////
    ////******FUNCION PARA BUSCAR PEDIDO POR DNI DEL CLIENTE*******************/////////////////////////
    public static void buscarPedidoPorDni() {
        
        
    }
    ///***************************************************************************////

    ////******FUNCION PARA VER DETALLE DE UN PEDIDO*******************/////////////////////////
    public static void verDetallePedido() {
    }
    ///***************************************************************************////

    ////******FUNCION PARA BOTON MODIFICAR*******************/////////////////////////
    public static void btnModificar() {
    }
    ///***************************************************************************////

    ////******FUNCION PARA ACTUALIZAR UN PEDIDO*******************/////////////////////////
    public static void actualizarPedido() {
    }
    ///***************************************************************************////

    ////******FUNCION AUXILIARES*******************/////////////////////////
    public static void combobox() {
        //FUNCION PARA EL MANEJ0 DE EVENTOS DE LOS COMBOX BOX DE TIMEPO DE PEDIDO Y CANTIDAD
        pedidojf.comboCantidad.removeAllItems();

        String nombreMembresia = (String) pedidojf.comboTiempoPedido.getSelectedItem();

        switch (nombreMembresia) {
            case "Mensual":
                pedidojf.comboCantidad.addItem("...");
                for (int i = 0; i < 12; i++) {

                    pedidojf.comboCantidad.addItem(String.valueOf(i + 1));
                }
                break;
            case "Dos semanas":
                pedidojf.comboCantidad.addItem("...");
                pedidojf.comboCantidad.addItem("1");
                break;
            case "...":
                pedidojf.comboCantidad.addItem("");
                break;

        }

    }

    public static void cargarServicio() {
        pedidojf.comoServicio.removeAllItems();
        for (int i = 0; i < servicioRepositorio.size(); i++) {
            pedidojf.comoServicio.addItem(servicioRepositorio.get(i).getServicio());
        }
        ///FUNCION PARA CARGAR LOS SERVICIOS EN EL COMBO BOX
    }

    ////******FIN DE FUNCION AUXILIARES*******************/////////////////////////
    ///***********FIN FUNCIONES PARA EL EVENTO DE LOS BOTONES******/////////////
    ////*****************FUNCIONES ENCAPSULADAS******************************///////////
    private static boolean chequearCliente(int dni) {

        for (Cliente cliente : clientesRepositorio) {

            if (cliente.getDni() == dni) {
                
                return true;
            }

        }

       
        return false;
    }

    private static Membresia buscarMembresia(String nombreServicio, String nombreMembresia) {
        int indiceSer = buscarServicioPorString(nombreServicio);
        ArrayList<Membresia> memAux = servicioRepositorio.get(indiceSer).getMembresias();
        for (int i = 0; i < memAux.size(); i++) {

            if (memAux.get(i).getMembresia().equals(nombreMembresia)) {

                Membresia a = new Membresia(memAux.get(i).getIdMembresia(), memAux.get(i).getMembresia(), memAux.get(i).getPrecio());
                return a;
            }

        }

        return null;
    }

    private static int buscarServicioPorString(String nombreServicio) {

        for (int i = 0; i < servicioRepositorio.size(); i++) {
            if (servicioRepositorio.get(i).getServicio().equals(nombreServicio)) {
                return i; // Found the servicio, return its index
            }
        }
        return -1; // Servicio not found

    }

    private static boolean chequearDni(String dni) {

        if (dni.length() <= 7 || dni.length() >= 9) {

            return false;
        }

        for (int i = 0; i < 8; i++) {
            if (!Character.isDigit(dni.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static int buscarCliente(int dni) {

        for (int i = 0; i < clientesRepositorio.size(); i++) {
            if (clientesRepositorio.get(i).getDni() == dni) {
                return i;
            }
        }
        return -1;

    }

    public static void cargarTatable() throws SQLException {
        DefaultTableModel pedidoTabla = new DefaultTableModel();
        pedidoTabla.addColumn("nroPedido");
        pedidoTabla.addColumn("dniCliente");
        pedidoTabla.addColumn("fechaPedido");
        pedidoTabla.addColumn("fechaVencimiento");
        pedidoTabla.addColumn("estado");
        pedidoTabla.addColumn("total");
        pedidoTabla.setRowCount(0);
        ResultSet res = pedidoQuery.selectPedido();

        while (res.next()) {
            if (res.getInt("estado") == 0) {

                Object[] datosFila = {res.getInt("idPedido"), res.getInt("clienteDni"), res.getDate("fecha_pedido"), res.getDate("fecha_vencimiento"), "IMPAGO", res.getDouble("total")};
                pedidoTabla.addRow(datosFila);

            } else {
                Object[] datosFila = {res.getInt("idPedido"), res.getInt("clienteDni"), res.getDate("fecha_pedido"), res.getDate("fecha_vencimiento"), "PAGADO", res.getDouble("total")};
                pedidoTabla.addRow(datosFila);
            }

        }
        pedidojf.tablaPedido.setModel(pedidoTabla);

    }

    private static Object[] getSelectRowData() {
        int selectedRow = pedidojf.tablaPedido.getSelectedRow();
        if (selectedRow != -1) {
            int columnCount = pedidojf.tablaPedido.getColumnCount();
            Object[] rowData = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = pedidojf.tablaPedido.getValueAt(selectedRow, i);
            }
            return rowData;
        }

        return null;
    }

    private static int buscarPedidoPoridPedido(int idPedido) {

      for (int i = 0; i < pedidosRepositorio.size(); i++) {
            if (pedidosRepositorio.get(i).getIdPedido() == idPedido) {
                return i;
            }
        }
        return -1;

    }

    ////**********************************************///////////
}
