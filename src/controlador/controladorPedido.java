/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static controlador.controladorCliente.clientesRepositorio;
import static controlador.controladorNavegacion.pedidojf;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import modelo.Cliente;
import modelo.Db.PedidoDb;
import modelo.Membresia;
import modelo.Pedido;

/**
 *
 * @author mercadito
 */
public class controladorPedido {

    static Membresia a = new Membresia(1, "Dos semanas", 400);
    static Membresia b = new Membresia(2, "Mensual", 50000);
    static ArrayList<Membresia> membresiasRepositorio = new ArrayList<>();
    static ArrayList<Pedido> pedidos = new ArrayList<>();
    static PedidoDb pedidoQuery = new PedidoDb();

    public static void estadoInicialPedido() {
       pedidojf.setVisible(true);
    }

    public static void registrarPedido() {

        try {

            String dni = pedidojf.txtDniPedido.getText();
            boolean checkDni = chequearDni(dni);
            boolean checkCLiente = buscarCliente(Integer.parseInt(dni));
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
            String nombreMembresia = (String) pedidojf.comboTiempoPedido.getSelectedItem();
            String cantidadMem = (String) pedidojf.comboCantidad.getSelectedItem();
            
            if(nombreMembresia.equals("...") && cantidadMem.isEmpty()){
              JOptionPane.showMessageDialog(null, "Seleccione adecuadamente los datos de la memebresia", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                ///pedidojf.txtDniPedido.setText("");
                return;
            }
            Membresia mem = buscarMembresia(nombreMembresia);
            
            
            

        } catch (Exception e) {
            System.out.println(e);
        }

    }
    
    
    
    
     public static void combobox(){
            pedidojf.comboCantidad.removeAllItems();
           // pedidojf.comboCantidad.addItem("");
      
        String nombreMembresia = (String) pedidojf.comboTiempoPedido.getSelectedItem();
        
        
           switch (nombreMembresia) {
               case "Mensual":
                     pedidojf.comboCantidad.addItem("...");
                for(int i =0 ;i<12 ;i++){
                
             pedidojf.comboCantidad.addItem(String.valueOf(i+1));
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
     

    ////*****************FUNCIONES ENCAPSULADAS******************************///////////
    private static boolean buscarCliente(int dni) {

        for (Cliente cliente : clientesRepositorio) {

            if (cliente.getDni() == dni) {
                // El cliente fue encontrado
                return true;
            }

        }

        // El cliente no fue encontrado
        return false;
    }

    private static Membresia buscarMembresia(String nombreMembresia) {

        for (Membresia membresia : membresiasRepositorio) {
            if (membresia.getMembresia().equals(nombreMembresia)) {
                // La membresía fue encontrada
                return membresia;
            }
        }
        // La membresía no fue encontrada
        return null;
    }

    /////FUNCION TEMPORAL HASTA QUE SE HAGA ALA CARGA DE MEMBRESIAS
    private void cargarMembresia() {
        membresiasRepositorio.add(a);
        membresiasRepositorio.add(b);

    }

    private static boolean chequearDni(String dni) {

        if (dni.length() < 7 && dni.length() > 9) {

            return false;
        }

        for (int i = 0; i < 8; i++) {
            if (!Character.isDigit(dni.charAt(i))) {
                return false;
            }
        }

        return true;
    }
    

   
   
   
    
    
    

    ////**********************************************///////////
}
