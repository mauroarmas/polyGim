package com.gymplatform.security;

import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Refresh account status and authority before entering any tenant transaction. */
public class WebAccountFilter extends OncePerRequestFilter {
    private final IdentityScopeExecutor scopes;
    private final IdentityService identity;
    public WebAccountFilter(IdentityScopeExecutor scopes, IdentityService identity) { this.scopes = scopes; this.identity = identity; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentAccountPrincipal principal) {
            try {
                var fresh = scopes.execute(() -> WebAuthenticationProvider.current(identity.requireEnabled(principal.accountId())));
                var context = SecurityContextHolder.createEmptyContext(); context.setAuthentication(fresh);
                SecurityContextHolder.setContext(context);
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false); if (session != null) session.invalidate();
                if ("true".equals(request.getHeader("HX-Request"))) {
                    response.setHeader("HX-Redirect", "/login?expired"); response.setStatus(200);
                } else response.sendRedirect("/login?expired");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
