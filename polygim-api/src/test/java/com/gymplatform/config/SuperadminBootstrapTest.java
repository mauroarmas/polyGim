package com.gymplatform.config;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.repository.identity.IdentityRepository;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuperadminBootstrapTest {
    @Test
    void incompleteConfigurationFailsAndEmptyConfigurationDoesNothing() {
        var scopes = mock(IdentityScopeExecutor.class);
        var repository = mock(IdentityRepository.class);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        assertThatCode(() -> new SuperadminBootstrap(scopes, repository, encoder, "", "").run(null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new SuperadminBootstrap(scopes, repository, encoder, "user@example.org", "").run(null))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(scopes, repository);
    }
    @Test
    void createsOnceWithoutOverwritingExistingAccount() {
        var scopes = mock(IdentityScopeExecutor.class);
        var repository = mock(IdentityRepository.class);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        when(scopes.execute(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
        when(repository.superadminExists()).thenReturn(false, true);
        var bootstrap = new SuperadminBootstrap(scopes, repository, encoder, " USER@EXAMPLE.ORG ", UUID.randomUUID().toString());
        bootstrap.run(null);
        bootstrap.run(null);
        var capture = org.mockito.ArgumentCaptor.forClass(CuentaAcceso.class);
        verify(repository, times(1)).persist(capture.capture());
        assertThat(capture.getValue().getEmail()).isEqualTo("user@example.org");
        assertThat(capture.getValue().getRol()).isEqualTo("SUPERADMIN");
        assertThat(capture.getValue().getGimnasioId()).isNull();
    }
}
