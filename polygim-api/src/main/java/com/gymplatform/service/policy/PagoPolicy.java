package com.gymplatform.service.policy;

import com.gymplatform.service.BusinessConflictException;
import java.math.BigDecimal;

public final class PagoPolicy {
    private PagoPolicy() { }
    public record Result(BigDecimal aplicado, BigDecimal vuelto, BigDecimal deudaRestante, String estadoPago) { }
    public static Result apply(BigDecimal total, BigDecimal pagado, BigDecimal recibido) {
        if (!valid(total) || total.signum() <= 0 || !valid(pagado) || pagado.signum() < 0 || pagado.compareTo(total) > 0
                || !valid(recibido) || recibido.signum() <= 0) throw new IllegalArgumentException("Importe inválido");
        BigDecimal deuda = total.subtract(pagado);
        if (deuda.signum() == 0) throw new BusinessConflictException();
        BigDecimal aplicado = recibido.min(deuda);
        BigDecimal restante = deuda.subtract(aplicado);
        return new Result(aplicado, recibido.subtract(aplicado), restante, restante.signum() == 0 ? "PAGADO" : "PENDIENTE");
    }
    private static boolean valid(BigDecimal value) { return value != null && value.scale() == 2 && value.precision() <= 19; }
}
