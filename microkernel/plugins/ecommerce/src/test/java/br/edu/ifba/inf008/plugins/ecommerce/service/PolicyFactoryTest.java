package br.edu.ifba.inf008.plugins.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.NoDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.StandardShippingPolicy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PolicyFactoryTest {

    private Discount discount(String code, DiscountType type) {
        return new Discount(1L, code, "Name", type, new BigDecimal("10"));
    }

    private ShippingMethod method(String code) {
        return new ShippingMethod(1L, code, "Name", Money.of("25.00"), 5, Money.of("500.00"));
    }

    @Test
    void mapsDiscountsToPolicies() {
        assertInstanceOf(NoDiscountPolicy.class, PolicyFactory.discountPolicyFor(null));
        assertInstanceOf(StudentDiscountPolicy.class,
                PolicyFactory.discountPolicyFor(discount("STUDENT15", DiscountType.PERCENTAGE)));
        assertInstanceOf(CouponDiscountPolicy.class,
                PolicyFactory.discountPolicyFor(discount("WELCOME10", DiscountType.PERCENTAGE)));
        assertInstanceOf(CouponDiscountPolicy.class,
                PolicyFactory.discountPolicyFor(discount("FIXED50", DiscountType.FIXED_AMOUNT)));
    }

    @Test
    void mapsShippingMethodsToPolicies() {
        assertInstanceOf(ExpressShippingPolicy.class,
                PolicyFactory.shippingPolicyFor(method("EXPRESS")));
        assertInstanceOf(PickupShippingPolicy.class,
                PolicyFactory.shippingPolicyFor(method("PICKUP")));
        assertInstanceOf(StandardShippingPolicy.class,
                PolicyFactory.shippingPolicyFor(method("STANDARD")));
        assertInstanceOf(StandardShippingPolicy.class,
                PolicyFactory.shippingPolicyFor(method("ECONOMY")));
    }
}
