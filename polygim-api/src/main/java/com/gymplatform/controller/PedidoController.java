package com.gymplatform.controller;

import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.controller.dto.PedidoDtos;
import com.gymplatform.service.PedidoQueryService;
import com.gymplatform.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final PedidoService service;
    private final PedidoQueryService queries;
    public PedidoController(PedidoService service, PedidoQueryService queries) { this.service = service; this.queries = queries; }
    @PostMapping public ResponseEntity<PedidoDtos.Order> createOrder(@Valid @RequestBody PedidoDtos.Create request) {
        var order = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + order.id())).body(order);
    }
    @GetMapping public PageResponse<PedidoDtos.Order> listOrders(@RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return queries.list(clienteId, page, size);
    }
    @GetMapping("/{id}") public PedidoDtos.Order getOrder(@PathVariable UUID id) { return queries.get(id); }
}
