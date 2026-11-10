package com.gymplatform.service;

import com.gymplatform.model.AnulacionPago;
import com.gymplatform.model.Pago;
import com.gymplatform.model.Pedido;
import com.gymplatform.repository.PagoRepository;
import com.gymplatform.repository.AnulacionRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AnulacionServiceTest {
    @Test void createsEvidenceOnceAndRepeatedRequestReturnsSameEvidence() {
        var payments = mock(PagoRepository.class); var orders = mock(PedidoRepository.class); var annulments = mock(AnulacionRepository.class); var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID paymentId = UUID.randomUUID(), orderId = UUID.randomUUID(), actor = UUID.randomUUID();
        var payment = new Pago(paymentId, orderId, new BigDecimal("50.00"), UUID.randomUUID(), Instant.parse("2026-11-10T10:00:00Z"));
        when(payments.find(paymentId)).thenReturn(Optional.of(payment));
        when(orders.lock(orderId)).thenReturn(Optional.of(mock(Pedido.class)));
        var saved = new AtomicReference<AnulacionPago>();
        when(annulments.findByPayment(paymentId)).thenAnswer(call -> Optional.ofNullable(saved.get()));
        doAnswer(call -> { saved.set(call.getArgument(0)); return null; }).when(annulments).persist(any());
        var service = new AnulacionService(payments, orders, annulments, executor, Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(actor, UUID.randomUUID(), "user@example.org"), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        try {
            var first = service.annul(paymentId); var second = service.annul(paymentId);
            assertThat(first.creada()).isTrue(); assertThat(second.creada()).isFalse();
            assertThat(second.anulacion()).isEqualTo(first.anulacion());
            assertThat(first.anulacion().anuladoPorId()).isEqualTo(actor);
            assertThat(first.anulacion().fechaAnulacion()).isEqualTo(Instant.parse("2026-11-10T12:00:00Z"));
            verify(annulments, times(1)).persist(any());
            var sequence = inOrder(payments, orders, annulments);
            sequence.verify(payments).find(paymentId); sequence.verify(orders).lock(orderId); sequence.verify(annulments).findByPayment(paymentId);
        } finally { SecurityContextHolder.clearContext(); }
    }
    @Test void nonexistentOrForeignPaymentIsNotFound() {
        var payments = mock(PagoRepository.class); var orders = mock(PedidoRepository.class); var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID id = UUID.randomUUID(); when(payments.find(id)).thenReturn(Optional.empty());
        var service = new AnulacionService(payments, orders, mock(AnulacionRepository.class), executor, Clock.systemUTC());
        assertThatThrownBy(() -> service.annul(id)).isInstanceOf(java.util.NoSuchElementException.class);
        verifyNoInteractions(orders);
    }
}
