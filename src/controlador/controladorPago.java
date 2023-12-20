/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import View.PagoJF;
import static controlador.controladorNavegacion.pedidojf;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import modelo.Db.PagoDb;
import modelo.Pago;
import static controlador.controladorPedido.pedidoQuery;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;



/**
 *
 * @author mercadito
 */
public class controladorPago {

    static PagoDb queryPago = new PagoDb();
    static PagoJF pagojf = new PagoJF();
  



    public static Pago iniciarPago(int idPedido) throws SQLException {
      double de = -1;
        pagojf.txtMonto.setText("");
        pagojf.setVisible(true);
        ResultSet resDatosDePago = queryPago.traerDatosDePago(idPedido);
        Pago pago = new Pago();
       
        while (resDatosDePago.next()) {
            pagojf.txtPagoDni.setText(resDatosDePago.getString("dni"));
            pagojf.txtPagoServicio.setText(resDatosDePago.getString("servicio"));
            pagojf.txtPagoMembresia.setText(resDatosDePago.getString("membresia"));
            pagojf.txtPagoFechaI.setText(String.valueOf(resDatosDePago.getDate("fecha_pedido")));
            pagojf.txtPagoFechaV.setText(String.valueOf(resDatosDePago.getDate("fecha_vencimiento")));
            pagojf.txtPagoCantidad.setText(String.valueOf(resDatosDePago.getInt("cantidad")));
            pagojf.txtPagoTotal.setText(String.valueOf(resDatosDePago.getDouble("total")));
        }
        ResultSet resPagos = queryPago.traerSumaPagos(idPedido);

        if (resPagos.next() == false) {
            pagojf.txtPagoDeuda.setText(pagojf.txtPagoTotal.getText());
            pagojf.btnRegistrarPago.setEnabled(true);
        } else {
            de = Double.parseDouble(pagojf.txtPagoTotal.getText()) - resPagos.getDouble("SumaMontos");
            pagojf.txtPagoDeuda.setText(String.valueOf(de));

        }

        if (de == 0) {
            pagojf.btnRegistrarPago.setEnabled(false);
        }

        for (ActionListener al : pagojf.btnRegistrarPago.getActionListeners()) {
            pagojf.btnRegistrarPago.removeActionListener(al);
        }
        for (ActionListener al : pagojf.btnCancelarPago.getActionListeners()) {
            pagojf.btnCancelarPago.removeActionListener(al);
        }

        pagojf.btnRegistrarPago.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent m) {
              try {
                    double deuda = Double.parseDouble(pagojf.txtPagoDeuda.getText());

                    double monto = Double.parseDouble(pagojf.txtMonto.getText());
                    if (monto > deuda) {
                        int idPago = queryPago.registrarPago(deuda, idPedido);
                        if (idPago == -1) {
                            JOptionPane.showMessageDialog(null, "Hubo un error al registrar el pago", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                            pagojf.txtMonto.setText("");
                             
                            return;

                        } else {
                            double vuelto = monto - deuda;
                            String vuel = String.valueOf(vuelto);
                            JOptionPane.showMessageDialog(null, "Pago registrado con exito"
                                    + "El vuelto del Cliente es : " + vuel, "Informacion", JOptionPane.WARNING_MESSAGE);
                            pago.setIdPago(idPago);
                            pago.setMonto(monto);
                            pagojf.txtPagoDeuda.setText("0");
                            pagojf.txtMonto.setText("");
                            pagojf.btnRegistrarPago.setEnabled(false);
                            int filasAfectadas = pedidoQuery.modificarEstadoPedido(idPedido);

                            controladorPedido.cargarTatable();
                             
                            return;
                        }

                    } else if (monto < deuda) {
                        int idPago = queryPago.registrarPago(monto, idPedido);
                        if (idPago == -1) {
                            JOptionPane.showMessageDialog(null, "Hubo un error al registrar el pago", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                            pagojf.txtMonto.setText("");
                                
                            return;

                        } else {

                            JOptionPane.showMessageDialog(null, "Pago registrado con exito 2", "Informacion", JOptionPane.WARNING_MESSAGE);
                            pago.setIdPago(idPago);
                            pago.setMonto(monto);
                            pagojf.txtMonto.setText("");
                            pagojf.txtPagoDeuda.setText(String.valueOf(deuda - monto));
                        
                            return;
                        }
                    } else {
                        int idPago = queryPago.registrarPago(monto, idPedido);
                        if (idPago == -1) {
                            JOptionPane.showMessageDialog(null, "Hubo un error al registrar el pago", "Adevertencia", JOptionPane.WARNING_MESSAGE);
                            pagojf.txtMonto.setText("");
                      
                            return;

                        } else {

                            JOptionPane.showMessageDialog(null, "Pago regustrado con exito 3", "Informacion", JOptionPane.WARNING_MESSAGE);
                            pago.setIdPago(idPago);
                            pago.setMonto(monto);
                            pagojf.txtPagoDeuda.setText("0");
                            pagojf.btnRegistrarPago.setEnabled(false);
                            int filasAfectadas = pedidoQuery.modificarEstadoPedido(idPedido);

                            controladorPedido.cargarTatable();
                            

                            return;
                        }

                    }
                   
                } catch (NumberFormatException h) {
                    System.out.println("La cadena no es un número double válido.");
                } catch (SQLException ex) {
                    Logger.getLogger(controladorPago.class.getName()).log(Level.SEVERE, null, ex);
                }             
                
              
            }

        });

        pagojf.btnCancelarPago.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pagojf.setVisible(false);
                
               
               
            }

        });
          

        return pago;


}

}