package com.gymplatform.controller;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.controller.dto.MoneyCodec;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.service.MembresiaService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membresias")
public class MembresiaController {
    private final MembresiaService service;
    public MembresiaController(MembresiaService service) { this.service = service; }
    @GetMapping public PageResponse<CatalogoDtos.Membership> listMemberships(@RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return service.list(disciplinaId, page, size); }
    @PostMapping public ResponseEntity<CatalogoDtos.Membership> createMembership(@Valid @RequestBody CatalogoDtos.MembershipCreate body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body.disciplinaId(), body.nombre(), MoneyCodec.parse(body.precio()), body.duracion(), body.unidadDuracion()));
    }
    @GetMapping("/{id}") public CatalogoDtos.Membership getMembership(@PathVariable UUID id) { return service.get(id); }
    @PutMapping("/{id}") public CatalogoDtos.Membership updateMembership(@PathVariable UUID id, @Valid @RequestBody CatalogoDtos.MembershipUpdate body) {
        return service.update(id, body.nombre(), MoneyCodec.parse(body.precio()), body.duracion(), body.unidadDuracion());
    }
}
