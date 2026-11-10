package com.gymplatform.service;

import com.gymplatform.service.policy.PagoPolicy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PagoPolicyTest {
    @Test void partialExactAndExcessPaymentsNeverOverpay() {
        var first = PagoPolicy.apply(new BigDecimal("200.50"), new BigDecimal("0.00"), new BigDecimal("50.25"));
        assertThat(first.aplicado()).isEqualByComparingTo("50.25");
        assertThat(first.deudaRestante()).isEqualByComparingTo("150.25");
        assertThat(first.estadoPago()).isEqualTo("PENDIENTE");
        var second = PagoPolicy.apply(new BigDecimal("200.50"), new BigDecimal("50.25"), new BigDecimal("200.00"));
        assertThat(second.aplicado()).isEqualByComparingTo("150.25");
        assertThat(second.vuelto()).isEqualByComparingTo("49.75");
        assertThat(second.deudaRestante()).isEqualByComparingTo("0.00");
        assertThat(second.estadoPago()).isEqualTo("PAGADO");
        var exact = PagoPolicy.apply(new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));
        assertThat(exact.vuelto()).isEqualByComparingTo("0.00");
    }
    @Test void rejectsInvalidAmountsAndPaymentOfSettledDebt() {
        assertThatThrownBy(() -> PagoPolicy.apply(new BigDecimal("10.00"), new BigDecimal("10.00"), new BigDecimal("1.00"))).isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> PagoPolicy.apply(new BigDecimal("10.00"), new BigDecimal("0.00"), new BigDecimal("0.00"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PagoPolicy.apply(new BigDecimal("10.00"), new BigDecimal("0.00"), new BigDecimal("1.001"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PagoPolicy.apply(new BigDecimal("10.00"), new BigDecimal("11.00"), new BigDecimal("1.00"))).isInstanceOf(IllegalArgumentException.class);
    }
}
