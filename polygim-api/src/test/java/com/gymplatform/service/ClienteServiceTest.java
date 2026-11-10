package com.gymplatform.service;

import com.gymplatform.controller.dto.ClienteDtos;
import com.gymplatform.repository.CatalogoRepository;
import com.gymplatform.repository.ClienteRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteServiceTest {
    @Test void dniRemainsTextAndMustHaveSevenOrEightDigits() {
        assertThat(ClienteService.validateDni("0123456")).isEqualTo("0123456");
        assertThat(ClienteService.validateDni("12345678")).isEqualTo("12345678");
        for (String invalid : new String[] {"123456", "123456789", "12A4567", " 1234567"})
            assertThatThrownBy(() -> ClienteService.validateDni(invalid)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void createsMinimumClientWithoutInventingOptionalFields() {
        var repository = mock(ClienteRepository.class);
        var catalogs = mock(CatalogoRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.business(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        var clock = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
        var service = new ClienteService(repository, catalogs, executor, clock);
        var result = service.create(new ClienteDtos.Write("0123456", " Ana ", " Perez ", null, null, null, null));
        assertThat(result.dni()).isEqualTo("0123456");
        assertThat(result.nombre()).isEqualTo("Ana");
        assertThat(result.apellido()).isEqualTo("Perez");
        assertThat(result.fechaIngreso()).isEqualTo("2026-11-10");
        assertThat(result.telefono()).isNull();
        assertThat(result.canalLlegadaId()).isNull();
        assertThat(result.direccion()).isNull();
        assertThat(result.localidadId()).isNull();
        verifyNoInteractions(catalogs);
    }
    @Test void rejectsBlankRequiredNames() {
        var repository = mock(ClienteRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        var service = new ClienteService(repository, mock(CatalogoRepository.class), executor, Clock.systemUTC());
        assertThatThrownBy(() -> service.create(new ClienteDtos.Write("12345678", " ", "Perez", null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}
