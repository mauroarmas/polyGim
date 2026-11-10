package com.gymplatform.controller;
import com.gymplatform.controller.dto.MoneyCodec;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class MoneyCodecTest {
    @Test void acceptsOnlyCanonicalDecimalsWithoutRoundingOrOverflow() {
        assertThat(MoneyCodec.parse("100.25").toPlainString()).isEqualTo("100.25");
        for (String value : new String[] {"01.00", "1", "1.0", "1.001", "-1.00", "100000000000000000.00"})
            assertThatThrownBy(() -> MoneyCodec.parse(value)).isInstanceOf(IllegalArgumentException.class);
    }
}
