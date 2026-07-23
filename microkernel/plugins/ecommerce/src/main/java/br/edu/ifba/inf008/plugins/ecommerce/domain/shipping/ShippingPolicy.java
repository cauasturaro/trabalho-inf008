package br.edu.ifba.inf008.plugins.ecommerce.domain.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * A shipping policy. Each implementation encapsulates how the shipping cost of
 * an order is computed, so the checkout can apply any of them polymorphically.
 */
public interface ShippingPolicy {

    /** Shipping cost for the given order. */
    BigDecimal calculateShipping(Order order);

    /** Human-readable description of the shipping method. */
    String getDescription();
}
