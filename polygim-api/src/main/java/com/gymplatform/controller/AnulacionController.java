package com.gymplatform.controller;

import com.gymplatform.controller.dto.AnulacionDtos;
import com.gymplatform.service.AnulacionService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos")
public class AnulacionController {
    private final AnulacionService service;
    public AnulacionController(AnulacionService service) { this.service = service; }
    @PutMapping("/{id}/anulacion") public ResponseEntity<AnulacionDtos.Evidence> annulPayment(@PathVariable UUID id) {
        var result = service.annul(id);
        return ResponseEntity.status(result.creada() ? HttpStatus.CREATED : HttpStatus.OK).body(result.anulacion());
    }
}
