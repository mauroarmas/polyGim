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
class PedidoApiIT extends OrderApiSupport {
    @Test void snapshotAndHistoryAreUnaffectedByLaterCatalogAndClientChanges() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 2));
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.precioUnitario").value("100.25"))
                .andExpect(jsonPath("$.total").value("200.50")).andExpect(jsonPath("$.deuda").value("200.50"))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"));
        mvc.perform(put("/api/v1/membresias/" + membershipA).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Nuevo", "precio", "50.00", "duracion", 1, "unidadDuracion", "DIAS"))))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/clientes/" + clientA).header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("dni", "12345678", "nombre", "Nombre nuevo", "apellido", "Perez"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.precioUnitario").value("100.25"))
                .andExpect(jsonPath("$.duracionMembresia").value(2)).andExpect(jsonPath("$.unidadDuracion").value("SEMANAS"));
        mvc.perform(get("/api/v1/pedidos?clienteId=" + clientA).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminB)).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isMethodNotAllowed());
    }
    @Test void cannotAssociateForeignResourcesOrSendCalculatedValues() throws Exception {
        for (var body : List.of(Map.of("clienteId", clientB, "membresiaId", membershipA, "cantidad", 1),
                Map.of("clienteId", clientA, "membresiaId", membershipB, "cantidad", 1))) {
            mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                    .andExpect(status().isNotFound());
        }
        for (var field : List.of("total", "precioUnitario", "detalles", "gimnasioId")) {
            var body = new java.util.HashMap<String, Object>(Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1));
            body.put(field, "forzado");
            mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + adminA).contentType(MediaType.APPLICATION_JSON).content(json(body)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/pedidos?clienteId=" + clientB).header("Authorization", "Bearer " + adminA)).andExpect(status().isNotFound());
    }
    @Test void payingAnExpiredPurchaseDoesNotChangeItsValidityOrPermitEditing() throws Exception {
        UUID order = create("pedidos", adminA, Map.of("clienteId", clientA, "membresiaId", membershipA, "cantidad", 1, "fechaCompra", "2020-01-01"));
        var before = mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vigencia").value("VENCIDA")).andReturn();
        String expiry = mapper.readTree(before.getResponse().getContentAsString()).path("fechaVencimiento").asText();
        mvc.perform(post("/api/v1/pedidos/" + order + "/pagos").header("Authorization", "Bearer " + adminA)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("montoRecibido", "100.25"))))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.vigencia").value("VENCIDA")).andExpect(jsonPath("$.estadoPago").value("PAGADO"))
                .andExpect(jsonPath("$.fechaVencimiento").value(expiry));
        mvc.perform(put("/api/v1/pedidos/" + order).header("Authorization", "Bearer " + adminA)).andExpect(status().isMethodNotAllowed());
    }
}
