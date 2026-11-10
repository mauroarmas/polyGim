package com.gymplatform.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CatalogoServiceTest {
    @Test
    void trimsNamesAndRejectsBlankOrOverlongValues() {
        assertThat(CatalogoService.normalizeName("  Entrenamiento  ")).isEqualTo("Entrenamiento");
        assertThatThrownBy(() -> CatalogoService.normalizeName("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CatalogoService.normalizeName(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CatalogoService.normalizeName("a".repeat(101))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void maximumMustBePositive() {
        assertThat(ConfiguracionService.validateMaximum(50)).isEqualTo(50);
        assertThatThrownBy(() -> ConfiguracionService.validateMaximum(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ConfiguracionService.validateMaximum(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
