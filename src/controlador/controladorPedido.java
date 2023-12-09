/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static controlador.controladorCliente.clientes;
import static controlador.controladorNavegacion.pedido;
import java.util.ArrayList;
import modelo.Cliente;
import modelo.Membresia;

/**
 *
 * @author mercadito
 */
public class controladorPedido {

    static Membresia a = new Membresia(1, "Dos semanas", 400);
    static Membresia b = new Membresia(2, "Mensual", 50000);
    static ArrayList<Membresia> c = new ArrayList<>();

    public static void registrarPedido() {

    }

    private boolean buscarCliente(int dni) {

        for (Cliente cliente : clientes) {

            if (cliente.getDni() == dni) {

                return true;
            }

        }

        return false;
    }

    private Membresia buscarMembresia() {
        Membresia mem = new Membresia();

        return mem;
    }

    private void cargarMembresia() {
        c.add(a);
        c.add(b);

    }

}
