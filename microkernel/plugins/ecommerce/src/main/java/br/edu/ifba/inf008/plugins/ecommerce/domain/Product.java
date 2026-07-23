package br.edu.ifba.inf008.plugins.ecommerce.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A product that can be sold. The available quantity is derived from stock
 * movements in the persistence layer and cached here so the domain can
 * validate cart operations against it.
 */
public class Product {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private int availableQuantity;
    private boolean active;

    public Product(Long id, String sku, String name, String description,
                   BigDecimal unitPrice, int availableQuantity, boolean active) {
        this.id = id;
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.unitPrice = Money.scale(Objects.requireNonNull(unitPrice, "unitPrice"));
        this.availableQuantity = availableQuantity;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isActive() {
        return active;
    }

    /** True when at least {@code quantity} units are available in stock. */
    public boolean hasStockFor(int quantity) {
        return availableQuantity >= quantity;
    }

    @Override
    public String toString() {
        return sku + " - " + name;
    }
}
