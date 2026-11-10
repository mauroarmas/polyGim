package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PagoApiIT extends OrderApiSupport {
    @Test void partialExcessAndHistoryKeepTheOrderDatesAndRespectTenants() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 2));
        var before = mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk()).andReturn();
        String expiry = mapper.readTree(before.getResponse().getContentAsString()).path("fechaVencimiento").asText();
        UUID receptionist = create("usuarios-recepcion", adminA, Map.of("email", UUID.randomUUID() + "@example.org", "password", UUID.randomUUID().toString()));
        String receptionToken = tokens.issue(receptionist);
        var first = mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + receptionToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "50.25"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.pago.montoAplicado").value("50.25"))
                .andExpect(jsonPath("$.pago.registradoPorId").value(receptionist.toString()))
                .andExpect(jsonPath("$.deudaRestante").value("150.25")).andExpect(jsonPath("$.estadoPago").value("PENDIENTE")).andReturn();
        UUID payment = UUID.fromString(mapper.readTree(first.getResponse().getContentAsString()).path("pago").path("id").asText());
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "200.00"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.pago.montoAplicado").value("150.25"))
                .andExpect(jsonPath("$.vuelto").value("49.75")).andExpect(jsonPath("$.deudaRestante").value("0.00"))
                .andExpect(jsonPath("$.estadoPago").value("PAGADO"));
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaVencimiento").value(expiry)).andExpect(jsonPath("$.estadoPago").value("PAGADO"));
        mvc.perform(get("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/pagos/" + payment).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/pagos/" + payment).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminB)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "1.00")))).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "1.00")))).andExpect(status().isConflict());
    }
    @Test void paymentAmountsAndServerAssignedFieldsAreStrict() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
        for (Object invalid : List.of("0.00", "-1.00", "1.001", new java.math.BigDecimal("1.00"))) {
            mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                            .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", invalid))))
                    .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("montoRecibido", "1.00", "registradoPorId", accountB))))
                .andExpect(status().isBadRequest());
    }
}
