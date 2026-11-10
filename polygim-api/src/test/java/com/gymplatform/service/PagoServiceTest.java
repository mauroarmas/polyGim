package com.gymplatform.service;

import com.gymplatform.model.Pedido;
import com.gymplatform.repository.PagoRepository;
import com.gymplatform.repository.PedidoRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PagoServiceTest {
    @Test void locksParentBeforeReadingValidPaymentsAndApplyingBalance() {
        var orders = mock(PedidoRepository.class); var payments = mock(PagoRepository.class); var executor = mock(TenantBusinessExecutor.class);
        when(executor.business(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID id = UUID.randomUUID(); var order = mock(Pedido.class);
        when(order.getTotal()).thenReturn(new BigDecimal("200.50"));
        when(orders.lock(id)).thenReturn(Optional.of(order));
        when(payments.sumValid(id)).thenReturn(new BigDecimal("50.25"));
        var service = new PagoService(orders, payments, mock(com.gymplatform.repository.AnulacionRepository.class), executor, Clock.systemUTC());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.gymplatform.security.CurrentAccountPrincipal(UUID.randomUUID(), UUID.randomUUID(), "user@example.org"), null,
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
        try { assertThat(service.charge(id, new BigDecimal("200.00")).pago().montoAplicado()).isEqualTo("150.25"); }
        finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
        var sequence = inOrder(orders, payments);
        sequence.verify(orders).lock(id); sequence.verify(payments).sumValid(id);
    }
}
