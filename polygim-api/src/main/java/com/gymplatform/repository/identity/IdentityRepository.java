package com.gymplatform.repository.identity;
import com.gymplatform.model.CuentaAcceso;
import java.util.Optional;
import java.util.UUID;
public interface IdentityRepository {
    Optional<CuentaAcceso> findByEmail(String email);
    Optional<CuentaAcceso> findById(UUID id);
    void persist(CuentaAcceso account);
    boolean superadminExists();
    Optional<CuentaAcceso> findFirstEnabledSuperadmin();
}
