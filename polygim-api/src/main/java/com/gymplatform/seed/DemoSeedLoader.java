package com.gymplatform.seed;

import com.gymplatform.controller.dto.MoneyCodec;
import com.gymplatform.model.Membresia;
import com.gymplatform.repository.CatalogoRepository;
import com.gymplatform.repository.GimnasioRepository;
import com.gymplatform.repository.MembresiaRepository;
import com.gymplatform.repository.identity.IdentityRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.service.CatalogoService;
import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.GymProvisioningExecutor;
import com.gymplatform.tenant.IdentityScopeExecutor;
import com.gymplatform.tenant.TenantBusinessExecutor;
import com.gymplatform.tenant.TenantContext;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoSeedLoader {
    private final GimnasioRepository gyms;
    private final GymProvisioningExecutor provisioning;
    private final IdentityRepository identities;
    private final IdentityScopeExecutor identityScope;
    private final CatalogoRepository catalogs;
    private final MembresiaRepository memberships;
    private final TenantBusinessExecutor business;
    private final PasswordEncoder encoder;
    public DemoSeedLoader(GimnasioRepository gyms, GymProvisioningExecutor provisioning, IdentityRepository identities,
            IdentityScopeExecutor identityScope, CatalogoRepository catalogs, MembresiaRepository memberships,
            TenantBusinessExecutor business, PasswordEncoder encoder) {
        this.gyms = gyms; this.provisioning = provisioning; this.identities = identities; this.identityScope = identityScope;
        this.catalogs = catalogs; this.memberships = memberships; this.business = business; this.encoder = encoder;
    }
    public void load(SeedDefinition definition, String adminEmail, String adminPassword, int maxQuantity) {
        definition.validate();
        String email = IdentityService.normalizeEmail(adminEmail);
        IdentityService.validateNewPassword(adminPassword);
        if (maxQuantity < 1) throw new IllegalArgumentException("Máximo inicial inválido");
        for (var gym : definition.gimnasios()) {
            if (gyms.find(gym.id()).isEmpty())
                provisioning.provision(gym.id(), gym.nombre().trim(), gym.logoUrl(), gym.colorPrimario(), gym.adminId(), email, encoder.encode(adminPassword), maxQuantity);
            var admin = identityScope.execute(() -> identities.findById(gym.adminId()).orElseThrow(() -> new IllegalStateException("Administrador de seed no disponible")));
            if (!admin.isHabilitada() || !"ADMIN".equals(admin.getRol()) || !gym.id().equals(admin.getGimnasioId())
                    || !email.equals(admin.getEmail()) || !encoder.matches(adminPassword, admin.getPasswordHash()))
                throw new IllegalStateException("Administrador de seed no disponible");
            var previous = SecurityContextHolder.getContext().getAuthentication();
            try {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        new CurrentAccountPrincipal(admin.getId(), admin.getGimnasioId(), admin.getEmail()), null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
                try (var ignored = TenantContext.open(TenantContext.Scope.TENANT, gym.id())) {
                    business.admin(() -> { loadGym(gym); return null; });
                }
            } finally { SecurityContextHolder.getContext().setAuthentication(previous); }
        }
    }
    private void loadGym(SeedDefinition.Gym gym) {
        for (var item : gym.disciplinas()) ensureNamed(CatalogoRepository.Type.DISCIPLINA, item);
        for (var item : gym.canales()) ensureNamed(CatalogoRepository.Type.CANAL, item);
        for (var item : gym.membresias()) {
            if (memberships.find(item.id()).isEmpty()) {
                catalogs.find(CatalogoRepository.Type.DISCIPLINA, item.disciplinaId()).orElseThrow(() -> new IllegalStateException("Disciplina de seed ausente"));
                memberships.persist(new Membresia(item.id(), item.disciplinaId(), CatalogoService.normalizeName(item.nombre()),
                        MoneyCodec.parse(item.precio()), item.duracion(), item.unidadDuracion()));
            }
        }
    }
    private void ensureNamed(CatalogoRepository.Type type, SeedDefinition.Named item) {
        if (catalogs.find(type, item.id()).isEmpty()) catalogs.persist(type.create(item.id(), CatalogoService.normalizeName(item.nombre())));
    }
}
