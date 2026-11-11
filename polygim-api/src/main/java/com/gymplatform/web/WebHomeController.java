package com.gymplatform.web;

import com.gymplatform.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller @Hidden
public class WebHomeController {
    private final ClienteService clients;
    private final MembresiaService memberships;
    private final PedidoQueryService orders;
    public WebHomeController(ClienteService clients, MembresiaService memberships, PedidoQueryService orders) { this.clients=clients;this.memberships=memberships;this.orders=orders; }
    @GetMapping("/") public String root() { return "redirect:/app"; }
    @GetMapping("/login") public String login() { return "web/login"; }
    @GetMapping("/app") public String home(Authentication auth, Model model) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) return "redirect:/app/plataforma";
        model.addAttribute("clientCount",clients.list(null,0,1).totalElements());
        model.addAttribute("membershipCount",memberships.list(null,0,1).totalElements());
        model.addAttribute("orderCount",orders.list(null,0,1).totalElements());
        model.addAttribute("recent",clients.list(null,0,5).content());
        return "web/home";
    }
}
