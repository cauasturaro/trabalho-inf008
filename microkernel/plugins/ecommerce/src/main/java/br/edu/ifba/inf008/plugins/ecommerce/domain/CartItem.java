package br.edu.ifba.inf008.plugins.ecommerce.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A line inside a {@link Cart}: a product together with the selected quantity.
 * The unit price is captured from the product when the line is created.
 */
public class CartItem {

    private final Product product;
    private int quantity;
    private final BigDecimal unitPrice;

    public CartItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** Line total = unit price * quantity. */
    public BigDecimal getLineTotal() {
        return Money.scale(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
