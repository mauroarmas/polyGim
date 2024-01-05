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
        clientejf.txtTelefono.setEnabled(false);
        clientejf.txtDni.setEnabled(false);
        clientejf.comboLocalidad.setEnabled(false);
        clientejf.comboComoLlego.setEnabled(false);

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
        clientejf.txtTelefono.setEnabled(true);
        clientejf.txtDni.setEnabled(true);
        clientejf.comboLocalidad.setEnabled(true);
        clientejf.comboComoLlego.setEnabled(true);

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

    public static void registrarCliente() {
        String alturaCliente = clientejf.txtAltura.getText();
        String calleCliente = clientejf.txtCalle.getText();
        String nombreCliente = clientejf.txtNombre.getText();
        String apellidoCliente = clientejf.txtApellido.getText();
        String telefonoCliente = clientejf.txtTelefono.getText();
        String dniCliente = clientejf.txtDni.getText();
        String localidadCliente = clientejf.comboLocalidad.getSelectedItem().toString();
        String comoLlegoCliente = clientejf.comboComoLlego.getSelectedItem().toString();
        
    }

    private static void cargarCombos() {
        for (String elemento : comoLlegoRepositorio) {
            clientejf.comboComoLlego.addItem(elemento);
        }

        for (Localidad localidad : localidadRepositorio) {
            clientejf.comboLocalidad.addItem(localidad.getLocalidad());
        }
    }

        

}

    

