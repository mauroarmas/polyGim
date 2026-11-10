package com.gymplatform.service;

import com.gymplatform.controller.dto.PageResponse;
import com.gymplatform.controller.dto.PagoDtos;
import com.gymplatform.model.Pago;
import com.gymplatform.repository.PagoRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.service.policy.PagoPolicy;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PagoService {
    private final PedidoRepository orders;
    private final PagoRepository payments;
    private final TenantBusinessExecutor executor;
    private final Clock clock;
    public PagoService(PedidoRepository orders, PagoRepository payments, TenantBusinessExecutor executor, Clock clock) {
        this.orders = orders; this.payments = payments; this.executor = executor; this.clock = clock;
    }
    public PagoDtos.Result charge(UUID orderId, BigDecimal received) {
        return executor.business(() -> {
            var order = orders.lock(orderId).orElseThrow(NoSuchElementException::new);
            var result = PagoPolicy.apply(order.getTotal(), payments.sumValid(orderId), received);
            var principal = (CurrentAccountPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var payment = new Pago(UUID.randomUUID(), orderId, result.aplicado(), principal.accountId(), clock.instant());
            payments.persist(payment);
            return new PagoDtos.Result(PagoDtos.payment(payment, null), result.vuelto().toPlainString(),
                    result.deudaRestante().toPlainString(), result.estadoPago());
        });
    }
    public PagoDtos.Payment get(UUID id) {
        return executor.business(() -> {
            var payment = payments.find(id).orElseThrow(NoSuchElementException::new);
            return PagoDtos.payment(payment, payments.findAnnulment(id).orElse(null));
        });
    }
    public PageResponse<PagoDtos.Payment> list(UUID orderId, Integer page, Integer size) {
        var request = PageResponse.request(page, size);
        return executor.business(() -> {
            orders.find(orderId).orElseThrow(NoSuchElementException::new);
            return PageResponse.of(payments.list(orderId, request).map(view -> PagoDtos.payment(view.pago(), view.anulacion())));
        });
    }
}
