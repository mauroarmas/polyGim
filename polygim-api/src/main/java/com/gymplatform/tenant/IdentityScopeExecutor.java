package com.gymplatform.tenant;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class IdentityScopeExecutor {
    private final TransactionTemplate transaction;
    public IdentityScopeExecutor(PlatformTransactionManager manager) { this.transaction = new TransactionTemplate(manager); }
    public <T> T execute(Supplier<T> action) {
        try (var ignored = TenantContext.open(TenantContext.Scope.AUTH, TenantContext.SENTINEL)) {
            return transaction.execute(status -> action.get());
        }
    }
}
