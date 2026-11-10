package com.gymplatform.integration.support;

import java.util.UUID;

/** Identificadores independientes; el alta real se hace por el flujo autorizado de provisioning. */
public record TenantFixtures(UUID gymA, UUID gymB) {
    public static TenantFixtures fresh() {
        return new TenantFixtures(UUID.randomUUID(), UUID.randomUUID());
    }

    /** El callback debe usar el ejecutor autorizado; no desactiva las protecciones. */
    public void provision(java.util.function.Consumer<UUID> provisionOne) {
        provisionOne.accept(gymA);
        provisionOne.accept(gymB);
    }
}
