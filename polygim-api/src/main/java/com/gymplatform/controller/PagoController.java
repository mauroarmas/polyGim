package com.gymplatform.controller;

import com.gymplatform.controller.dto.MoneyCodec;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.controller.dto.PagoDtos;
import com.gymplatform.service.PagoService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PagoController {
    private final PagoService service;
    public PagoController(PagoService service) { this.service = service; }
    @PostMapping("/pedidos/{id}/pagos") public ResponseEntity<PagoDtos.Result> createPayment(@PathVariable UUID id, @Valid @RequestBody PagoDtos.Create body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.charge(id, MoneyCodec.parse(body.montoRecibido())));
    }
    @GetMapping("/pedidos/{id}/pagos") public PageResponse<PagoDtos.Payment> listOrderPayments(@PathVariable UUID id,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return service.list(id, page, size); }
    @GetMapping("/pagos/{id}") public PagoDtos.Payment getPayment(@PathVariable UUID id) { return service.get(id); }
}
