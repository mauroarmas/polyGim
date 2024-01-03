/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static controlador.controladorDatos.pedidosRepositorio;
import static controlador.controladorDatos.clientesRepositorio;
import static controlador.controladorNavegacion.pedidojf;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.FocusListener;
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
        pedidojf.comoServicio.setEnabled(false);
        cargarTatable();
        cargarServicio();
        pedidojf.tablaPedido.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                pedidojf.btnAñadirPago.setEnabled(true);
                pedidojf.btnModificarPedidoTabla.setEnabled(true);

            }

        });

        pedidojf.getContentPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pedidojf.tablaPedido.clearSelection();
                pedidojf.btnAñadirPago.setEnabled(false);
                pedidojf.btnModificarPedidoTabla.setEnabled(false);

                // pedidojf.txtDniBuscarPedido.requestFocusInWindow();
            }
        });

        pedidojf.txtDniBuscarPedido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                manejadorBusquedaAutomatica();

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                manejadorBusquedaAutomatica();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                manejadorBusquedaAutomatica();

            }

        });

        pedidojf.txtDniPedido.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Acción cuando el JTextField obtiene el foco

            }

            @Override
            public void focusLost(FocusEvent e) {
                String dniText = pedidojf.txtDniPedido.getText();
                if (dniText.isEmpty()) {

                } else {
                    int dni = Integer.parseInt(dniText);
                    boolean checkDni = chequearDni(dniText);
                    boolean checkCLiente = chequearCliente(Integer.parseInt(dniText));

                    if (checkDni == false) {

                        JOptionPane.showMessageDialog(null, "El formato del dni no es valido", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                        pedidojf.txtDniPedido.setText("");
                        return;
                    }

                    if (checkCLiente == false) {

                        JOptionPane.showMessageDialog(null, "El cliente no esta registrado en el sistema", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                        pedidojf.txtDniPedido.setText("");
                        return;
                    } else {
                        try {
                            ResultSet res = pedidoQuery.getClienteForPedido(dni);
                            if (res.next() == false) {

                            } else {
                                JOptionPane.showMessageDialog(null, res.getString("apellido") + ", " + res.getString("nombre"), "Cliente", JOptionPane.INFORMATION_MESSAGE);
                            }

                        } catch (SQLException ex) {
                            Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }

            }
        });

    }

    ///***********FUNCIONES PARA EL EVENTO DE LOS BOTONES******/////////////
    ///**********FUNCION PARA EL REGISTRO DE PEDIDOS**********////
    public static void registrarPedido() {

        try {

            String dni = pedidojf.txtDniPedido.getText();
            /*
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
             */
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
            pedidosRepositorio.add(p);
            controladorPago.iniciarPago(idPedido);
            pedidojf.txtDniPedido.setText("");
            combobox();
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
        controladorPago.iniciarPago(idPedido);
        pedidojf.tablaPedido.clearSelection();
    }

    ////******FUNCION PARA BOTON MODIFICAR*******************/////////////////////////
    public static void btnModificar() {
        pedidojf.btnActualizarPedido.setEnabled(true);
        pedidojf.btnRegistrarPedido.setEnabled(false);
        pedidojf.txtDniPedido.setEnabled(false);
        Object[] ped = getSelectRowData();
        int idPedido = (int) ped[0];
        int dni = (int) ped[1];
        pedidojf.txtDniPedido.setText(String.valueOf(dni));
        int index = buscarPedidoPoridPedido(idPedido);
        ArrayList<Detalle> detM = pedidosRepositorio.get(index).getDetalles();

        for (int i = 0; i < pedidojf.comboTiempoPedido.getItemCount(); i++) {
            if (pedidojf.comboTiempoPedido.getItemAt(i).equals(detM.get(0).getMem().getMembresia())) {
                pedidojf.comboTiempoPedido.setSelectedIndex(i);
                // Salir del bucle una vez que se encuentra y selecciona el ítem
                break;
            }
        }

        for (int j = 0; j < pedidojf.comboCantidad.getItemCount(); j++) {
            if (pedidojf.comboCantidad.getItemAt(j).equals(String.valueOf(detM.get(0).getCantidad()))) {
                pedidojf.comboCantidad.setSelectedIndex(j);
                break;
            }

        }
            actualizarPedido( index, 0,idPedido);
    }
    ///***************************************************************************////

    ////******FUNCION PARA ACTUALIZAR UN PEDIDO*******************/////////////////////////
    public static void actualizarPedido(int indexPedido, int indexDetalle,int idPedido) {

        for (ActionListener al : pedidojf.btnActualizarPedido.getActionListeners()) {
            pedidojf.btnActualizarPedido.removeActionListener(al);
        }
        pedidojf.btnActualizarPedido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String servicio = (String) pedidojf.comoServicio.getSelectedItem();
                String nombreMembresia = (String) pedidojf.comboTiempoPedido.getSelectedItem();
                String cantidadMem = (String) pedidojf.comboCantidad.getSelectedItem();

                if (nombreMembresia.equals("...") || (cantidadMem.isEmpty() || cantidadMem.equals("..."))) {
                    JOptionPane.showMessageDialog(null, "Seleccione adecuadamente los datos de la memebresia", "Adevertencia", JOptionPane.WARNING_MESSAGE);

                    return;
                }

                Membresia mem = buscarMembresia(servicio, nombreMembresia);

                LocalDate fechaI = LocalDate.now();

                pedidosRepositorio.get(indexPedido).setFechaPedido(fechaI);

                pedidosRepositorio.get(indexPedido).obtenerFechaVen(fechaI, nombreMembresia, cantidadMem);

                pedidosRepositorio.get(indexPedido).modificarArrayDetalle(indexDetalle, Integer.parseInt(cantidadMem), mem);
                pedidosRepositorio.get(indexPedido).calcularSumaSubtotales();
                ArrayList<Pago> pagoP = pedidosRepositorio.get(indexPedido).getPagos();
                ArrayList<Detalle> detP = pedidosRepositorio.get(indexPedido).getDetalles();
                if(pagoP.isEmpty()){
                    try {
                        int filasAfectadasPedido=pedidoQuery.updatePedido(idPedido,
                                pedidosRepositorio.get(indexPedido).getFechaPedido() ,
                                pedidosRepositorio.get(indexPedido).getFechaVencimiento(),
                                0,
                                pedidosRepositorio.get(indexPedido).getTotal());
                    int[] filasAfectadasDetalle = new int[detP.size()];
                    for(int l=0;l<detP.size();l++){
                     filasAfectadasDetalle[l]=pedidoQuery.updateDetalle(detP.get(l).getIdDetalle(),
                             idPedido,
                             detP.get(l).getCantidad(),
                             detP.get(l).getMem().getIdMembresia(),
                             detP.get(l).getSubtotal());
                    }
                        pedidojf.btnActualizarPedido.setEnabled(false);
                        pedidojf.btnRegistrarPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setText("");
                          pedidojf.comboTiempoPedido.setSelectedIndex(0);
                         
                       cargarTatable();
                            
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    
                    
                }else{
                double montoTotal=0;
                for(int k=0;k<pagoP.size();k++){
                 montoTotal = montoTotal + pagoP.get(k).getMonto();
                }
                
                if(montoTotal>pedidosRepositorio.get(indexPedido).getTotal()){
                    
                      try {
                        int filasAfectadasPedido=pedidoQuery.updatePedido(idPedido,
                                pedidosRepositorio.get(indexPedido).getFechaPedido() ,
                                pedidosRepositorio.get(indexPedido).getFechaVencimiento(),
                                1,
                                pedidosRepositorio.get(indexPedido).getTotal());
                    int[] filasAfectadasDetalle = new int[detP.size()];
                    for(int l=0;l<detP.size();l++){
                     filasAfectadasDetalle[l]=pedidoQuery.updateDetalle(detP.get(l).getIdDetalle(),
                             idPedido,
                             detP.get(l).getCantidad(),
                             detP.get(l).getMem().getIdMembresia(),
                             detP.get(l).getSubtotal());
                    }
                        pedidojf.btnActualizarPedido.setEnabled(false);
                        pedidojf.btnRegistrarPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setText("");
                          pedidojf.comboTiempoPedido.setSelectedIndex(0);
                         
                       cargarTatable();
                            
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    
                    pedidosRepositorio.get(indexPedido).getPagos().clear();
                    int filasAfectadasDeleteP = 0;
                    try {
                        filasAfectadasDeleteP = pedidoQuery.deletePago(pedidosRepositorio.get(indexPedido).getIdPedido());
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int idGenerado = 0;
                    try {
                        idGenerado = pedidoQuery.insertPago(montoTotal, pedidosRepositorio.get(indexPedido).getIdPedido());
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                     if(idGenerado == -1 || filasAfectadasDeleteP==0){
                         JOptionPane.showMessageDialog(null, "HUBO UN ERROR CON LA BASE DE DATOS", "ERROR", JOptionPane.WARNING_MESSAGE);

                          return;
                     
                     }
                    
                    
                     double vuelto = montoTotal - pedidosRepositorio.get(indexPedido).getTotal();
                     
                         JOptionPane.showMessageDialog(null, "el vuelto del cliente es "+vuelto, "INFORMACION", JOptionPane.WARNING_MESSAGE);
                       
                    
                    
                }else if(montoTotal<pedidosRepositorio.get(indexPedido).getTotal()){
                     try {
                        int filasAfectadasPedido=pedidoQuery.updatePedido(idPedido,
                                pedidosRepositorio.get(indexPedido).getFechaPedido() ,
                                pedidosRepositorio.get(indexPedido).getFechaVencimiento(),
                                0,
                                pedidosRepositorio.get(indexPedido).getTotal());
                    int[] filasAfectadasDetalle = new int[detP.size()];
                    for(int l=0;l<detP.size();l++){
                     filasAfectadasDetalle[l]=pedidoQuery.updateDetalle(detP.get(l).getIdDetalle(),
                             idPedido,
                             detP.get(l).getCantidad(),
                             detP.get(l).getMem().getIdMembresia(),
                             detP.get(l).getSubtotal());
                    }
                        pedidojf.btnActualizarPedido.setEnabled(false);
                        pedidojf.btnRegistrarPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setText("");
                          pedidojf.comboTiempoPedido.setSelectedIndex(0);
                         
                       cargarTatable();
                            
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        controladorPago.iniciarPago(pedidosRepositorio.get(indexPedido).getIdPedido());
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                }else{
                     try {
                        int filasAfectadasPedido=pedidoQuery.updatePedido(idPedido,
                                pedidosRepositorio.get(indexPedido).getFechaPedido() ,
                                pedidosRepositorio.get(indexPedido).getFechaVencimiento(),
                                1,
                                pedidosRepositorio.get(indexPedido).getTotal());
                    int[] filasAfectadasDetalle = new int[detP.size()];
                    for(int l=0;l<detP.size();l++){
                     filasAfectadasDetalle[l]=pedidoQuery.updateDetalle(detP.get(l).getIdDetalle(),
                             idPedido,
                             detP.get(l).getCantidad(),
                             detP.get(l).getMem().getIdMembresia(),
                             detP.get(l).getSubtotal());
                    }
                        pedidojf.btnActualizarPedido.setEnabled(false);
                        pedidojf.btnRegistrarPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setText("");
                          pedidojf.comboTiempoPedido.setSelectedIndex(0);
                         
                       cargarTatable();
                            
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        controladorPago.iniciarPago(pedidosRepositorio.get(indexPedido).getIdPedido());
                    } catch (SQLException ex) {
                        Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                }
                
                }
                
                  pedidojf.btnActualizarPedido.setEnabled(false);
                        pedidojf.btnRegistrarPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setEnabled(true);
                         pedidojf.txtDniPedido.setText("");

            }
                    
        });

    }
    ///***************************************************************************////

    ///////////////FUNCIONES VALIDADORAS******************///////////
    private static boolean chequearCliente(int dni) {

        for (Cliente cliente : clientesRepositorio) {

            if (cliente.getDni() == dni) {

                return true;
            }

        }

        return false;
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

    ////////////////////////////////////7///////
    ///***********FUNCIONES DE BUSQUEDA******////////
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

    private static int buscarCliente(int dni) {

        for (int i = 0; i < clientesRepositorio.size(); i++) {
            if (clientesRepositorio.get(i).getDni() == dni) {
                return i;
            }
        }
        return -1;

    }

    public static void buscarPedidoPorDni(ResultSet res) throws SQLException {
        DefaultTableModel pedidoTabla = new DefaultTableModel();
        pedidoTabla.addColumn("nroPedido");
        pedidoTabla.addColumn("dniCliente");
        pedidoTabla.addColumn("fechaPedido");
        pedidoTabla.addColumn("fechaVencimiento");
        pedidoTabla.addColumn("estado");
        pedidoTabla.addColumn("total");
        pedidoTabla.setRowCount(0);
        //  ResultSet res = pedidoQuery.selectPedido();

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

    public static int buscarPedidoPoridPedido(int idPedido) {

        for (int i = 0; i < pedidosRepositorio.size(); i++) {
            if (pedidosRepositorio.get(i).getIdPedido() == idPedido) {
                return i;
            }
        }
        return -1;

    }
    //////////////////////////////    

    ///***********FUNCIONES PARA EL COMBOBOX******////////
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
            case "Semanal":
                pedidojf.comboCantidad.addItem("...");
                pedidojf.comboCantidad.addItem("1");
                pedidojf.comboCantidad.addItem("2");
                pedidojf.comboCantidad.addItem("3");

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

    }
    //////////////////////////////    
    ///***********FUNCIONES PARALA TABLA******////////

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

    private static void manejadorBusquedaAutomatica() {
        String dniText = pedidojf.txtDniBuscarPedido.getText();
        if (dniText.isEmpty()) {
            try {
                cargarTatable();
            } catch (SQLException ex) {
                Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            int dni = Integer.parseInt(dniText);

            try {
                ResultSet res = pedidoQuery.getPedidoByDni(dni);
                buscarPedidoPorDni(res);
            } catch (SQLException ex) {
                Logger.getLogger(controladorPedido.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
