package com.gymplatform.service;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.identity.IdentityRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityServiceTest {
    @Test
    void normalizesEmailAndLooksUpGlobally() {
        var repository = mock(IdentityRepository.class);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var service = new IdentityService(repository, encoder);
        var account = mock(CuentaAcceso.class);
        String password = UUID.randomUUID().toString();
        when(account.getPasswordHash()).thenReturn(encoder.encode(password));
        when(account.isHabilitada()).thenReturn(true);
        when(repository.findByEmail("user@example.org")).thenReturn(Optional.of(account));
        assertThat(service.authenticate("  USER@Example.ORG ", password)).isSameAs(account);
        verify(repository).findByEmail("user@example.org");
    }

    @Test
    void rejectsWrongPasswordAndDisabledAccountGenerically() {
        var repository = mock(IdentityRepository.class);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var service = new IdentityService(repository, encoder);
        var account = mock(CuentaAcceso.class);
        var password = UUID.randomUUID().toString();
        when(account.getPasswordHash()).thenReturn(encoder.encode(password));
        when(account.isHabilitada()).thenReturn(false);
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.authenticate("user@example.org", password)).isInstanceOf(BadCredentialsException.class);
        when(account.isHabilitada()).thenReturn(true);
        assertThatThrownBy(() -> service.authenticate("user@example.org", UUID.randomUUID().toString())).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void passwordPolicyChecksCharactersAndUtf8BytesWithoutTruncation() {
        assertThatThrownBy(() -> IdentityService.validateNewPassword("a".repeat(11))).isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> IdentityService.validateNewPassword("é".repeat(36))).doesNotThrowAnyException();
        assertThatThrownBy(() -> IdentityService.validateNewPassword("é".repeat(37))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdentityService.validateNewPassword("a".repeat(73))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eachRequestRereadsAccountAndRejectsDisabledIdentity() {
        var repository = mock(IdentityRepository.class);
        var service = new IdentityService(repository, PasswordEncoderFactories.createDelegatingPasswordEncoder());
        var account = mock(CuentaAcceso.class);
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(account));
        when(account.isHabilitada()).thenReturn(true, false);
        assertThat(service.requireEnabled(id)).isSameAs(account);
        assertThatThrownBy(() -> service.requireEnabled(id)).isInstanceOf(BadCredentialsException.class);
        verify(repository, times(2)).findById(id);
    }
}
