package br.edu.ifba.inf008.plugins.ecommerce.domain.shipping;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A shipping method as configured in the database ({@code shipping_methods}
 * table). It carries the data used to build the corresponding
 * {@link ShippingPolicy} at checkout time.
 */
public class ShippingMethod {

    private final Long id;
    private final String code;
    private final String name;
    private final BigDecimal baseCost;
    private final int estimatedDays;
    private final BigDecimal freeShippingThreshold;

    public ShippingMethod(Long id, String code, String name, BigDecimal baseCost,
                          int estimatedDays, BigDecimal freeShippingThreshold) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.baseCost = Objects.requireNonNull(baseCost, "baseCost");
        this.estimatedDays = estimatedDays;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    /** Nullable: some methods never ship for free. */
    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    @Override
    public String toString() {
        return name + " (" + estimatedDays + " days)";
    }
}
