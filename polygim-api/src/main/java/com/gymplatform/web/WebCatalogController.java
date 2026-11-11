package com.gymplatform.web;

import com.gymplatform.controller.dto.MoneyCodec;
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
public class WebCatalogController {
    private final CatalogoService catalogs; private final MembresiaService memberships; private final ConfiguracionService configuration;
    public WebCatalogController(CatalogoService catalogs,MembresiaService memberships,ConfiguracionService configuration) {this.catalogs=catalogs;this.memberships=memberships;this.configuration=configuration;}
    private Type type(String kind) {
        return switch(kind) { case "disciplinas"->Type.DISCIPLINA;case "canales"->Type.CANAL;case "localidades"->Type.LOCALIDAD;default->throw new java.util.NoSuchElementException();};
    }
    @GetMapping("/app/catalogos/{kind}") public String catalog(@PathVariable String kind,@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("listing",catalogs.list(type(kind),page,20));model.addAttribute("kind",kind);
        model.addAttribute("catalogTitle",switch(kind){case "disciplinas"->"Disciplinas";case "canales"->"Cómo llegaron";default->"Localidades";});return "web/catalog";
    }
    @PostMapping("/app/catalogos/{kind}") public String create(@PathVariable String kind,@Valid @ModelAttribute WebForms.Name form,RedirectAttributes flash) {
        catalogs.create(type(kind),form.nombre());flash.addFlashAttribute("notice","Registro creado.");return "redirect:/app/catalogos/"+kind;
    }
    @PostMapping("/app/catalogos/{kind}/{id}") public String update(@PathVariable String kind,@PathVariable UUID id,@Valid @ModelAttribute WebForms.Name form,RedirectAttributes flash) {
        catalogs.update(type(kind),id,form.nombre());flash.addFlashAttribute("notice","Registro actualizado.");return "redirect:/app/catalogos/"+kind;
    }
    @GetMapping("/app/membresias") public String memberships(@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("listing",memberships.list(null,page,20));var names=new java.util.HashMap<UUID,String>();
        WebViewSupport.all(p->catalogs.list(Type.DISCIPLINA,p,100)).forEach(d->names.put(d.id(),d.nombre()));model.addAttribute("disciplineNames",names);return "web/memberships";
    }
    @GetMapping("/app/membresias/nueva") public String newMembership(Model model) {model.addAttribute("membership",null);options(model);return "web/membership-form";}
    @GetMapping("/app/membresias/{id}/editar") public String editMembership(@PathVariable UUID id,Model model) {model.addAttribute("membership",memberships.get(id));options(model);return "web/membership-form";}
    private void options(Model model) {model.addAttribute("disciplines",WebViewSupport.all(p->catalogs.list(Type.DISCIPLINA,p,100)));}
    @PostMapping("/app/membresias") public String createMembership(@Valid @ModelAttribute WebForms.Membership form,RedirectAttributes flash) {
        if(form.disciplinaId()==null) throw new IllegalArgumentException();
        memberships.create(form.disciplinaId(),form.nombre(),MoneyCodec.parse(form.precio()),form.duracion(),form.unidadDuracion());
        flash.addFlashAttribute("notice","Membresía creada.");return "redirect:/app/membresias";
    }
    @PostMapping("/app/membresias/{id}/editar") public String updateMembership(@PathVariable UUID id,@Valid @ModelAttribute WebForms.Membership form,RedirectAttributes flash) {
        memberships.update(id,form.nombre(),MoneyCodec.parse(form.precio()),form.duracion(),form.unidadDuracion());
        flash.addFlashAttribute("notice","Membresía actualizada. Las compras anteriores conservan sus condiciones.");return "redirect:/app/membresias";
    }
    @GetMapping("/app/configuracion") public String configuration(Model model) {model.addAttribute("configuration",configuration.get());return "web/configuration";}
    @PostMapping("/app/configuracion") public String updateConfiguration(@Valid @ModelAttribute WebForms.Configuration form,RedirectAttributes flash) {
        configuration.update(form.cantidadMaximaCompra());flash.addFlashAttribute("notice","Configuración guardada.");return "redirect:/app/configuracion";
    }
}
