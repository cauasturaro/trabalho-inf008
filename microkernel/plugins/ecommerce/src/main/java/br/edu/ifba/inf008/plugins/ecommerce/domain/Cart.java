package br.edu.ifba.inf008.plugins.ecommerce.domain;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A shopping cart holding the products a customer intends to buy. Adding a
 * product validates the requested quantity against the product stock; if a
 * line for the same product already exists, the quantities are merged and the
 * combined amount is validated again.
 */
public class Cart {

    private Long id;
    private final Customer customer;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(Customer customer) {
        this.customer = Objects.requireNonNull(customer, "customer");
    }

    public Cart(Long id, Customer customer) {
        this(customer);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    /**
     * Adds {@code quantity} units of {@code product} to the cart, validating
     * the total requested quantity against the available stock.
     *
     * @throws InsufficientStockException if the resulting quantity exceeds stock
     * @throws IllegalArgumentException   if quantity is not positive
     */
    public void addProduct(Product product, int quantity) throws InsufficientStockException {
        Objects.requireNonNull(product, "product");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        CartItem existing = findItem(product);
        int resultingQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
        if (!product.hasStockFor(resultingQuantity)) {
            throw new InsufficientStockException(product.getSku(), resultingQuantity,
                    product.getAvailableQuantity());
        }

        if (existing == null) {
            items.add(new CartItem(product, quantity));
        } else {
            existing.setQuantity(resultingQuantity);
        }
    }

    /** Removes the line for the given product, if present. */
    public void removeProduct(Product product) {
        CartItem existing = findItem(product);
        if (existing != null) {
            items.remove(existing);
        }
    }

    private CartItem findItem(Product product) {
        for (CartItem item : items) {
            if (item.getProduct().getSku().equals(product.getSku())) {
                return item;
            }
        }
        return null;
    }

    /** Subtotal of the cart = sum of every line total. */
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return Money.scale(subtotal);
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
