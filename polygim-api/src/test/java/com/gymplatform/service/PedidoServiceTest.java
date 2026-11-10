package com.gymplatform.service;

import com.gymplatform.controller.dto.PedidoDtos;
import com.gymplatform.model.*;
import com.gymplatform.repository.*;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {
    @Test void capturesCurrentMembershipAndRejectsRenewingAnAlreadyRenewedOrder() {
        var orders = mock(PedidoRepository.class); var clients = mock(ClienteRepository.class);
        var memberships = mock(MembresiaRepository.class); var tenants = mock(TenantRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.business(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID client = UUID.randomUUID(), member = UUID.randomUUID(), previous = UUID.randomUUID(), gym = UUID.randomUUID();
        when(clients.find(client)).thenReturn(Optional.of(mock(Cliente.class)));
        when(memberships.find(member)).thenReturn(Optional.of(new Membresia(member, UUID.randomUUID(), "Plan", new BigDecimal("100.25"), 2, "SEMANAS")));
        when(tenants.list(eq(ConfiguracionGimnasio.class), eq(0), eq(1))).thenReturn(List.of(new ConfiguracionGimnasio(UUID.randomUUID(), 12)));
        var service = new PedidoService(orders, clients, memberships, tenants, executor,
                Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), gym, "user@example.org"), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        try {
            var result = service.create(new PedidoDtos.Create(client, member, 2, null, null));
            assertThat(result.total()).isEqualTo("200.50");
            assertThat(result.fechaVencimiento()).isEqualTo(LocalDate.parse("2026-12-08"));
            var antecedent = new Pedido(previous, client, new DetallePedido(member, 1, new BigDecimal("100.25"), 2, "SEMANAS"),
                    new BigDecimal("100.25"), LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-15"), null, UUID.randomUUID());
            when(orders.lock(previous)).thenReturn(Optional.of(antecedent));
            when(orders.hasSuccessor(previous)).thenReturn(true);
            assertThatThrownBy(() -> service.create(new PedidoDtos.Create(client, member, 1, null, previous))).isInstanceOf(BusinessConflictException.class);
            verify(orders, times(1)).persist(any(Pedido.class));
        } finally { SecurityContextHolder.clearContext(); }
    }
}
