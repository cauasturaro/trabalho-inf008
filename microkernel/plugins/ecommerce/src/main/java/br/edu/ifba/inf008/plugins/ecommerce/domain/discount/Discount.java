package br.edu.ifba.inf008.plugins.ecommerce.domain.discount;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A discount as configured in the database ({@code discounts} table). It
 * carries the data used to build the corresponding {@link DiscountPolicy}
 * at checkout time.
 */
public class Discount {

    private final Long id;
    private final String code;
    private final String name;
    private final DiscountType type;
    private final BigDecimal value;

    public Discount(Long id, String code, String name, DiscountType type, BigDecimal value) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.value = Objects.requireNonNull(value, "value");
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

    public DiscountType getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
