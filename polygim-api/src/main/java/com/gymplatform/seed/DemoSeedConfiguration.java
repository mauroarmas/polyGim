package com.gymplatform.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.repository.identity.IdentityRepository;
import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.tenant.IdentityScopeExecutor;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Configuration
@Profile("demo")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoSeedConfiguration {
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    ApplicationRunner demoSeedRunner(DemoSeedLoader loader, IdentityScopeExecutor scopes, IdentityRepository identities,
            ObjectMapper mapper, @Value("${app.seed.admin-email:}") String email,
            @Value("${app.seed.admin-password:}") String password, @Value("${app.default-gym-max-quantity}") int maximum) {
        return args -> {
            var account = scopes.execute(() -> identities.findFirstEnabledSuperadmin()
                    .orElseThrow(() -> new IllegalStateException("Se necesita un superadministrador habilitado para el seed")));
            var options = new LoaderOptions(); options.setAllowDuplicateKeys(false);
            SeedDefinition definition;
            try (var input = new ClassPathResource("seed/gimnasios-demo.yml").getInputStream()) {
                Object source = new Yaml(new SafeConstructor(options)).load(input);
                definition = mapper.convertValue(source, SeedDefinition.class);
            }
            var previous = SecurityContextHolder.getContext().getAuthentication();
            try {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        new CurrentAccountPrincipal(account.getId(), null, account.getEmail()), null,
                        List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
                loader.load(definition, email, password, maximum);
            } finally { SecurityContextHolder.getContext().setAuthentication(previous); }
        };
    }
}
