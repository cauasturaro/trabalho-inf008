package br.edu.ifba.inf008.plugins.ecommerce.domain.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;

import java.math.BigDecimal;

/**
 * Express shipping: a flat base cost, always charged (no free threshold).
 */
public class ExpressShippingPolicy implements ShippingPolicy {

    private final BigDecimal baseCost;

    public ExpressShippingPolicy(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }

    @Override
    public BigDecimal calculateShipping(Order order) {
        return Money.scale(baseCost);
    }

    @Override
    public String getDescription() {
        return "Express shipping";
    }
}
