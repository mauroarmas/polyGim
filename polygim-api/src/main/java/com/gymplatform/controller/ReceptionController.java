package com.gymplatform.controller;

import com.gymplatform.controller.dto.AuthDtos;
import com.gymplatform.controller.dto.ReceptionDtos;
import com.gymplatform.service.ReceptionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios-recepcion")
public class ReceptionController {
    private final ReceptionService service;
    public ReceptionController(ReceptionService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<AuthDtos.User> createReception(@Valid @RequestBody ReceptionDtos.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.email(), request.password()));
    }
    @PutMapping("/{id}/deshabilitacion")
    public AuthDtos.User disableReception(@PathVariable UUID id) { return service.disable(id); }
}
