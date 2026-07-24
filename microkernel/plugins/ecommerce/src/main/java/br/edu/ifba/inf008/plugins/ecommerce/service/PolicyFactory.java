package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.NoDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.StandardShippingPolicy;

/**
 * Maps database configuration rows to the polymorphic policy objects applied
 * at checkout. This is the single place that decides which concrete policy
 * class corresponds to which configuration; from here on, the checkout only
 * sees the {@link DiscountPolicy} and {@link ShippingPolicy} interfaces.
 */
public final class PolicyFactory {

    private static final String STUDENT_CODE_PREFIX = "STUDENT";
    private static final String EXPRESS_CODE = "EXPRESS";
    private static final String PICKUP_CODE = "PICKUP";

    private PolicyFactory() {
    }

    /** Builds the discount policy for a selected discount; null means none. */
    public static DiscountPolicy discountPolicyFor(Discount discount) {
        if (discount == null) {
            return new NoDiscountPolicy();
        }
        if (discount.getCode().startsWith(STUDENT_CODE_PREFIX)) {
            return new StudentDiscountPolicy(discount.getValue());
        }
        return new CouponDiscountPolicy(discount.getCode(), discount.getType(), discount.getValue());
    }

    /** Builds the shipping policy for a selected shipping method. */
    public static ShippingPolicy shippingPolicyFor(ShippingMethod method) {
        switch (method.getCode()) {
            case EXPRESS_CODE:
                return new ExpressShippingPolicy(method.getBaseCost());
            case PICKUP_CODE:
                return new PickupShippingPolicy();
            default:
                return new StandardShippingPolicy(method.getBaseCost(),
                        method.getFreeShippingThreshold());
        }
    }
}
