package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * A discount policy. Each implementation encapsulates one discount rule, so the
 * checkout can apply any of them polymorphically.
 */
public interface DiscountPolicy {

    /** Amount (never greater than the subtotal) to subtract from the order. */
    BigDecimal calculateDiscount(Order order);

    /** Human-readable description of the discount, e.g. the coupon code. */
    String getDescription();
}
