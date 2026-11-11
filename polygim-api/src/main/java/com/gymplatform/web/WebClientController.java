package com.gymplatform.web;

import com.gymplatform.repository.CatalogoRepository.Type;
import com.gymplatform.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @Hidden
@RequestMapping("/app/clientes")
public class WebClientController {
    private final ClienteService clients;
    private final CatalogoService catalogs;
    private final PedidoQueryService orders;
    private final MembresiaService memberships;
    public WebClientController(ClienteService clients, CatalogoService catalogs, PedidoQueryService orders, MembresiaService memberships) {
        this.clients=clients;this.catalogs=catalogs;this.orders=orders;this.memberships=memberships;
    }
    @GetMapping public String list(@RequestParam(defaultValue="") String dni, @RequestParam(defaultValue="0") int page,
            @RequestHeader(name="HX-Request",defaultValue="false") boolean partial, Model model) {
        String filter=dni.isBlank()?null:dni.trim();
        // An incomplete search is a normal input state, not an invalid business query.
        boolean valid = filter==null || filter.matches("[0-9]{7,8}");
        model.addAttribute("listing",valid?clients.list(filter,page,20):new com.gymplatform.controller.dto.PageResponse<>(java.util.List.of(),0,20,0,0));
        model.addAttribute("dni",dni); model.addAttribute("incompleteDni",!valid);
        return partial?"web/clients :: results":"web/clients";
    }
    @GetMapping("/nuevo") public String createForm(Model model) { model.addAttribute("client",null); options(model); return "web/client-form"; }
    @PostMapping public String create(@Valid @ModelAttribute WebForms.Client form, RedirectAttributes flash) {
        var client=clients.create(form.request()); flash.addFlashAttribute("notice","Cliente registrado."); return "redirect:/app/clientes/"+client.id();
    }
    @GetMapping("/{id}") public String detail(@PathVariable UUID id,@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("client",clients.get(id)); var listing=orders.list(id,page,20); model.addAttribute("listing",listing);
        var names=new java.util.HashMap<UUID,String>(); for(var order:listing.content()) names.computeIfAbsent(order.membresiaId(), key->memberships.get(key).nombre());
        model.addAttribute("membershipNames",names); return "web/client";
    }
    @GetMapping("/{id}/editar") public String edit(@PathVariable UUID id,Model model) { model.addAttribute("client",clients.get(id)); options(model); return "web/client-form"; }
    @PostMapping("/{id}/editar") public String update(@PathVariable UUID id,@Valid @ModelAttribute WebForms.Client form,RedirectAttributes flash) {
        clients.update(id,form.request());flash.addFlashAttribute("notice","Datos del cliente actualizados.");return "redirect:/app/clientes/"+id;
    }
    private void options(Model model) {
        model.addAttribute("channels",WebViewSupport.all(p->catalogs.list(Type.CANAL,p,100)));
        model.addAttribute("localities",WebViewSupport.all(p->catalogs.list(Type.LOCALIDAD,p,100)));
    }
}
