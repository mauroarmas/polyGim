package com.gymplatform.controller;

import com.gymplatform.controller.dto.ClienteDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.service.ClienteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }
    @PostMapping public ResponseEntity<ClienteDtos.Client> createClient(@Valid @RequestBody ClienteDtos.Write request) {
        var client = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + client.id())).body(client);
    }
    @GetMapping public PageResponse<ClienteDtos.Client> listClients(@RequestParam(required = false) String dni,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return service.list(dni, page, size);
    }
    @GetMapping("/{id}") public ClienteDtos.Client getClient(@PathVariable UUID id) { return service.get(id); }
    @PutMapping("/{id}") public ClienteDtos.Client updateClient(@PathVariable UUID id, @Valid @RequestBody ClienteDtos.Write request) {
        return service.update(id, request);
    }
}
