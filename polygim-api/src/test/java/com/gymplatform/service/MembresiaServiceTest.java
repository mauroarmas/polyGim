package com.gymplatform.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MembresiaServiceTest {
    @Test
    void priceIsPositiveExactAndFitsDatabase() {
        assertThat(MembresiaService.validatePrice(new BigDecimal("100.25"))).isEqualByComparingTo("100.25");
        for (String value : new String[] {"0.00", "-1.00", "1.001", "100000000000000000.00"}) {
            assertThatThrownBy(() -> MembresiaService.validatePrice(new BigDecimal(value))).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> MembresiaService.validatePrice(null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void durationRequiresPositiveQuantityAndKnownUnit() {
        for (String unit : new String[] {"DIAS", "SEMANAS", "MESES"})
            assertThatCode(() -> MembresiaService.validateDuration(1, unit)).doesNotThrowAnyException();
        assertThatThrownBy(() -> MembresiaService.validateDuration(0, "DIAS")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MembresiaService.validateDuration(1, "ANIOS")).isInstanceOf(IllegalArgumentException.class);
    }
}
