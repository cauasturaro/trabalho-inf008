package br.edu.ifba.inf008.plugins.ecommerce.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An immutable line of an {@link Order}. It captures the product, the quantity
 * purchased, the unit price at purchase time and the resulting line total.
 * Order items are created and owned by their {@link Order} (composition).
 */
public class OrderItem {

    private final Product product;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    OrderItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice();
        this.lineTotal = Money.scale(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
