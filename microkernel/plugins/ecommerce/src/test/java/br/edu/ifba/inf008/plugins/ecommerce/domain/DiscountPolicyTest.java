package br.edu.ifba.inf008.plugins.ecommerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.StudentDiscountPolicy;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {

    private Order orderWith(CustomerType type, String price, int quantity) {
        Customer customer = new Customer(1L, "Name", "e@example.com", type);
        Order order = new Order(customer);
        order.addItem(new Product(1L, "A", "Name", "Desc", Money.of(price), 100, true), quantity);
        return order;
    }

    @Test
    void percentageCouponTakesPercentOfSubtotal() {
        Order order = orderWith(CustomerType.REGULAR, "100.00", 2); // subtotal 200
        DiscountPolicy policy = new CouponDiscountPolicy("WELCOME10", DiscountType.PERCENTAGE, new java.math.BigDecimal("10"));

        assertEquals(Money.of("20.00"), policy.calculateDiscount(order));
    }

    @Test
    void fixedCouponSubtractsFixedAmountCappedAtSubtotal() {
        Order order = orderWith(CustomerType.REGULAR, "30.00", 1); // subtotal 30
        DiscountPolicy policy = new CouponDiscountPolicy("FIXED50", DiscountType.FIXED_AMOUNT, new java.math.BigDecimal("50"));

        assertEquals(Money.of("30.00"), policy.calculateDiscount(order));
    }

    @Test
    void studentDiscountAppliesOnlyToStudents() {
        DiscountPolicy policy = new StudentDiscountPolicy(new java.math.BigDecimal("15"));

        Order studentOrder = orderWith(CustomerType.STUDENT, "100.00", 1); // 100
        Order regularOrder = orderWith(CustomerType.REGULAR, "100.00", 1);

        assertEquals(Money.of("15.00"), policy.calculateDiscount(studentOrder));
        assertEquals(Money.ZERO, policy.calculateDiscount(regularOrder));
    }
}
