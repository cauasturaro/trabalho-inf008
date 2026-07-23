package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * Student discount: applies a percentage of the subtotal, but only when the
 * order belongs to a student customer. For any other customer the discount is
 * zero.
 */
public class StudentDiscountPolicy implements DiscountPolicy {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final BigDecimal percentage;

    public StudentDiscountPolicy(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (!order.getCustomer().isStudent()) {
            return Money.ZERO;
        }
        BigDecimal subtotal = order.getSubtotal();
        return Money.scale(subtotal.multiply(percentage).divide(ONE_HUNDRED));
    }

    @Override
    public String getDescription() {
        return "Student discount";
    }
}
