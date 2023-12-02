/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package poly;
import java.util.ArrayList;
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
         ArrayList<Direccion> direcciones = new ArrayList<>();
        Direccion d = new Direccion();
        d.setAltura(7);
        d.setCalle("ameghino");
        d.setIdDireccion(1);
        direcciones.add(d);
        Localidad a = new Localidad();
       a.setIdLocalidad(1);
       a.setLocalidad("alberdi");
       a.setDirecciones(direcciones);
        a.toString();
        
    }
    
}
