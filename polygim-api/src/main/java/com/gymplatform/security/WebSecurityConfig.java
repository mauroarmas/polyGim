package com.gymplatform.security;

import com.gymplatform.service.IdentityService;
import com.gymplatform.tenant.IdentityScopeExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {
    @Bean @Order(1)
    SecurityFilterChain webFilterChain(HttpSecurity http, IdentityScopeExecutor scopes, IdentityService identity) throws Exception {
        http.securityMatcher("/", "/login", "/logout", "/app/**", "/assets/**")
                .authenticationProvider(new WebAuthenticationProvider(scopes, identity))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/assets/**").permitAll()
                        .requestMatchers("/app/plataforma/**").hasRole("SUPERADMIN")
                        .requestMatchers("/app/recepcion/**", "/app/configuracion/**", "/app/catalogos/**", "/app/pagos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/app/membresias/**").hasRole("ADMIN")
                        .requestMatchers("/app/membresias/nueva", "/app/membresias/*/editar").hasRole("ADMIN")
                        .requestMatchers("/app/clientes/**", "/app/pedidos/**", "/app/membresias/**").hasAnyRole("ADMIN", "RECEPCION")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/app", true).failureUrl("/login?error"))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").deleteCookies("JSESSIONID"))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> {
                            if ("true".equals(request.getHeader("HX-Request"))) {
                                response.setHeader("HX-Redirect", "/login"); response.setStatus(200);
                            } else response.sendRedirect("/login");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403); response.setContentType("text/html;charset=UTF-8");
                            response.getWriter().write("<!doctype html><html lang='es'><meta name='viewport' content='width=device-width, initial-scale=1'><title>Acceso denegado</title><link rel='stylesheet' href='/assets/app.css'><main class='standalone'><p class='eyebrow'>403 · ACCESO DENEGADO</p><h1>No pudimos completar la acción</h1><p>No tenés permiso o el formulario venció. Volvé a abrir la página antes de reintentar.</p><a class='button' href='/app'>Volver al inicio</a></main></html>");
                        }))
                .addFilterAfter(new WebAccountFilter(scopes, identity), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new TenantRequestFilter(), WebAccountFilter.class);
        return http.build();
    }
}
