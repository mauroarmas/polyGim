package com.gymplatform.service.policy;

import com.gymplatform.service.MembresiaService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;

public final class PedidoPolicy {
    private static final LocalDate MIN_DATABASE_DATE = LocalDate.of(-4712, 1, 1);
    private static final LocalDate MAX_DATABASE_DATE = LocalDate.of(5874897, 12, 31);
    private PedidoPolicy() { }
    public static BigDecimal total(BigDecimal price, int quantity, int maximum) {
        MembresiaService.validatePrice(price);
        if (quantity < 1 || maximum < 1 || quantity > maximum) throw new IllegalArgumentException("Cantidad inválida");
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        if (total.precision() > 19) throw new IllegalArgumentException("Total fuera de rango");
        return total;
    }
    public static LocalDate purchaseDate(LocalDate requested, Clock clock) { return purchaseDate(requested, LocalDate.now(clock)); }
    public static LocalDate purchaseDate(LocalDate requested, LocalDate today) {
        LocalDate date = requested == null ? today : requested;
        if (date.isAfter(today)) throw new IllegalArgumentException("Fecha futura inválida");
        requireDatabaseDate(date);
        return date;
    }
    public static LocalDate renewalBase(LocalDate purchase, LocalDate previousExpiry) {
        return previousExpiry != null && !previousExpiry.isBefore(purchase) ? previousExpiry : purchase;
    }
    public static LocalDate expiry(LocalDate base, int quantity, int duration, String unit) {
        MembresiaService.validateDuration(duration, unit);
        if (quantity < 1) throw new IllegalArgumentException("Cantidad inválida");
        try {
            long length = Math.multiplyExact((long) quantity, duration);
            LocalDate result = switch (unit) {
                case "DIAS" -> base.plusDays(length);
                case "SEMANAS" -> base.plusWeeks(length);
                case "MESES" -> base.plusMonths(length);
                default -> throw new IllegalArgumentException("Unidad inválida");
            };
            requireDatabaseDate(result);
            return result;
        } catch (ArithmeticException | DateTimeException ex) {
            throw new IllegalArgumentException("Fecha fuera de rango");
        }
    }
    private static void requireDatabaseDate(LocalDate date) {
        if (date.isBefore(MIN_DATABASE_DATE) || date.isAfter(MAX_DATABASE_DATE)) throw new IllegalArgumentException("Fecha fuera de rango");
    }
}
