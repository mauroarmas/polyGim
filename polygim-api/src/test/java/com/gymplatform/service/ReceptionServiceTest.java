package com.gymplatform.service;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.UsuarioGimnasioRepository;
import com.gymplatform.tenant.TenantBusinessExecutor;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReceptionServiceTest {
    @Test
    void createsReceptionWithNormalizedEmailAndEncodedPassword() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var gym = UUID.randomUUID();
        when(repository.createReception(any(), anyString(), anyString())).thenAnswer(call ->
                new CuentaAcceso(call.getArgument(0), call.getArgument(1), call.getArgument(2), "RECEPCION", gym));
        var service = new ReceptionService(repository, executor, encoder);
        String password = UUID.randomUUID().toString();
        var user = service.create(" USER@Example.ORG ", password);
        var hash = ArgumentCaptor.forClass(String.class);
        verify(repository).createReception(eq(user.id()), eq("user@example.org"), hash.capture());
        assertThat(encoder.matches(password, hash.getValue())).isTrue();
        assertThat(user.rol()).isEqualTo("RECEPCION");
        assertThat(user.gimnasioId()).isEqualTo(gym);
    }

    @Test
    void disablingMissingOrForeignProfileReturnsNotFound() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID id = UUID.randomUUID();
        when(repository.disableReception(id)).thenReturn(Optional.empty());
        var service = new ReceptionService(repository, executor, PasswordEncoderFactories.createDelegatingPasswordEncoder());
        assertThatThrownBy(() -> service.disable(id)).isInstanceOf(java.util.NoSuchElementException.class);
        verify(repository).disableReception(id);
    }

    @Test
    void rejectedPasswordNeverCreatesAnAccount() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var service = new ReceptionService(repository, mock(TenantBusinessExecutor.class),
                PasswordEncoderFactories.createDelegatingPasswordEncoder());
        assertThatThrownBy(() -> service.create("user@example.org", "a".repeat(11))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void duplicateGlobalEmailIsNotSilentlyReplaced() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        when(repository.createReception(any(), anyString(), anyString()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));
        var service = new ReceptionService(repository, executor, PasswordEncoderFactories.createDelegatingPasswordEncoder());
        assertThatThrownBy(() -> service.create("user@example.org", UUID.randomUUID().toString()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        verify(repository, times(1)).createReception(any(), anyString(), anyString());
    }

    @Test
    void disablingAnAlreadyDisabledAccountIsIdempotent() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        UUID id = UUID.randomUUID();
        var account = new CuentaAcceso(id, id + "@example.org", UUID.randomUUID().toString(), "RECEPCION", UUID.randomUUID());
        account.deshabilitar();
        when(repository.disableReception(id)).thenReturn(Optional.of(account));
        var service = new ReceptionService(repository, executor, PasswordEncoderFactories.createDelegatingPasswordEncoder());
        assertThat(service.disable(id).habilitada()).isFalse();
        assertThat(service.disable(id).habilitada()).isFalse();
    }
    @Test
    void directoryUsesAdminScopeAndReturnsOnlyPublicAccountFields() {
        var repository = mock(UsuarioGimnasioRepository.class);
        var executor = mock(TenantBusinessExecutor.class);
        when(executor.admin(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        var account = new CuentaAcceso(UUID.randomUUID(), "staff@example.org", UUID.randomUUID().toString(), "RECEPCION", UUID.randomUUID());
        when(repository.listReception(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(account)));
        var service = new ReceptionService(repository, executor, PasswordEncoderFactories.createDelegatingPasswordEncoder());
        var result = service.list(0, 20);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().email()).isEqualTo(account.getEmail());
        verify(executor).admin(any());
        assertThatThrownBy(() -> service.list(-1, 20)).isInstanceOf(IllegalArgumentException.class);
    }
}
