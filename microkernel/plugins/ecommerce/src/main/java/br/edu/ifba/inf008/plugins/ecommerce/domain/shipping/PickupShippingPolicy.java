package br.edu.ifba.inf008.plugins.ecommerce.domain.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * Store pickup: the customer collects the order, so shipping is always free.
 */
public class PickupShippingPolicy implements ShippingPolicy {

    @Override
    public BigDecimal calculateShipping(Order order) {
        return Money.ZERO;
    }

    @Override
    public String getDescription() {
        return "Store pickup";
    }
}
