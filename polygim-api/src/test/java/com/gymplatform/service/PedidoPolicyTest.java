package com.gymplatform.service;

import com.gymplatform.service.policy.PedidoPolicy;
import com.gymplatform.service.policy.VigenciaPolicy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PedidoPolicyTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
    @Test void calculatesExactSnapshotTotalAndRejectsInvalidQuantitiesOrOverflow() {
        assertThat(PedidoPolicy.total(new BigDecimal("100.25"), 2, 12)).isEqualByComparingTo("200.50");
        assertThatThrownBy(() -> PedidoPolicy.total(new BigDecimal("100.25"), 0, 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PedidoPolicy.total(new BigDecimal("100.25"), 13, 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PedidoPolicy.total(new BigDecimal("1.001"), 1, 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PedidoPolicy.total(new BigDecimal("99999999999999999.99"), 2, 12)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void defaultsTodayAcceptsPastAndRejectsFuture() {
        assertThat(PedidoPolicy.purchaseDate(null, CLOCK)).isEqualTo("2026-11-10");
        assertThat(PedidoPolicy.purchaseDate(LocalDate.parse("2026-10-01"), CLOCK)).isEqualTo("2026-10-01");
        assertThatThrownBy(() -> PedidoPolicy.purchaseDate(LocalDate.parse("2026-11-11"), CLOCK)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void calculatesDaysWeeksMonthsLeapYearsAndCalendarOverflow() {
        assertThat(PedidoPolicy.expiry(LocalDate.parse("2026-11-10"), 3, 2, "SEMANAS")).isEqualTo("2026-12-22");
        assertThat(PedidoPolicy.expiry(LocalDate.parse("2024-01-31"), 1, 1, "MESES")).isEqualTo("2024-02-29");
        assertThat(PedidoPolicy.expiry(LocalDate.parse("2026-11-10"), 2, 3, "DIAS")).isEqualTo("2026-11-16");
        assertThatThrownBy(() -> PedidoPolicy.expiry(LocalDate.MAX, 1, 1, "DIAS")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PedidoPolicy.expiry(LocalDate.of(5874897, 12, 31), 1, 1, "DIAS")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PedidoPolicy.expiry(LocalDate.now(CLOCK), 1, 1, "OTRA")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void renewalUsesExpiryWhenStillValidAndExpiryDayIsInclusive() {
        var purchase = LocalDate.parse("2026-11-10");
        assertThat(PedidoPolicy.renewalBase(purchase, LocalDate.parse("2026-11-30"))).isEqualTo("2026-11-30");
        assertThat(PedidoPolicy.renewalBase(purchase, LocalDate.parse("2026-11-09"))).isEqualTo(purchase);
        assertThat(PedidoPolicy.renewalBase(purchase, purchase)).isEqualTo(purchase);
        assertThat(VigenciaPolicy.status(purchase, purchase)).isEqualTo("VIGENTE");
        assertThat(VigenciaPolicy.status(purchase, purchase.plusDays(1))).isEqualTo("VENCIDA");
    }
}
