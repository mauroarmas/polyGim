package com.gymplatform.integration;

import com.gymplatform.integration.support.PostgresIntegrationSupport;
import com.gymplatform.model.ConfiguracionGimnasio;
import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.model.UsuarioGimnasio;
import com.gymplatform.tenant.GymProvisioningExecutor;
import com.gymplatform.tenant.TenantContext;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TenantIsolationIT extends PostgresIntegrationSupport {
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired GymProvisioningExecutor provisioning;
    @Autowired TenantRepository tenantRepository;

    @Test
    void persistAndQueriesAreIsolatedAcrossReusedConnections() throws Exception {
        UUID gymA = UUID.randomUUID(), gymB = UUID.randomUUID();
        UUID userA = UUID.randomUUID(), userB = UUID.randomUUID();
        createGym(gymA, userA);
        createGym(gymB, userB);
        for (int i = 0; i < 2; i++) {
            assertOwnAndNotForeign(gymA, userA, userB);
            assertOwnAndNotForeign(gymB, userB, userA);
        }
        for (var scope : new TenantContext.Scope[] {TenantContext.Scope.AUTH, TenantContext.Scope.PLATFORM}) {
            try (var ignored = TenantContext.open(scope, TenantContext.SENTINEL)) {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    assertThat(entityManager.find(UsuarioGimnasio.class, userA)).isNull();
                    assertThat(entityManager.createQuery("select count(u) from UsuarioGimnasio u", Long.class).getSingleResult()).isZero();
                });
            }
        }
    }

    private void createGym(UUID gym, UUID user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try {
            provisioning.provision(gym, "Gimnasio " + gym, "https://example.org/logo", "#123456",
                    user, user + "@example.org", UUID.randomUUID().toString(), 12);
        } finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void cannotAssociateAnAccountFromAnotherGymOrForceTenantAtPersist() throws Exception {
        UUID gymA = UUID.randomUUID(), gymB = UUID.randomUUID();
        UUID userA = UUID.randomUUID(), unlinkedAccount = UUID.randomUUID();
        createGym(gymA, userA);
        createGym(gymB, UUID.randomUUID());
        try (var ignored = TenantContext.open(TenantContext.Scope.AUTH, TenantContext.SENTINEL)) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    entityManager.persist(new CuentaAcceso(unlinkedAccount, unlinkedAccount + "@example.org",
                            UUID.randomUUID().toString(), "RECEPCION", gymA)));
        }
        try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, gymB)) {
            assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                entityManager.persist(new UsuarioGimnasio(unlinkedAccount));
                entityManager.flush();
            })).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                var forced = new UsuarioGimnasio(unlinkedAccount);
                ReflectionTestUtils.setField(forced, "gimnasioId", gymA);
                entityManager.persist(forced);
                entityManager.flush();
            })).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void provisioningDoesNotOpenAnExistingGym() {
        UUID gym = UUID.randomUUID();
        createGym(gym, UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(UUID.randomUUID(), null, "platform@example.org"), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try {
            assertThatThrownBy(() -> provisioning.provision(gym, "Repetido", "https://example.org/logo", "#123456",
                            UUID.randomUUID(), UUID.randomUUID() + "@example.org", UUID.randomUUID().toString(), 12))
                    .isInstanceOf(RuntimeException.class);
        } finally { SecurityContextHolder.clearContext(); }
    }

    private void assertOwnAndNotForeign(UUID gym, UUID own, UUID other) throws Exception {
        try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, gym)) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    new CurrentAccountPrincipal(own, gym, own + "@example.org"), null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                var ownUser = entityManager.find(UsuarioGimnasio.class, own);
                assertThat(ownUser).isNotNull();
                assertThat(ownUser.getGimnasioId()).isEqualTo(gym);
                assertThat(entityManager.find(UsuarioGimnasio.class, other)).isNull();
                assertThat(entityManager.createQuery("select count(u) from UsuarioGimnasio u", Long.class).getSingleResult()).isEqualTo(1L);
                assertThat(entityManager.createQuery("select u from UsuarioGimnasio u", UsuarioGimnasio.class).getResultList())
                        .extracting(UsuarioGimnasio::getId).containsExactly(own);
                assertThat(tenantRepository.find(UsuarioGimnasio.class, own)).isPresent();
                assertThat(tenantRepository.find(UsuarioGimnasio.class, other)).isEmpty();
                assertThat(tenantRepository.count(UsuarioGimnasio.class)).isEqualTo(1);
                assertThat(tenantRepository.list(UsuarioGimnasio.class, 0, 20)).extracting(UsuarioGimnasio::getId).containsExactly(own);
                assertThatThrownBy(() -> tenantRepository.find(CuentaAcceso.class, own))
                        .isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class)
                        .hasCauseInstanceOf(IllegalArgumentException.class);
            });
        } finally { SecurityContextHolder.clearContext(); }
        assertThatThrownBy(TenantContext::require).isInstanceOf(IllegalStateException.class);
    }
}
