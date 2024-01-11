/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import View.ClienteJF;
import java.util.ArrayList;
import modelo.Cliente;
import static controlador.controladorDatos.clientesRepositorio;
import static controlador.controladorDatos.localidadRepositorio;
import static controlador.controladorDatos.comoLlegoRepositorio;
import static controlador.controladorNavegacion.clientejf;
import static controlador.controladorNavegacion.pedidojf;
import static controlador.controladorPedido.pedidoQuery;
import java.sql.ResultSet;
import java.sql.SQLException;



import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Db.ClienteDb;
import modelo.Db.DireccionDb;
import modelo.Db.LocalidadDb;
import modelo.Direccion;
import modelo.Localidad;

/**
 *
 * @author mercadito
 */
public class controladorCliente {
   
   static ClienteDb queryCliente = new ClienteDb();
   static DireccionDb queryDireccion = new DireccionDb();
   static LocalidadDb queryLocalidad = new LocalidadDb(); 
   
   public static void estadoInicialCliente() throws SQLException{
        clientejf.setVisible(true);
        setDefault();
        cargarTable();
        cargarCombos();
   
   }
   public static void setDefault(){
        clientejf.txtAltura.setEnabled(false);
        clientejf.txtCalle.setEnabled(false);
        clientejf.txtNombre.setEnabled(false);
        clientejf.txtApellido.setEnabled(false);
        clientejf.txtTelefonoArea.setEnabled(false);
        clientejf.txtTelefono.setEnabled(false);
        clientejf.txtDni.setEnabled(false);
        clientejf.comboLocalidad.setEnabled(false);
        clientejf.comboComoLlego.setEnabled(false);
        clientejf.btnNuevaLocalidad.setEnabled(false);
        clientejf.buscadorCliente.setEnabled(true);

        clientejf.btnModificar.setEnabled(false);
        clientejf.btnHistorial.setEnabled(false);

        clientejf.btnNuevo.setVisible(true);
        clientejf.btnCancelar.setVisible(false);

        clientejf.btnActualizar.setVisible(false);
        clientejf.btnRegistrar.setVisible(false);
       
   }
   public static void nuevoCliente(){
        clientejf.txtAltura.setEnabled(true);
        clientejf.txtCalle.setEnabled(true);
        clientejf.txtNombre.setEnabled(true);
        clientejf.txtApellido.setEnabled(true);
        clientejf.txtTelefonoArea.setEnabled(true);
        clientejf.txtTelefono.setEnabled(true);
        clientejf.txtDni.setEnabled(true);
        clientejf.comboLocalidad.setEnabled(true);
        clientejf.comboComoLlego.setEnabled(true);
        clientejf.btnNuevaLocalidad.setEnabled(true);
        clientejf.buscadorCliente.setEnabled(false);

        clientejf.btnRegistrar.setVisible(true);
        clientejf.btnNuevo.setVisible(false);
        clientejf.btnCancelar.setVisible(true);
       
   }
    public static void cargarTable() throws SQLException {
        DefaultTableModel clienteTabla = new DefaultTableModel();
        
        String direccionCLiente ="";
        String localidadCLiente ="";
        clienteTabla.addColumn("DNI");
        //clienteTabla.addColumn("APELLIDO");
        clienteTabla.addColumn("APELLIDO Y NOMBRE");
        clienteTabla.addColumn("TELEFONO");
        clienteTabla.addColumn("FECHA DE INGRESO");
        clienteTabla.addColumn("LOCALIDAD");
        clienteTabla.addColumn("DIRECCIÓN");
        clienteTabla.addColumn("COMO LLEGÓ");
        clienteTabla.setRowCount(0);
        ResultSet resCliente = queryCliente.getAllCliente();

        while (resCliente.next()) {

            ResultSet resDireccion = queryDireccion.ejecutarSelect("SELECT * FROM direccion WHERE idDireccion=?",resCliente.getString("idDireccion"));

            if(resDireccion.next()){

                direccionCLiente = resDireccion.getString("calle") + " " + resDireccion.getString("altura");

                ResultSet resLocalidad = queryLocalidad.ejecutarSelect("SELECT * FROM localidad WHERE idLocalidad=?",resDireccion.getString("idLocalidad"));
                if(resLocalidad.next()){
                    localidadCLiente = resLocalidad.getString("localidad");                          
                }
                
            }
            
            Object[] datosFila = {
                resCliente.getInt("dni"), 
                resCliente.getString("nombre")+ " "+ resCliente.getString("apellido"), 
                resCliente.getString("telefono"), 
                resCliente.getDate("fecha_ingreso"), 
                localidadCLiente,
                direccionCLiente,
                resCliente.getString("como_llego") 
            };


            clienteTabla.addRow(datosFila);

            
        }
        clientejf.tablaClientes.setModel(clienteTabla);

    }

