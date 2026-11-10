package com.gymplatform.service;

import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.controller.dto.PedidoDtos;
import com.gymplatform.repository.ClienteRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.time.Clock;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PedidoQueryService {
    private final PedidoRepository orders;
    private final ClienteRepository clients;
    private final TenantBusinessExecutor executor;
    private final Clock clock;
    public PedidoQueryService(PedidoRepository orders, ClienteRepository clients, TenantBusinessExecutor executor, Clock clock) {
        this.orders = orders; this.clients = clients; this.executor = executor; this.clock = clock;
    }
    public PedidoDtos.Order get(UUID id) {
        var today = LocalDate.now(clock);
        return executor.business(() -> {
            var order = orders.find(id).orElseThrow(NoSuchElementException::new);
            return PedidoDtos.from(order, today, order.getTotal());
        });
    }
    public PageResponse<PedidoDtos.Order> list(UUID client, Integer page, Integer size) {
        var today = LocalDate.now(clock);
        var request = PageResponse.request(page, size);
        return executor.business(() -> {
            if (client != null) clients.find(client).orElseThrow(NoSuchElementException::new);
            return PageResponse.of(orders.list(client, request).map(order -> PedidoDtos.from(order, today, order.getTotal())));
        });
    }
}
