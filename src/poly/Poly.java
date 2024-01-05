/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package poly;
import controlador.controladorDatos;
import controlador.controladorNavegacion;
import controlador.controladorServicio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import modelo.*;
import org.mariadb.jdbc.Connection;
/**
 *
 * @author agust
 */
public class Poly {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
       /* 
        ///CALCULAR FECHA ACTUAL
        LocalDate aa =  LocalDate.now();
        System.out.println(aa);
        
        /////INGRESAR UNA FECHA CUALQUIERA
        int dia = 10;
        int mes = 2;
        int anio = 2000;
        LocalDate b = LocalDate.of(anio,mes,dia);
        System.out.println(b);
        /////OBTENER LOS VALORES DE UN DATO TIPO DATE
           int d = b.getDayOfMonth();
           int m = b.getMonthValue();
           int a = b.getYear();
                  
        */
           /*
         Pruba a = new Pruba();
        a.setNombre(1);
        a.añadirPago(1, 20);
       ArrayList<Pago> agus = a.getPag();
        
          for (int i = 0; i < agus.size(); i++) {
            System.out.println(agus.get(i).getMonto());
        }
                 
         */
            
                
                //LocalDate aa =  LocalDate.now();
                //java.sql.Date fechaaql = java.sql.Date.valueOf(aa);
                //System.out.println(aa);
                //System.out.println(fechaaql);
            //   controladorNavegacion.iniciarPedido();
            
           //  controladorNavegacion.iniciarPedido();
          
           controladorDatos.loadArrayList();
           controladorNavegacion.iniciarCLiente();
         
          
    }
    
   
           
              
}
       
       
       
        
     
    
    

