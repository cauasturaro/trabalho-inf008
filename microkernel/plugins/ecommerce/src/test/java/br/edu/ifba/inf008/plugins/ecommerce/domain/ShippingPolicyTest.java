package br.edu.ifba.inf008.plugins.ecommerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.StandardShippingPolicy;
import org.junit.jupiter.api.Test;

class ShippingPolicyTest {

    private Order orderWithSubtotal(String price, int quantity) {
        Customer customer = new Customer(1L, "Name", "e@example.com", CustomerType.REGULAR);
        Order order = new Order(customer);
        order.addItem(new Product(1L, "A", "Name", "Desc", Money.of(price), 100, true), quantity);
        return order;
    }

    @Test
    void standardIsFreeAboveThreshold() {
        ShippingPolicy policy = new StandardShippingPolicy(Money.of("25.00"), Money.of("500.00"));
        Order order = orderWithSubtotal("600.00", 1);

        assertEquals(Money.ZERO, policy.calculateShipping(order));
    }

    @Test
    void standardChargesBaseCostBelowThreshold() {
        ShippingPolicy policy = new StandardShippingPolicy(Money.of("25.00"), Money.of("500.00"));
        Order order = orderWithSubtotal("100.00", 1);

        assertEquals(Money.of("25.00"), policy.calculateShipping(order));
    }

    @Test
    void expressChargesFlatCost() {
        ShippingPolicy policy = new ExpressShippingPolicy(Money.of("60.00"));
        Order order = orderWithSubtotal("10000.00", 1);

        assertEquals(Money.of("60.00"), policy.calculateShipping(order));
    }

    @Test
    void pickupIsAlwaysFree() {
        ShippingPolicy policy = new PickupShippingPolicy();
        Order order = orderWithSubtotal("100.00", 1);

        assertEquals(Money.ZERO, policy.calculateShipping(order));
    }
}
