package com.gymplatform.controller.dto;

import com.gymplatform.model.Pedido;
import com.gymplatform.service.policy.VigenciaPolicy;
import com.gymplatform.service.policy.PagoPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public final class PedidoDtos {
    private PedidoDtos() { }
    @Schema(name = "OrderCreate", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Create(@NotNull UUID clienteId, @NotNull UUID membresiaId, @NotNull @Min(1) Integer cantidad,
                         LocalDate fechaCompra, UUID pedidoAnteriorId) { }
    @Schema(requiredProperties = {"id", "clienteId", "membresiaId", "cantidad", "precioUnitario", "duracionMembresia", "unidadDuracion", "total", "fechaCompra", "fechaBaseVencimiento", "fechaVencimiento", "deuda", "estadoPago", "vigencia"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Order(UUID id, UUID clienteId, UUID membresiaId, int cantidad,
                        @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String precioUnitario,
                        int duracionMembresia, @Schema(allowableValues = {"DIAS", "SEMANAS", "MESES"}) String unidadDuracion,
                        @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String total, LocalDate fechaCompra,
                        LocalDate fechaBaseVencimiento, LocalDate fechaVencimiento, UUID pedidoAnteriorId,
                        @Schema(pattern = "^(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String deuda,
                        @Schema(allowableValues = {"PENDIENTE", "PAGADO"}) String estadoPago,
                        @Schema(allowableValues = {"VIGENTE", "VENCIDA"}) String vigencia) { }
    public static Order from(Pedido order, LocalDate today, BigDecimal debt) {
        var detail = order.getDetalle();
        return new Order(order.getId(), order.getClienteId(), detail.getMembresiaId(), detail.getCantidad(),
                detail.getPrecioUnitario().toPlainString(), detail.getDuracionMembresia(), detail.getUnidadDuracion(),
                order.getTotal().toPlainString(), order.getFechaCompra(), order.getFechaBaseVencimiento(), order.getFechaVencimiento(),
                order.getPedidoAnteriorId(), debt.toPlainString(), PagoPolicy.paymentState(debt),
                VigenciaPolicy.status(order.getFechaVencimiento(), today));
    }
}
