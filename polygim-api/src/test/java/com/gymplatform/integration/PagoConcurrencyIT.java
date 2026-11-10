package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import com.gymplatform.repository.PedidoRepository;
import java.math.BigDecimal;
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
class PagoConcurrencyIT extends OrderApiSupport {
    @SpyBean PedidoRepository orders;
    @Test void simultaneousPaymentsSerializeAndNeverExceedTheBalance() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        var locked = new CountDownLatch(1); var secondEntered = new CountDownLatch(1); var calls = new AtomicInteger();
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                Object result = invocation.callRealMethod(); locked.countDown();
                if (!secondEntered.await(10, TimeUnit.SECONDS)) throw new AssertionError("Segundo cobro no llegó al lock");
                return result;
            }
            if (!locked.await(10, TimeUnit.SECONDS)) throw new AssertionError("Primer cobro no tomó el lock");
            secondEntered.countDown(); return invocation.callRealMethod();
        }).when(orders).lock(eq(order));
        String body = json(Map.of("montoRecibido", "70.00"));
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse());
            var second = pool.submit(() -> mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse());
            var a = first.get(20, TimeUnit.SECONDS); var b = second.get(20, TimeUnit.SECONDS);
            assertThat(a.getStatus()).isEqualTo(201); assertThat(b.getStatus()).isEqualTo(201);
            BigDecimal sum = new BigDecimal(mapper.readTree(a.getContentAsString()).path("pago").path("montoAplicado").asText())
                    .add(new BigDecimal(mapper.readTree(b.getContentAsString()).path("pago").path("montoAplicado").asText()));
            assertThat(sum).isEqualByComparingTo("100.25");
        }
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.deuda").value("0.00"));
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test void simultaneousAnnulmentsCreateOneEvidence() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        UUID payment = pay(order, "50.00");
        var locked = installBarrier(order);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + adminA)).andReturn().getResponse());
            assertThat(locked.await(15, TimeUnit.SECONDS)).isTrue();
            var second = pool.submit(() -> mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + adminA)).andReturn().getResponse());
            var a = first.get(25, TimeUnit.SECONDS); var b = second.get(25, TimeUnit.SECONDS);
            assertThat(java.util.List.of(a.getStatus(), b.getStatus())).containsExactlyInAnyOrder(201, 200);
            assertThat(mapper.readTree(a.getContentAsString())).isEqualTo(mapper.readTree(b.getContentAsString()));
        }
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.deuda").value("100.25"));
        mvc.perform(get("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].estado").value("ANULADO"));
    }

    @Test void annulmentAndPaymentShareTheParentLock() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        UUID payment = pay(order, "50.00");
        var locked = installBarrier(order);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var annul = pool.submit(() -> mvc.perform(put("/api/v1/pagos/" + payment + "/anulacion").header("Authorization", "Bearer " + adminA)).andReturn().getResponse());
            assertThat(locked.await(15, TimeUnit.SECONDS)).isTrue();
            var charge = pool.submit(() -> mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                    .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "70.00")))).andReturn().getResponse());
            assertThat(annul.get(25, TimeUnit.SECONDS).getStatus()).isEqualTo(201);
            var result = charge.get(25, TimeUnit.SECONDS);
            assertThat(result.getStatus()).isEqualTo(201);
            assertThat(mapper.readTree(result.getContentAsString()).path("pago").path("montoAplicado").asText()).isEqualTo("70.00");
        }
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.deuda").value("30.25"));
    }

    private UUID pay(UUID order, String amount) throws Exception {
        var response = mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", amount))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        return UUID.fromString(mapper.readTree(response.getContentAsString()).path("pago").path("id").asText());
    }
    private CountDownLatch installBarrier(UUID order) {
        var locked = new CountDownLatch(1); var secondEntered = new CountDownLatch(1); var calls = new AtomicInteger();
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                Object result = invocation.callRealMethod(); locked.countDown();
                if (!secondEntered.await(15, TimeUnit.SECONDS)) throw new AssertionError("La segunda operación no llegó al lock");
                return result;
            }
            if (!locked.await(15, TimeUnit.SECONDS)) throw new AssertionError("La primera operación no tomó el lock");
            secondEntered.countDown(); return invocation.callRealMethod();
        }).when(orders).lock(eq(order));
        return locked;
    }
}
