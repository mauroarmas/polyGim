/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;

/**
 *
 * @author mercadito
 */
public class Pruba {
    int nombre;
    ArrayList<Pago> pag ;

    public Pruba() {
        this.pag =  new ArrayList<>();
    }
   
    
     
    public int getNombre() {
        return nombre;
    }

    public void setNombre(int nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Pago> getPag() {
        return pag;
    }

    public void setPag(ArrayList<Pago> pag) {
        this.pag = pag;
    }
    

   public void añadirPago (int id, double monto){
       
          if (pag == null) {
            pag = new ArrayList<>();
        }

       
       
     Pago pago = new Pago();
    pago.setIdPago(id);
    pago.setMonto(monto);
    pag.add(pago);
   }
   
   
    
    
    
    
    
}
