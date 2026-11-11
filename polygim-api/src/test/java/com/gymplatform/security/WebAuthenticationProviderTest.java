package com.gymplatform.security;

import com.gymplatform.model.CuentaAcceso;
import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class WebAuthenticationProviderTest {
    private IdentityScopeExecutor scopes() {
        var scopes=mock(IdentityScopeExecutor.class);
        when(scopes.execute(any())).thenAnswer(call->((Supplier<?>)call.getArgument(0)).get());return scopes;
    }
    @Test void successfulAuthenticationUsesStoredIdentityAndDiscardsPassword() {
        var identity=mock(IdentityService.class);String password=UUID.randomUUID().toString();
        var account=new CuentaAcceso(UUID.randomUUID(),"user@example.org",UUID.randomUUID().toString(),"ADMIN",UUID.randomUUID());
        when(identity.authenticate("user@example.org",password)).thenReturn(account);
        var result=new WebAuthenticationProvider(scopes(),identity).authenticate(UsernamePasswordAuthenticationToken.unauthenticated("user@example.org",password));
        assertNull(result.getCredentials());
        assertEquals(account.getGimnasioId(),((CurrentAccountPrincipal)result.getPrincipal()).gimnasioId());
        assertEquals("ROLE_ADMIN",result.getAuthorities().iterator().next().getAuthority());
    }
    @Test void malformedEmailIsAnAuthenticationFailure() {
        var identity=mock(IdentityService.class);when(identity.authenticate(eq(""),any())).thenThrow(new IllegalArgumentException());
        assertThrows(BadCredentialsException.class,()->new WebAuthenticationProvider(scopes(),identity)
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated("",UUID.randomUUID().toString())));
    }
    @Test void disabledOrIncorrectCredentialsDoNotCreateAuthentication() {
        var identity=mock(IdentityService.class);when(identity.authenticate(anyString(),any())).thenThrow(new BadCredentialsException("Credenciales inválidas"));
        assertThrows(BadCredentialsException.class,()->new WebAuthenticationProvider(scopes(),identity)
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated("user@example.org",null)));
    }
}
