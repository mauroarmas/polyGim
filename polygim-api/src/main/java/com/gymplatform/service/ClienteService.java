package com.gymplatform.service;

import com.gymplatform.controller.dto.ClienteDtos;
import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.model.Cliente;
import com.gymplatform.model.Direccion;
import com.gymplatform.repository.CatalogoRepository;
import com.gymplatform.repository.ClienteRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.time.Clock;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final CatalogoRepository catalogs;
    private final TenantBusinessExecutor executor;
    private final Clock clock;
    public ClienteService(ClienteRepository repository, CatalogoRepository catalogs, TenantBusinessExecutor executor, Clock clock) {
        this.repository = repository; this.catalogs = catalogs; this.executor = executor; this.clock = clock;
    }
    public static String validateDni(String dni) {
        if (dni == null || !dni.matches("[0-9]{7,8}")) throw new IllegalArgumentException("DNI inválido");
        return dni;
    }
    public ClienteDtos.Client create(ClienteDtos.Write request) {
        String dni = validateDni(request.dni());
        String name = CatalogoService.normalizeName(request.nombre());
        String surname = CatalogoService.normalizeName(request.apellido());
        return executor.business(() -> {
            validateReferences(request);
            var client = new Cliente(UUID.randomUUID(), LocalDate.now(clock), dni, name, surname,
                    clean(request.telefono()), request.canalLlegadaId(), address(request.direccion()), request.localidadId());
            repository.persist(client); return dto(client);
        });
    }
    public ClienteDtos.Client update(UUID id, ClienteDtos.Write request) {
        String dni = validateDni(request.dni());
        String name = CatalogoService.normalizeName(request.nombre());
        String surname = CatalogoService.normalizeName(request.apellido());
        return executor.business(() -> {
            var client = repository.find(id).orElseThrow(NoSuchElementException::new);
            validateReferences(request);
            client.update(dni, name, surname, clean(request.telefono()), request.canalLlegadaId(), address(request.direccion()), request.localidadId());
            return dto(client);
        });
    }
    public ClienteDtos.Client get(UUID id) {
        return executor.business(() -> dto(repository.find(id).orElseThrow(NoSuchElementException::new)));
    }
    public PageResponse<ClienteDtos.Client> list(String dni, Integer page, Integer size) {
        if (dni != null) validateDni(dni);
        var request = PageResponse.request(page, size);
        return executor.business(() -> PageResponse.of(repository.list(dni, request).map(ClienteService::dto)));
    }
    private void validateReferences(ClienteDtos.Write request) {
        if (request.canalLlegadaId() != null)
            catalogs.find(CatalogoRepository.Type.CANAL, request.canalLlegadaId()).orElseThrow(NoSuchElementException::new);
        if (request.localidadId() != null)
            catalogs.find(CatalogoRepository.Type.LOCALIDAD, request.localidadId()).orElseThrow(NoSuchElementException::new);
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static Direccion address(ClienteDtos.Address address) {
        if (address == null || (clean(address.calle()) == null && clean(address.altura()) == null)) return null;
        return new Direccion(clean(address.calle()), clean(address.altura()));
    }
    private static ClienteDtos.Client dto(Cliente client) {
        var address = client.getDireccion() == null ? null : new ClienteDtos.Address(client.getDireccion().getCalle(), client.getDireccion().getAltura());
        return new ClienteDtos.Client(client.getId(), client.getDni(), client.getNombre(), client.getApellido(), client.getTelefono(),
                client.getCanalLlegadaId(), address, client.getLocalidadId(), client.getFechaIngreso());
    }
}
