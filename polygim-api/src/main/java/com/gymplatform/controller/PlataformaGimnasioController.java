package com.gymplatform.controller;

import com.gymplatform.controller.dto.GimnasioDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.service.GimnasioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plataforma/gimnasios")
public class PlataformaGimnasioController {
    private final GimnasioService service;
    public PlataformaGimnasioController(GimnasioService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<GimnasioDtos.Created> create(@Valid @RequestBody GimnasioDtos.CreateRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/plataforma/gimnasios/" + created.gimnasio().id())).body(created);
    }
    @GetMapping
    public PageResponse<GimnasioDtos.Gym> list(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return service.list(page, size);
    }
    @GetMapping("/{id}")
    public GimnasioDtos.Gym get(@PathVariable UUID id) { return service.find(id); }
}
