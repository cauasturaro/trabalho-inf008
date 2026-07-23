package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Promotional coupon discount. Depending on the {@link DiscountType} it applies
 * either a percentage of the subtotal or a fixed amount. The discount never
 * exceeds the subtotal.
 */
public class CouponDiscountPolicy implements DiscountPolicy {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final String code;
    private final DiscountType type;
    private final BigDecimal value;

    public CouponDiscountPolicy(String code, DiscountType type, BigDecimal value) {
        this.code = Objects.requireNonNull(code, "code");
        this.type = Objects.requireNonNull(type, "type");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        BigDecimal subtotal = order.getSubtotal();
        BigDecimal discount = type == DiscountType.PERCENTAGE
                ? subtotal.multiply(value).divide(ONE_HUNDRED)
                : value;
        return Money.scale(discount.min(subtotal));
    }

    @Override
    public String getDescription() {
        return "Coupon " + code;
    }
}
