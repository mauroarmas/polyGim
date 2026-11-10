package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import com.gymplatform.repository.PedidoRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RenovacionConcurrencyIT extends OrderApiSupport {
    @SpyBean PedidoRepository orders;

    @Test void onlyOneConcurrentRenewalOfTheSameOrderSucceeds() throws Exception {
        UUID previous = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        var locked = new CountDownLatch(1);
        var secondEntered = new CountDownLatch(1);
        var calls = new AtomicInteger();
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                Object result = invocation.callRealMethod();
                locked.countDown();
                if (!secondEntered.await(10, TimeUnit.SECONDS)) throw new AssertionError("Segunda renovación no llegó al lock");
                return result;
            }
            if (!locked.await(10, TimeUnit.SECONDS)) throw new AssertionError("Primera renovación no tomó el lock");
            secondEntered.countDown();
            return invocation.callRealMethod();
        }).when(orders).lock(eq(previous));
        String body = json(Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1, "pedidoAnteriorId", previous));
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus());
            var second = pool.submit(() -> mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus());
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS))).containsExactlyInAnyOrder(201, 409);
        }
        mvc.perform(get("/api/v1/pedidos?clienteId=" + clientA).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test void rejectsWrongClientMembershipAndEarlierPurchaseDate() throws Exception {
        UUID previous = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        UUID anotherClient = create("clientes", adminA, Map.of("dni", "7654321", "nombre", "Otra", "apellido", "Persona"));
        UUID anotherMembership = create("membresias", adminA, Map.of("disciplinaId", disciplineA, "nombre", "Otra", "precio", "12.00", "duracion", 1, "unidadDuracion", "DIAS"));
        for (var body : List.of(
                Map.of("clienteId", anotherClient, "membresiaId", membershipA, "cantidad", 1, "pedidoAnteriorId", previous),
                Map.of("clienteId", clientA, "membresiaId", anotherMembership, "cantidad", 1, "pedidoAnteriorId", previous),
                Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1, "pedidoAnteriorId", previous,
                        "fechaCompra", LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()))) {
            mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA)
                            .contentType(MediaType.APPLICATION_JSON).content(json(body))).andExpect(status().isBadRequest());
        }
    }
}
