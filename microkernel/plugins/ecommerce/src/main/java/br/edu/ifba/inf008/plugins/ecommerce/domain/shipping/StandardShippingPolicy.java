package br.edu.ifba.inf008.plugins.ecommerce.domain.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * Standard shipping: charges a base cost, but ships for free when the order
 * subtotal reaches a configured threshold.
 */
public class StandardShippingPolicy implements ShippingPolicy {

    private final BigDecimal baseCost;
    private final BigDecimal freeShippingThreshold;

    public StandardShippingPolicy(BigDecimal baseCost, BigDecimal freeShippingThreshold) {
        this.baseCost = baseCost;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    @Override
    public BigDecimal calculateShipping(Order order) {
        if (freeShippingThreshold != null
                && order.getSubtotal().compareTo(freeShippingThreshold) >= 0) {
            return Money.ZERO;
        }
        return Money.scale(baseCost);
    }

    @Override
    public String getDescription() {
        return "Standard shipping";
    }
}
