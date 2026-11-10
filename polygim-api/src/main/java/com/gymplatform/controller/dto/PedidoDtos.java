package com.gymplatform.controller.dto;

import com.gymplatform.model.Pedido;
import com.gymplatform.service.policy.VigenciaPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class PedidoDtos {
    private PedidoDtos() { }
    public record Create(@NotNull UUID clienteId, @NotNull UUID membresiaId, @NotNull @Min(1) Integer cantidad,
                         LocalDate fechaCompra, UUID pedidoAnteriorId) { }
    public record Order(UUID id, UUID clienteId, UUID membresiaId, int cantidad, String precioUnitario,
                        int duracionMembresia, String unidadDuracion, String total, LocalDate fechaCompra,
                        LocalDate fechaBaseVencimiento, LocalDate fechaVencimiento, UUID pedidoAnteriorId,
                        String deuda, String estadoPago, String vigencia) { }
    public static Order from(Pedido order, LocalDate today, BigDecimal debt) {
        var detail = order.getDetalle();
        return new Order(order.getId(), order.getClienteId(), detail.getMembresiaId(), detail.getCantidad(),
                detail.getPrecioUnitario().toPlainString(), detail.getDuracionMembresia(), detail.getUnidadDuracion(),
                order.getTotal().toPlainString(), order.getFechaCompra(), order.getFechaBaseVencimiento(), order.getFechaVencimiento(),
                order.getPedidoAnteriorId(), debt.toPlainString(), debt.signum() == 0 ? "PAGADO" : "PENDIENTE",
                VigenciaPolicy.status(order.getFechaVencimiento(), today));
    }
}
