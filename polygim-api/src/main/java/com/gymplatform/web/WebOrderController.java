package com.gymplatform.web;

import com.gymplatform.controller.dto.*;
import com.gymplatform.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @Hidden
public class WebOrderController {
    private final ClienteService clients; private final MembresiaService memberships; private final PedidoQueryService queries;
    private final PedidoService orders; private final PagoService payments; private final AnulacionService annulments;
    private final ConfiguracionService configuration; private final Clock clock;
    public WebOrderController(ClienteService clients,MembresiaService memberships,PedidoQueryService queries,PedidoService orders,
            PagoService payments,AnulacionService annulments,ConfiguracionService configuration,Clock clock) {
        this.clients=clients;this.memberships=memberships;this.queries=queries;this.orders=orders;
        this.payments=payments;this.annulments=annulments;this.configuration=configuration;this.clock=clock;
    }
    @GetMapping("/app/pedidos") public String list(@RequestParam(defaultValue="0") int page,Model model) {
        var listing=queries.list(null,page,20); model.addAttribute("listing",listing);
        var names=new java.util.HashMap<UUID,String>(); var plans=new java.util.HashMap<UUID,String>();
        for(var order:listing.content()) {
            names.computeIfAbsent(order.clienteId(),id->{var client=clients.get(id);return client.nombre()+" "+client.apellido();});
            plans.computeIfAbsent(order.membresiaId(),id->memberships.get(id).nombre());
        }
        model.addAttribute("clientNames",names);model.addAttribute("membershipNames",plans); return "web/orders";
    }
    @GetMapping("/app/clientes/{id}/comprar") public String purchaseForm(@PathVariable UUID id,@RequestParam(required=false) UUID anterior,Model model) {
        model.addAttribute("client",clients.get(id)); model.addAttribute("memberships",WebViewSupport.all(p->memberships.list(null,p,100)));
        model.addAttribute("today",LocalDate.now(clock)); model.addAttribute("maximum",configuration.get().cantidadMaximaCompra());
        PedidoDtos.Order previous=anterior==null?null:queries.get(anterior);
        if(previous!=null && !previous.clienteId().equals(id)) throw new java.util.NoSuchElementException();
        model.addAttribute("previous",previous);return "web/purchase";
    }
    @PostMapping("/app/clientes/{id}/comprar") public String purchase(@PathVariable UUID id,@Valid @ModelAttribute WebForms.Purchase form,RedirectAttributes flash) {
        var order=orders.create(new PedidoDtos.Create(id,form.membresiaId(),form.cantidad(),form.fechaCompra(),form.pedidoAnteriorId()));
        flash.addFlashAttribute("notice","Compra registrada. Ya podés registrar el pago.");return "redirect:/app/pedidos/"+order.id();
    }
    @GetMapping("/app/pedidos/{id}") public String detail(@PathVariable UUID id,@RequestParam(defaultValue="0") int page,Model model) {
        var order=queries.get(id);model.addAttribute("order",order);model.addAttribute("client",clients.get(order.clienteId()));
        model.addAttribute("membership",memberships.get(order.membresiaId()));
        var history=payments.list(id,page,20); model.addAttribute("listing",history);
        var formatter=java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(clock.getZone());
        var dates=new java.util.HashMap<UUID,String>(); var annulledDates=new java.util.HashMap<UUID,String>();
        for(var payment:history.content()) {
            dates.put(payment.id(),formatter.format(payment.fechaRegistro()));
            if(payment.anulacion()!=null) annulledDates.put(payment.id(),formatter.format(payment.anulacion().fechaAnulacion()));
        }
        model.addAttribute("paymentDates",dates); model.addAttribute("annulmentDates",annulledDates);return "web/order";
    }
    @PostMapping("/app/pedidos/{id}/pagos") public String payment(@PathVariable UUID id,@Valid @ModelAttribute WebForms.Payment form,RedirectAttributes flash) {
        var result=payments.charge(id,MoneyCodec.parse(form.montoRecibido()));
        flash.addFlashAttribute("notice","Pago registrado: "+result.pago().montoAplicado()+". Vuelto: "+result.vuelto()+". Deuda restante: "+result.deudaRestante()+".");
        return "redirect:/app/pedidos/"+id;
    }
    @GetMapping("/app/pagos/{id}/anular") public String confirmation(@PathVariable UUID id,Model model) {
        var payment=payments.get(id);model.addAttribute("payment",payment);var order=queries.get(payment.pedidoId());model.addAttribute("client",clients.get(order.clienteId()));return "web/annul";
    }
    @PostMapping("/app/pagos/{id}/anular") public String annul(@PathVariable UUID id,RedirectAttributes flash) {
        var payment=payments.get(id);annulments.annul(id);flash.addFlashAttribute("notice","Pago anulado. El movimiento se conserva en el historial.");return "redirect:/app/pedidos/"+payment.pedidoId();
    }
}
