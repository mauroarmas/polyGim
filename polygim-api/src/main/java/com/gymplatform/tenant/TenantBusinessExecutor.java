package com.gymplatform.tenant;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TenantBusinessExecutor {
    private final TenantGuard guard;
    private final TransactionTemplate transaction;
    public TenantBusinessExecutor(TenantGuard guard, PlatformTransactionManager manager) {
        this.guard = guard; this.transaction = new TransactionTemplate(manager);
    }
    public <T> T admin(Supplier<T> action) {
        guard.requireAdmin();
        return transaction.execute(status -> action.get());
    }
    public <T> T business(Supplier<T> action) {
        guard.requireBusiness();
        return transaction.execute(status -> action.get());
    }
}
