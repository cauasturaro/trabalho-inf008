package br.edu.ifba.inf008.plugins.ecommerce.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatting helpers shared by the JavaFX views.
 */
final class Formats {

    private static final NumberFormat CURRENCY =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private Formats() {
    }

    static String currency(BigDecimal value) {
        return CURRENCY.format(value);
    }
}
