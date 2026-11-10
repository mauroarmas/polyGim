package com.gymplatform.security;

import java.util.UUID;

public record CurrentAccountPrincipal(UUID accountId, UUID gimnasioId, String email) { }
