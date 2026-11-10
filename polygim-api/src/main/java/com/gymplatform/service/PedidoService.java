package com.gymplatform.service;

import com.gymplatform.controller.dto.PedidoDtos;
import com.gymplatform.model.ConfiguracionGimnasio;
import com.gymplatform.model.DetallePedido;
import com.gymplatform.model.Pedido;
import com.gymplatform.repository.ClienteRepository;
import com.gymplatform.repository.MembresiaRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.repository.TenantRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.service.policy.PedidoPolicy;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.time.Clock;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {
    private final PedidoRepository orders;
    private final ClienteRepository clients;
    private final MembresiaRepository memberships;
    private final TenantRepository tenants;
    private final TenantBusinessExecutor executor;
    private final Clock clock;
    public PedidoService(PedidoRepository orders, ClienteRepository clients, MembresiaRepository memberships,
                         TenantRepository tenants, TenantBusinessExecutor executor, Clock clock) {
        this.orders = orders; this.clients = clients; this.memberships = memberships;
        this.tenants = tenants; this.executor = executor; this.clock = clock;
    }
    public PedidoDtos.Order create(PedidoDtos.Create request) {
        LocalDate today = LocalDate.now(clock);
        LocalDate purchase = PedidoPolicy.purchaseDate(request.fechaCompra(), today);
        return executor.business(() -> {
            clients.find(request.clienteId()).orElseThrow(NoSuchElementException::new);
            var membership = memberships.find(request.membresiaId()).orElseThrow(NoSuchElementException::new);
            int maximum = tenants.list(ConfiguracionGimnasio.class, 0, 1).stream().findFirst()
                    .orElseThrow(NoSuchElementException::new).getCantidadMaximaCompra();
            var total = PedidoPolicy.total(membership.getPrecio(), request.cantidad(), maximum);
            LocalDate base = purchase;
            if (request.pedidoAnteriorId() != null) {
                var previous = orders.lock(request.pedidoAnteriorId()).orElseThrow(NoSuchElementException::new);
                if (!previous.getClienteId().equals(request.clienteId()) || !previous.getDetalle().getMembresiaId().equals(request.membresiaId())
                        || purchase.isBefore(previous.getFechaCompra())) throw new IllegalArgumentException("Antecedente inválido");
                if (orders.hasSuccessor(previous.getId())) throw new BusinessConflictException();
                base = PedidoPolicy.renewalBase(purchase, previous.getFechaVencimiento());
            }
            var detail = new DetallePedido(membership.getId(), request.cantidad(), membership.getPrecio(), membership.getDuracion(), membership.getUnidadDuracion());
            var principal = (CurrentAccountPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var order = new Pedido(UUID.randomUUID(), request.clienteId(), detail, total, purchase, base,
                    PedidoPolicy.expiry(base, request.cantidad(), membership.getDuracion(), membership.getUnidadDuracion()),
                    request.pedidoAnteriorId(), principal.accountId());
            orders.persist(order);
            return PedidoDtos.from(order, today, total);
        });
    }
}
