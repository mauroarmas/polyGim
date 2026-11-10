package com.gymplatform.controller;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuracion")
public class ConfiguracionController {
    private final ConfiguracionService service;
    public ConfiguracionController(ConfiguracionService service) { this.service = service; }
    @GetMapping public CatalogoDtos.Configuration getConfiguration() { return service.get(); }
    @PutMapping public CatalogoDtos.Configuration updateConfiguration(@Valid @RequestBody CatalogoDtos.ConfigurationWrite body) {
        return service.update(body.cantidadMaximaCompra());
    }
}
