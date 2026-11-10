package com.gymplatform.controller.dto;
import java.time.Instant;
import java.util.UUID;
import com.gymplatform.model.Pago;
import com.gymplatform.model.AnulacionPago;
import com.gymplatform.service.policy.PagoPolicy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
public final class PagoDtos {
    private PagoDtos() { }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(requiredProperties = {"id", "pedidoId", "montoAplicado", "estado", "registradoPorId", "fechaRegistro"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Payment(UUID id, UUID pedidoId, @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String montoAplicado,
                          @Schema(allowableValues = {"REGISTRADO", "ANULADO"}) String estado, UUID registradoPorId, Instant fechaRegistro, AnulacionDtos.Evidence anulacion) { }
    @Schema(name = "PaymentResult", requiredProperties = {"pago", "vuelto", "deudaRestante", "estadoPago"}, additionalProperties = Schema.AdditionalPropertiesValue.FALSE) public record Result(Payment pago,
            @Schema(pattern = "^(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String vuelto,
            @Schema(pattern = "^(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") String deudaRestante,
            @Schema(allowableValues = {"PENDIENTE", "PAGADO"}) String estadoPago) { }
    @Schema(name = "PaymentCreate", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Create(@NotNull @Schema(pattern = "^(?!0\\.00$)(0|[1-9][0-9]{0,16})\\.[0-9]{2}$") @JsonDeserialize(using = MoneyCodec.StrictString.class) String montoRecibido) { }
    public static AnulacionDtos.Evidence annulment(AnulacionPago item) {
        return item == null ? null : new AnulacionDtos.Evidence(item.getId(), item.getPagoId(), item.getAnuladoPorId(), item.getCreatedAt());
    }
    public static Payment payment(Pago payment, AnulacionPago annulment) {
        return new Payment(payment.getId(), payment.getPedidoId(), payment.getMontoAplicado().toPlainString(),
                PagoPolicy.movementState(annulment != null), payment.getRegistradoPorId(), payment.getCreatedAt(), annulment(annulment));
    }
}
