package com.gymplatform.web;

import com.gymplatform.controller.dto.GimnasioDtos;
import com.gymplatform.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @Hidden
public class WebAdministrationController {
    private final ReceptionService reception; private final GimnasioService gyms;
    public WebAdministrationController(ReceptionService reception,GimnasioService gyms){this.reception=reception;this.gyms=gyms;}
    @GetMapping("/app/recepcion") public String reception(@RequestParam(defaultValue="0") int page,Model model){model.addAttribute("listing",reception.list(page,20));return "web/reception";}
    @PostMapping("/app/recepcion") public String createReception(@Valid @ModelAttribute WebForms.Reception form,RedirectAttributes flash){reception.create(form.email(),form.password());flash.addFlashAttribute("notice","Cuenta de recepción creada.");return "redirect:/app/recepcion";}
    @PostMapping("/app/recepcion/{id}/deshabilitar") public String disableReception(@PathVariable UUID id,RedirectAttributes flash){reception.disable(id);flash.addFlashAttribute("notice","Cuenta deshabilitada. Ya no puede operar.");return "redirect:/app/recepcion";}
    @GetMapping("/app/plataforma") public String gyms(@RequestParam(defaultValue="0") int page,Model model){model.addAttribute("listing",gyms.list(page,20));return "web/platform";}
    @PostMapping("/app/plataforma") public String createGym(@Valid @ModelAttribute WebForms.Gym form,RedirectAttributes flash){
        gyms.create(new GimnasioDtos.CreateRequest(form.nombre(),form.logoUrl(),form.colorPrimario(),new GimnasioDtos.NewAccount(form.email(),form.password())));
        flash.addFlashAttribute("notice","Gimnasio y primer administrador creados.");return "redirect:/app/plataforma";
    }
}
