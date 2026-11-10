package com.gymplatform.seed;

import com.gymplatform.controller.dto.MoneyCodec;
import com.gymplatform.service.CatalogoService;
import com.gymplatform.service.MembresiaService;
import com.gymplatform.tenant.TenantContext;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public record SeedDefinition(List<Gym> gimnasios) {
    public record Named(UUID id, String nombre) { }
    public record Membership(UUID id, UUID disciplinaId, String nombre, String precio, int duracion, String unidadDuracion) { }
    public record Gym(UUID id, UUID adminId, String nombre, String logoUrl, String colorPrimario,
                      List<Named> disciplinas, List<Named> canales, List<Membership> membresias) { }

    public void validate() {
        if (gimnasios == null || gimnasios.isEmpty()) throw new IllegalArgumentException("Seed vacío");
        Set<UUID> gyms = new HashSet<>(), admins = new HashSet<>(), disciplines = new HashSet<>(), channels = new HashSet<>(), memberships = new HashSet<>();
        for (var gym : gimnasios) {
            if (gym == null || gym.id() == null || TenantContext.SENTINEL.equals(gym.id()) || !gyms.add(gym.id())
                    || gym.adminId() == null || !admins.add(gym.adminId())) throw new IllegalArgumentException("IDs de seed inválidos");
            CatalogoService.normalizeName(gym.nombre());
            URI logo = URI.create(gym.logoUrl());
            if (logo.getHost() == null || !("https".equals(logo.getScheme()) || "http".equals(logo.getScheme()))
                    || gym.colorPrimario() == null || !gym.colorPrimario().matches("#[0-9A-Fa-f]{6}"))
                throw new IllegalArgumentException("Metadatos de seed inválidos");
            if (gym.disciplinas() == null || gym.canales() == null || gym.membresias() == null)
                throw new IllegalArgumentException("Catálogos de seed inválidos");
            Set<UUID> ownDisciplines = validateNamed(gym.disciplinas(), disciplines);
            validateNamed(gym.canales(), channels);
            for (var membership : gym.membresias()) {
                if (membership == null || membership.id() == null || !memberships.add(membership.id())
                        || !ownDisciplines.contains(membership.disciplinaId())) throw new IllegalArgumentException("Membresía de seed inválida");
                CatalogoService.normalizeName(membership.nombre());
                MembresiaService.validatePrice(MoneyCodec.parse(membership.precio()));
                MembresiaService.validateDuration(membership.duracion(), membership.unidadDuracion());
            }
        }
    }
    private static Set<UUID> validateNamed(List<Named> items, Set<UUID> allIds) {
        Set<UUID> own = new HashSet<>(); Set<String> names = new HashSet<>();
        for (var item : items) {
            if (item == null || item.id() == null || !allIds.add(item.id()) || !own.add(item.id())
                    || !names.add(CatalogoService.normalizeName(item.nombre()).toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Catálogo de seed inválido");
        }
        return own;
    }
}
