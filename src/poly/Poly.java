/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package poly;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import modelo.*;
/**
 *
 * @author agust
 */
public class Poly {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       LocalDate fec = LocalDate.now();
       Cliente c = new Cliente(43652957,"mercado","agustin","3865",fec,"recomendacion");
       
        System.out.println(c.toString());
        System.out.println("Prueba de cambiooo");
    }
    
}
