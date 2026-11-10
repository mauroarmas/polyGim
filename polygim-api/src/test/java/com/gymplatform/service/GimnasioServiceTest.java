package com.gymplatform.service;

import com.gymplatform.controller.dto.GimnasioDtos;
import com.gymplatform.repository.GimnasioRepository;
import com.gymplatform.tenant.GymProvisioningExecutor;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GimnasioServiceTest {
    @Test
    void createsGymAndAdministratorWithNormalizedEmailAndConfiguredLimit() {
        var provisioning = mock(GymProvisioningExecutor.class);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var service = new GimnasioService(provisioning, mock(GimnasioRepository.class), encoder, 13);
        var password = UUID.randomUUID().toString();
        var request = new GimnasioDtos.CreateRequest(" Gimnasio ", "https://example.org/logo", "#112233",
                new GimnasioDtos.NewAccount(" ADMIN@Example.Org ", password));
        var result = service.create(request);
        var hash = ArgumentCaptor.forClass(String.class);
        verify(provisioning).provision(eq(result.gimnasio().id()), eq("Gimnasio"), eq("https://example.org/logo"), eq("#112233"),
                eq(result.administradorInicial().id()), eq("admin@example.org"), hash.capture(), eq(13));
        assertThat(encoder.matches(password, hash.getValue())).isTrue();
        assertThat(result.administradorInicial().gimnasioId()).isEqualTo(result.gimnasio().id());
        assertThat(result.administradorInicial().rol()).isEqualTo("ADMIN");
    }
    @Test
    void invalidPasswordFailsBeforeProvisioning() {
        var provisioning = mock(GymProvisioningExecutor.class);
        var service = new GimnasioService(provisioning, mock(GimnasioRepository.class),
                PasswordEncoderFactories.createDelegatingPasswordEncoder(), 2);
        var request = new GimnasioDtos.CreateRequest("Gym", "https://example.org/logo", "#112233",
                new GimnasioDtos.NewAccount("user@example.org", "a".repeat(11)));
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(provisioning);
    }
    @Test
    void aProvisioningFailureIsNotReportedAsSuccess() {
        var provisioning = mock(GymProvisioningExecutor.class);
        doThrow(new IllegalStateException("failure")).when(provisioning)
                .provision(any(), any(), any(), any(), any(), any(), any(), anyInt());
        var service = new GimnasioService(provisioning, mock(GimnasioRepository.class),
                PasswordEncoderFactories.createDelegatingPasswordEncoder(), 2);
        var request = new GimnasioDtos.CreateRequest("Gym", "https://example.org/logo", "#112233",
                new GimnasioDtos.NewAccount("user@example.org", UUID.randomUUID().toString()));
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalStateException.class);
    }
}
