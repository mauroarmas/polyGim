package com.gymplatform.service;

import com.gymplatform.controller.dto.PagoDtos;
import com.gymplatform.controller.dto.AnulacionDtos;
import com.gymplatform.model.AnulacionPago;
import com.gymplatform.repository.AnulacionRepository;
import com.gymplatform.repository.PagoRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.time.Clock;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AnulacionService {
    public record Result(AnulacionDtos.Evidence anulacion, boolean creada) { }
    private final PagoRepository payments;
    private final PedidoRepository orders;
    private final AnulacionRepository annulments;
    private final TenantBusinessExecutor executor;
    private final Clock clock;
    public AnulacionService(PagoRepository payments, PedidoRepository orders, AnulacionRepository annulments,
                            TenantBusinessExecutor executor, Clock clock) {
        this.payments = payments; this.orders = orders; this.annulments = annulments; this.executor = executor; this.clock = clock;
    }
    public Result annul(UUID paymentId) {
        return executor.admin(() -> {
            var payment = payments.find(paymentId).orElseThrow(NoSuchElementException::new);
            orders.lock(payment.getPedidoId()).orElseThrow(NoSuchElementException::new);
            var existing = annulments.findByPayment(paymentId);
            if (existing.isPresent()) return new Result(PagoDtos.annulment(existing.get()), false);
            var principal = (CurrentAccountPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var annulment = new AnulacionPago(UUID.randomUUID(), paymentId, principal.accountId(),
                    clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
            annulments.persist(annulment);
            return new Result(PagoDtos.annulment(annulment), true);
        });
    }
}
