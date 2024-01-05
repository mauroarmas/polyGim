/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import View.*;
import java.sql.SQLException;

/**
 *
 * @author mercadito
 */
public class controladorNavegacion {
    static PedidoJF pedidojf = new PedidoJF();
    static ClienteJF clientejf = new ClienteJF();
    static ServicioJF serviciojf = new ServicioJF();
    public static void iniciarPrincipal(){
  
     ///**AQUI ARRANCA EL SISTEMA**********////////// 
     ///**AUN SE DEBE DEFINIR ÑA PANTALLA PRINCIPAL**********//////////       
        
   }
    public static void iniciarCLiente() throws SQLException {
        controladorCliente.estadoInicialCliente();

    }
    
    
    
    
    
   public static void iniciarPedido() throws SQLException{
   //pedidojf.setVisible(true);
   //controladorPedido.estadoInicialPedido();
        //controladorServicio.cargarArrayServicios();
       
       
        controladorPedido.estadoInicialPedido();
        
   }
    
    
}
