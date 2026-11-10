package com.gymplatform.tenant;

import java.util.UUID;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class GymTenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {
    @Override public UUID resolveCurrentTenantIdentifier() { return TenantContext.require().tenantId(); }
    @Override public boolean validateExistingCurrentSessions() { return true; }
    @Override public boolean isRoot(UUID tenantId) { return false; }
}
