package com.gymplatform.controller.dto;
import java.time.Instant;
import java.util.UUID;
import com.gymplatform.model.Pago;
import com.gymplatform.model.AnulacionPago;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
public final class PagoDtos {
    private PagoDtos() { }
    public record Annulment(UUID id, UUID pagoId, UUID anuladoPorId, Instant fechaAnulacion) { }
    public record Payment(UUID id, UUID pedidoId, String montoAplicado, String estado, UUID registradoPorId, Instant fechaRegistro, Annulment anulacion) { }
    public record Result(Payment pago, String vuelto, String deudaRestante, String estadoPago) { }
    public record Create(@NotNull @JsonDeserialize(using = MoneyCodec.StrictString.class) String montoRecibido) { }
    public static Annulment annulment(AnulacionPago item) {
        return item == null ? null : new Annulment(item.getId(), item.getPagoId(), item.getAnuladoPorId(), item.getCreatedAt());
    }
    public static Payment payment(Pago payment, AnulacionPago annulment) {
        return new Payment(payment.getId(), payment.getPedidoId(), payment.getMontoAplicado().toPlainString(),
                annulment == null ? "REGISTRADO" : "ANULADO", payment.getRegistradoPorId(), payment.getCreatedAt(), annulment(annulment));
    }
}
