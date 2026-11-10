package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnulacionApiIT extends OrderApiSupport {
    private UUID pay(UUID order, String amount) throws Exception {
        var response = mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", amount))))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(response.getResponse().getContentAsString()).path("pago").path("id").asText());
    }
    @Test void annulmentReopensDebtPreservesEvidenceAndIsIdempotent() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 2));
        UUID firstPayment = pay(order, "50.25"), secondPayment = pay(order, "200.00");
        var original = mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk()).andReturn();
        String expiry = mapper.readTree(original.getResponse().getContentAsString()).path("fechaVencimiento").asText();
        UUID receptionist = create("usuarios-recepcion", adminA, Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()));
        mvc.perform(put("/api/v1/pagos/" + secondPayment + "/anulacion").header("Authorization", "Bearer " + tokens.issue(receptionist)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/pagos/" + secondPayment + "/anulacion").header("Authorization", "Bearer " + adminB))
                .andExpect(status().isNotFound());
        var first = mvc.perform(put("/api/v1/pagos/" + secondPayment + "/anulacion").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.anuladoPorId").value(accountA.toString())).andReturn();
        var evidence = mapper.readTree(first.getResponse().getContentAsString());
        var repeated = mvc.perform(put("/api/v1/pagos/" + secondPayment + "/anulacion").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andReturn();
        assertThat(mapper.readTree(repeated.getResponse().getContentAsString())).isEqualTo(evidence);
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.deuda").value("150.25")).andExpect(jsonPath("$.estadoPago").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaVencimiento").value(expiry));
        mvc.perform(get("/api/v1/pagos/" + secondPayment).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADO")).andExpect(jsonPath("$.anulacion.id").value(evidence.path("id").asText()));
        mvc.perform(get("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(put("/api/v1/pagos/" + firstPayment + "/anulacion").header("Authorization", "Bearer " + adminA)).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isMethodNotAllowed());
    }
}
