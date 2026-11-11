package com.gymplatform.integration;

import com.gymplatform.integration.support.OrderApiSupport;
import com.gymplatform.security.CurrentAccountPrincipal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebFlowIT extends OrderApiSupport {
    private MockHttpSession session(UUID account, UUID gym, String role) {
        var session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(new UsernamePasswordAuthenticationToken(
                new CurrentAccountPrincipal(account, gym, account + "@example.org"), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)))));
        return session;
    }
    @Test void loginIsPublicAndBusinessRequiresSession() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Ingresar")));
        mvc.perform(get("/app/clientes")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/app/clientes").header("HX-Request", "true"))
                .andExpect(status().isOk()).andExpect(header().string("HX-Redirect", "/login"));
        mvc.perform(post("/login").param("username", "none@example.org").param("password", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/login").with(csrf()).param("username", "none@example.org").param("password", UUID.randomUUID().toString()))
                .andExpect(redirectedUrl("/login?error"));
    }
    @Test void pagesRenderAndHtmxSearchRemainsTenantScoped() throws Exception {
        var session = session(accountA, gymA, "ADMIN");
        for (String path : List.of("/app", "/app/clientes", "/app/clientes/nuevo", "/app/clientes/" + clientA,
                "/app/clientes/" + clientA + "/editar", "/app/clientes/" + clientA + "/comprar",
                "/app/pedidos", "/app/membresias", "/app/membresias/nueva", "/app/membresias/" + membershipA + "/editar",
                "/app/catalogos/disciplinas", "/app/catalogos/canales", "/app/catalogos/localidades", "/app/recepcion", "/app/configuracion")) {
            mvc.perform(get(path).session(session)).andExpect(status().isOk());
        }
        mvc.perform(get("/app/clientes").param("dni", "12345678").header("HX-Request", "true").session(session))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Ana")))
                .andExpect(content().string(not(containsString("Otra"))));
        mvc.perform(get("/app/clientes/" + clientB).session(session)).andExpect(status().isNotFound());
        // Even stale or altered session claims are replaced by the current stored identity.
        var stale = session(accountA, gymB, "SUPERADMIN");
        mvc.perform(get("/app/clientes/" + clientA).session(stale)).andExpect(status().isOk());
        mvc.perform(get("/app/plataforma").session(stale)).andExpect(status().isForbidden());
        mvc.perform(get("/app/membresias/" + membershipB + "/editar").session(session)).andExpect(status().isNotFound());
    }
    @Test void purchasePaymentAnnulmentAndRenewalUseExistingRules() throws Exception {
        var session = session(accountA, gymA, "ADMIN");
        mvc.perform(post("/app/clientes/" + clientA + "/comprar").session(session).with(csrf())
                        .param("membresiaId", membershipB.toString()).param("cantidad", "2"))
                .andExpect(status().isNotFound());
        var result = mvc.perform(post("/app/clientes/" + clientA + "/comprar").session(session).with(csrf())
                        .param("membresiaId", membershipA.toString()).param("cantidad", "2"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String orderPath = result.getResponse().getRedirectedUrl();
        assertNotNull(orderPath);
        mvc.perform(get(orderPath).session(session)).andExpect(status().isOk()).andExpect(content().string(containsString("200.50")));
        mvc.perform(post(orderPath + "/pagos").session(session).param("montoRecibido", "50.25"))
                .andExpect(status().isForbidden());
        mvc.perform(post(orderPath + "/pagos").session(session).with(csrf()).param("montoRecibido", "50.25"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post(orderPath + "/pagos").session(session).with(csrf()).param("montoRecibido", "200.00"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("notice", containsString("49.75")));
        var paymentResponse = mvc.perform(get(orderPath.replace("/app", "/api/v1") + "/pagos")
                .header("Authorization", "Bearer " + adminA)).andReturn();
        var payments = mapper.readTree(paymentResponse.getResponse().getContentAsString()).get("content");
        var payment = payments.get(0).get("id").asText();
        mvc.perform(get("/app/pagos/" + payment + "/anular").session(session)).andExpect(status().isOk());
        mvc.perform(post("/app/pagos/" + payment + "/anular").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get(orderPath).session(session)).andExpect(status().isOk()).andExpect(content().string(containsString("ANULADO")));
        mvc.perform(post("/app/clientes/" + clientA + "/comprar").session(session).with(csrf())
                        .param("membresiaId", membershipA.toString()).param("cantidad", "1")
                        .param("pedidoAnteriorId", orderPath.substring(orderPath.lastIndexOf('/') + 1)))
                .andExpect(status().is3xxRedirection());
    }
    @Test void receptionCannotConfigureOrAnnulAndDisabledSessionStopsWorking() throws Exception {
        String password = UUID.randomUUID().toString();
        String email = UUID.randomUUID() + "@example.org";
        UUID id = create("usuarios-recepcion", adminA, Map.of("email", email, "password", password));
        var login = mvc.perform(post("/login").with(csrf()).param("username", email).param("password", password))
                .andExpect(redirectedUrl("/app")).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/app/clientes").session(session)).andExpect(status().isOk());
        mvc.perform(get("/app/configuracion").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/app/pagos/" + UUID.randomUUID() + "/anular").session(session).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/app/plataforma").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clientes").session(session)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/usuarios-recepcion/" + id + "/deshabilitacion").header("Authorization", "Bearer " + adminA))
                .andExpect(status().isOk());
        mvc.perform(get("/app/clientes").session(session)).andExpect(redirectedUrl("/login?expired"));
        mvc.perform(get("/app/clientes").session(session(id, gymA, "RECEPCION")).header("HX-Request", "true"))
                .andExpect(status().isOk()).andExpect(header().string("HX-Redirect", "/login?expired"));
    }
    @Test void receptionDirectoryIsTenantScopedAndWritesValidateInput() throws Exception {
        String emailA = UUID.randomUUID() + "@example.org", emailB = UUID.randomUUID() + "@example.org";
        create("usuarios-recepcion", adminA, Map.of("email", emailA, "password", UUID.randomUUID().toString()));
        UUID foreign = create("usuarios-recepcion", adminB, Map.of("email", emailB, "password", UUID.randomUUID().toString()));
        var session = session(accountA, gymA, "ADMIN");
        mvc.perform(get("/app/recepcion").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString(emailA))).andExpect(content().string(not(containsString(emailB))));
        mvc.perform(post("/app/recepcion/" + foreign + "/deshabilitar").session(session).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/app/clientes").session(session).with(csrf()).param("dni", "1").param("nombre", "Ana").param("apellido", "Perez"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(post("/app/catalogos/disciplinas").session(session).with(csrf()).param("nombre", "Nueva actividad"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/app/configuracion").session(session).with(csrf()).param("cantidadMaximaCompra", "4"))
                .andExpect(status().is3xxRedirection());
    }
    @Test void platformCanProvisionButCannotReadTenantBusiness() throws Exception {
        UUID platform = UUID.randomUUID();
        try (var connection = java.sql.DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.prepareStatement("insert into cuenta_acceso (id,email,password_hash,rol,habilitada,gimnasio_id,created_at,updated_at,version) values (?,?,?,'SUPERADMIN',true,null,now(),now(),0)")) {
            statement.setObject(1, platform); statement.setString(2, platform + "@example.org");
            statement.setString(3, org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()));
            statement.executeUpdate();
        }
        var session = session(platform, null, "SUPERADMIN");
        mvc.perform(get("/app").session(session)).andExpect(redirectedUrl("/app/plataforma"));
        mvc.perform(get("/app/plataforma").session(session)).andExpect(status().isOk());
        mvc.perform(post("/app/plataforma").session(session).with(csrf()).param("nombre", "Nuevo gimnasio")
                        .param("logoUrl", "https://example.org/logo.png").param("colorPrimario", "#123456")
                        .param("email", UUID.randomUUID() + "@example.org").param("password", UUID.randomUUID().toString()))
                .andExpect(redirectedUrl("/app/plataforma"));
        mvc.perform(get("/app/clientes/" + clientA).session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
    }
}
