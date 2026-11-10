package com.gymplatform.tenant;

import java.util.UUID;
import com.gymplatform.security.CurrentAccountPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.*;

class TenantScopeTest {
    @Test
    void absentContextFailsClosedAndSentinelIsNeverRoot() {
        var resolver = new GymTenantIdentifierResolver();
        assertThatThrownBy(resolver::resolveCurrentTenantIdentifier).isInstanceOf(IllegalStateException.class);
        assertThat(resolver.isRoot(TenantContext.SENTINEL)).isFalse();
    }

    @Test
    void authAndPlatformCannotBeUsedForBusiness() throws Exception {
        for (var scope : new TenantContext.Scope[] {TenantContext.Scope.AUTH, TenantContext.Scope.PLATFORM}) {
            try (var ignored = TenantContext.open(scope, TenantContext.SENTINEL)) {
                assertThat(new GymTenantIdentifierResolver().resolveCurrentTenantIdentifier()).isEqualTo(TenantContext.SENTINEL);
                assertThatThrownBy(() -> new TenantGuard().requireBusiness()).isInstanceOf(RuntimeException.class);
            }
        }
    }

    @Test
    void businessScopeRejectsSentinelAndNestedTenantSwitch() throws Exception {
        assertThatThrownBy(() -> TenantContext.open(TenantContext.Scope.TENANT, TenantContext.SENTINEL))
                .isInstanceOf(IllegalArgumentException.class);
        var first = UUID.randomUUID();
        try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, first)) {
            assertThat(new GymTenantIdentifierResolver().resolveCurrentTenantIdentifier()).isEqualTo(first);
            assertThatThrownBy(() -> TenantContext.open(TenantContext.Scope.TENANT, UUID.randomUUID()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void transactionPreventsNewScopeAndFinallyAlwaysClearsContext() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            assertThatThrownBy(() -> TenantContext.open(TenantContext.Scope.TENANT, UUID.randomUUID()))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
        assertThatThrownBy(() -> {
            try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, UUID.randomUUID())) {
                throw new IllegalArgumentException("test");
            }
        }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(TenantContext::require).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void businessGuardRequiresTenantAndGymRole() throws Exception {
        var gym = UUID.randomUUID();
        try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, gym)) {
            assertThatThrownBy(() -> new TenantGuard().requireBusiness()).isInstanceOf(RuntimeException.class);
            var auth = new UsernamePasswordAuthenticationToken(new CurrentAccountPrincipal(UUID.randomUUID(), gym, "test@example.org"), null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            assertThatCode(() -> new TenantGuard().requireBusiness()).doesNotThrowAnyException();
            assertThatCode(() -> new TenantGuard().requireAdmin()).doesNotThrowAnyException();
            var receptionist = new UsernamePasswordAuthenticationToken(new CurrentAccountPrincipal(UUID.randomUUID(), gym, "test@example.org"), null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_RECEPCION")));
            SecurityContextHolder.getContext().setAuthentication(receptionist);
            assertThatThrownBy(() -> new TenantGuard().requireAdmin()).isInstanceOf(RuntimeException.class);
            var foreign = new UsernamePasswordAuthenticationToken(new CurrentAccountPrincipal(UUID.randomUUID(), UUID.randomUUID(), "test@example.org"), null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(foreign);
            assertThatThrownBy(() -> new TenantGuard().requireBusiness()).isInstanceOf(RuntimeException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
