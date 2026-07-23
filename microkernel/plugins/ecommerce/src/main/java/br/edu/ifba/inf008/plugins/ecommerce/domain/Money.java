package br.edu.ifba.inf008.plugins.ecommerce.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Small helper for monetary values. Every amount handled by the domain is
 * kept with two decimal places and {@link RoundingMode#HALF_UP}, matching the
 * {@code DECIMAL(10,2)} columns used in the database.
 */
public final class Money {

    public static final BigDecimal ZERO = scale(BigDecimal.ZERO);

    private Money() {
    }

    /** Normalizes a value to the monetary scale (2 decimals, HALF_UP). */
    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Builds a normalized monetary value from a plain decimal string. */
    public static BigDecimal of(String value) {
        return scale(new BigDecimal(value));
    }
}
