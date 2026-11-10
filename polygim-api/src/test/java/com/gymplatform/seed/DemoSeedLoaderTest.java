package com.gymplatform.seed;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DemoSeedLoaderTest {
    @Test void validatesStableIdsPricesAndDisciplineReferencesBeforeAnyWrites() {
        UUID gym = UUID.randomUUID(), admin = UUID.randomUUID(), discipline = UUID.randomUUID();
        var good = new SeedDefinition.Gym(gym, admin, "Gym", "https://example.org/logo", "#123456",
                List.of(new SeedDefinition.Named(discipline, "Actividad")), List.of(),
                List.of(new SeedDefinition.Membership(UUID.randomUUID(), discipline, "Plan", "100.25", 2, "SEMANAS")));
        assertThatCode(() -> new SeedDefinition(List.of(good)).validate()).doesNotThrowAnyException();
        var invalidPrice = new SeedDefinition.Gym(gym, admin, "Gym", "https://example.org/logo", "#123456", good.disciplinas(), List.of(),
                List.of(new SeedDefinition.Membership(UUID.randomUUID(), discipline, "Plan", "0.00", 2, "SEMANAS")));
        assertThatThrownBy(() -> new SeedDefinition(List.of(invalidPrice)).validate()).isInstanceOf(IllegalArgumentException.class);
        var foreign = new SeedDefinition.Gym(gym, admin, "Gym", "https://example.org/logo", "#123456", good.disciplinas(), List.of(),
                List.of(new SeedDefinition.Membership(UUID.randomUUID(), UUID.randomUUID(), "Plan", "1.00", 1, "DIAS")));
        assertThatThrownBy(() -> new SeedDefinition(List.of(foreign)).validate()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SeedDefinition(List.of(good, good)).validate()).isInstanceOf(IllegalArgumentException.class);
    }
}
