package com.gymplatform.controller;

import com.gymplatform.controller.dto.PageResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PageResponseTest {
    @Test
    void defaultsAndLimitsAreEnforcedWithoutOverflow() {
        assertThat(PageResponse.request(null, null).getPageSize()).isEqualTo(20);
        assertThat(PageResponse.request(1, 100).getOffset()).isEqualTo(100);
        assertThatThrownBy(() -> PageResponse.request(-1, 20)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResponse.request(0, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResponse.request(Integer.MAX_VALUE, 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
