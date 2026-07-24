package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * Null-object policy used when the customer selects no discount: the discount
 * is always zero. Keeps the checkout free of null checks.
 */
public class NoDiscountPolicy implements DiscountPolicy {

    @Override
    public BigDecimal calculateDiscount(Order order) {
        return Money.ZERO;
    }

    @Override
    public String getDescription() {
        return "No discount";
    }
}