    public static void registrarCliente() throws SQLException {
        int alturaCliente = Integer.parseInt(clientejf.txtAltura.getText());
        String calleCliente = clientejf.txtCalle.getText();
        String nombreCliente = clientejf.txtNombre.getText();
        String apellidoCliente = clientejf.txtApellido.getText();
        String telefonoCliente = clientejf.txtTelefonoArea.getText() + "-" + clientejf.txtTelefono.getText();
        String dniCliente = clientejf.txtDni.getText();
        String localidadCliente = clientejf.comboLocalidad.getSelectedItem().toString();
        String comoLlegoCliente = clientejf.comboComoLlego.getSelectedItem().toString();
        LocalDate fechaActual = LocalDate.now();
        int idDireccionCliente = verifDireccion(calleCliente,alturaCliente, localidadCliente);
        
        
        queryCliente.ejecutarUpdateDeleteInsert("insert into cliente (dni, apellido, nombre, telefono, fecha_ingreso, como_llego, idDireccion) values (?, ?, ?, ?, ?, ?, ?)", dniCliente, apellidoCliente, nombreCliente, telefonoCliente, fechaActual, comoLlegoCliente, idDireccionCliente);
        setDefault();
        cargarTable();
        limpiarLabels();
        
    }

    private static void cargarCombos() {
        for (String elemento : comoLlegoRepositorio) {
            clientejf.comboComoLlego.addItem(elemento);
        }

        for (Localidad localidad : localidadRepositorio) {
            clientejf.comboLocalidad.addItem(localidad.getLocalidad());
        }
    }
    
    //Este metodo verifica si existe la direccion de lo contrario la crea
    
    private static int verifDireccion(String calleCliente, int alturaCliente, String localidad) throws SQLException { 
        
        ResultSet resLocalidad =  queryLocalidad.ejecutarSelect("SELECT idLocalidad FROM localidad WHERE localidad=?", localidad);
        int idLoc = 0;
        if(resLocalidad.next()){
            idLoc = resLocalidad.getInt("idLocalidad");
            System.out.println("idLoc = " + idLoc);
        }
        
        ResultSet resDireccion = queryDireccion.ejecutarSelect("SELECT idDireccion FROM direccion WHERE calle=? and altura=? and idLocalidad=?", calleCliente, alturaCliente, idLoc);
        
        int idDireccion = 0;
        if(resDireccion.next()){
            idDireccion = resDireccion.getInt("idDireccion");
            System.out.println("idDireccion ya existente = " + idDireccion);
        }else{
            idDireccion = queryDireccion.ejecutarInsertConDevolucionDeClavePrimariaGenerada("insert into direccion (calle, altura, idLocalidad) values (?, ?, ?);",calleCliente, alturaCliente, idLoc);
            System.out.println("idDireccion recien creada = " + idDireccion);
        }
        
        return idDireccion;
    }

    private static void limpiarLabels() {
        clientejf.txtNombre.setText("");
        clientejf.txtApellido.setText("");
        clientejf.txtDni.setText("");
        clientejf.txtTelefonoArea.setText("");
        clientejf.txtTelefono.setText("");
      
        clientejf.comboLocalidad.setSelectedIndex(0);
        clientejf.txtAltura.setText("");
        clientejf.txtCalle.setText("");

        clientejf.comboComoLlego.setSelectedIndex(0);
    }
    
    public static boolean verificarCampos(){
        
        boolean resultado=true;
        boolean isEmpty=false;
           
        if(clientejf.txtNombre.getText().length()<2 || clientejf.txtNombre.getText().length()>30){
            resultado = false;
            if(clientejf.txtNombre.getText().length()==0){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "Nombre Invalido", "Error", JOptionPane.ERROR_MESSAGE);
            } 
            
        }else if(clientejf.txtApellido.getText().length()<2 || clientejf.txtApellido.getText().length()>30){
            resultado = false;
            if(clientejf.txtApellido.getText().length()==0){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "Apellido Invalido", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        }else if(clientejf.txtDni.getText().length()!=8){
            resultado = false;
            if(clientejf.txtDni.getText().length()==0){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "DNI Invalido", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        }else if(clientejf.txtTelefonoArea.getText().length()+clientejf.txtTelefono.getText().length() != 10){
            resultado = false;
            if((clientejf.txtTelefonoArea.getText().length()==0) || (clientejf.txtTelefono.getText().length()==0)){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "Código de Área o Telefono Invalido/s", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        }else if(clientejf.txtCalle.getText().length()<2 || clientejf.txtCalle.getText().length()>40){
            resultado = false;
            if(clientejf.txtCalle.getText().length()==0){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "Nombre de calle Invalido", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        }else if(clientejf.txtAltura.getText().length()<1 || clientejf.txtAltura.getText().length()>5){
            resultado = false;
            if(clientejf.txtAltura.getText().length()==0){
                isEmpty=true;
            }else{
                JOptionPane.showMessageDialog(null, "Altura de calle Invalido", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        }else if(clientejf.comboLocalidad.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(null, "Seleccione una localidad", "Error", JOptionPane.ERROR_MESSAGE);
            resultado = false;
        }else if(clientejf.comboComoLlego.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(null, "Seleccione un motivo de llegada", "Error", JOptionPane.ERROR_MESSAGE);
            resultado = false;
        }

        
        if(isEmpty){
            JOptionPane.showMessageDialog(null, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return resultado;
    }
    
    
    
    
    private static void actualizarCliente(){
        
    }

}

    

