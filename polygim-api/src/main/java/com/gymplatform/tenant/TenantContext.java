package com.gymplatform.tenant;

import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TenantContext {
    public static final UUID SENTINEL = new UUID(0, 0);
    public enum Scope { AUTH, PLATFORM, TENANT, PROVISIONING }
    public record Current(Scope scope, UUID tenantId) { }
    private static final ThreadLocal<Current> CURRENT = new ThreadLocal<>();
    private TenantContext() { }

    public static ScopeHandle open(Scope scope, UUID tenantId) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(tenantId, "tenantId");
        boolean administrative = scope == Scope.AUTH || scope == Scope.PLATFORM;
        if (administrative != SENTINEL.equals(tenantId)) throw new IllegalArgumentException("Scope y tenant incompatibles");
        if (CURRENT.get() != null || TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("No se puede cambiar tenant en un scope o transacción abiertos");
        Current current = new Current(scope, tenantId);
        CURRENT.set(current);
        return new ScopeHandle(Thread.currentThread(), current);
    }
    public static Current require() {
        Current current = CURRENT.get();
        if (current == null) throw new IllegalStateException("No hay un scope de tenant activo");
        return current;
    }
    public static final class ScopeHandle implements AutoCloseable {
        private final Thread owner;
        private final Current current;
        private boolean closed;
        private ScopeHandle(Thread owner, Current current) { this.owner = owner; this.current = current; }
        @Override public void close() {
            if (closed) return;
            if (Thread.currentThread() != owner || CURRENT.get() != current)
                throw new IllegalStateException("Scope de tenant cerrado fuera de su contexto");
            CURRENT.remove();
            closed = true;
        }
    }
}
