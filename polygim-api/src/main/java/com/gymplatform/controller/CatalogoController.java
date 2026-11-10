package com.gymplatform.controller;

import com.gymplatform.controller.dto.CatalogoDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.repository.CatalogoRepository.Type;
import com.gymplatform.service.CatalogoService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CatalogoController {
    private final CatalogoService service;
    public CatalogoController(CatalogoService service) { this.service = service; }
    private ResponseEntity<CatalogoDtos.Item> create(Type type, CatalogoDtos.NameWrite body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(type, body.nombre()));
    }
    @GetMapping("/disciplinas") public PageResponse<CatalogoDtos.Item> listDisciplines(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return service.list(Type.DISCIPLINA, page, size); }
    @PostMapping("/disciplinas") public ResponseEntity<CatalogoDtos.Item> createDiscipline(@Valid @RequestBody CatalogoDtos.NameWrite body) { return create(Type.DISCIPLINA, body); }
    @GetMapping("/disciplinas/{id}") public CatalogoDtos.Item getDiscipline(@PathVariable UUID id) { return service.get(Type.DISCIPLINA, id); }
    @PutMapping("/disciplinas/{id}") public CatalogoDtos.Item updateDiscipline(@PathVariable UUID id, @Valid @RequestBody CatalogoDtos.NameWrite body) { return service.update(Type.DISCIPLINA, id, body.nombre()); }
    @GetMapping("/canales-llegada") public PageResponse<CatalogoDtos.Item> listArrivalChannels(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return service.list(Type.CANAL, page, size); }
    @PostMapping("/canales-llegada") public ResponseEntity<CatalogoDtos.Item> createArrivalChannel(@Valid @RequestBody CatalogoDtos.NameWrite body) { return create(Type.CANAL, body); }
    @GetMapping("/canales-llegada/{id}") public CatalogoDtos.Item getArrivalChannel(@PathVariable UUID id) { return service.get(Type.CANAL, id); }
    @PutMapping("/canales-llegada/{id}") public CatalogoDtos.Item updateArrivalChannel(@PathVariable UUID id, @Valid @RequestBody CatalogoDtos.NameWrite body) { return service.update(Type.CANAL, id, body.nombre()); }
    @GetMapping("/localidades") public PageResponse<CatalogoDtos.Item> listLocalitys(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) { return service.list(Type.LOCALIDAD, page, size); }
    @PostMapping("/localidades") public ResponseEntity<CatalogoDtos.Item> createLocality(@Valid @RequestBody CatalogoDtos.NameWrite body) { return create(Type.LOCALIDAD, body); }
    @GetMapping("/localidades/{id}") public CatalogoDtos.Item getLocality(@PathVariable UUID id) { return service.get(Type.LOCALIDAD, id); }
    @PutMapping("/localidades/{id}") public CatalogoDtos.Item updateLocality(@PathVariable UUID id, @Valid @RequestBody CatalogoDtos.NameWrite body) { return service.update(Type.LOCALIDAD, id, body.nombre()); }
}
