package com.gymplatform.service.policy;
import java.time.LocalDate;
public final class VigenciaPolicy {
    private VigenciaPolicy() { }
    public static String status(LocalDate expiry, LocalDate today) { return today.isAfter(expiry) ? "VENCIDA" : "VIGENTE"; }
}
