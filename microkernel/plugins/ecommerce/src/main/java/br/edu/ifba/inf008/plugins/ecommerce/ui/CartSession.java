package br.edu.ifba.inf008.plugins.ecommerce.ui;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;
import br.edu.ifba.inf008.plugins.ecommerce.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UI-level session holding the cart currently being assembled. The cart and
 * checkout tabs share this object: the cart tab mutates it through the
 * {@link CartService} and the checkout tab reads it to compute the summary.
 * Views register listeners to refresh themselves whenever the cart changes.
 */
public class CartSession {

    private final CartService cartService;
    private final List<Runnable> listeners = new ArrayList<>();
    private Customer customer;
    private Cart cart;

    public CartSession(CartService cartService) {
        this.cartService = Objects.requireNonNull(cartService, "cartService");
    }

    /** Starts a fresh cart for the given customer. */
    public void startFor(Customer customer) {
        this.customer = customer;
        this.cart = cartService.createCart(customer);
        notifyChanged();
    }

    public Customer getCustomer() {
        return customer;
    }

    /** Null until a customer is selected. */
    public Cart getCart() {
        return cart;
    }

    public boolean isReady() {
        return cart != null;
    }

    public void addProduct(String sku, int quantity) throws InsufficientStockException {
        requireCart();
        cartService.addProduct(cart, sku, quantity);
        notifyChanged();
    }

    public void removeProduct(Product product) {
        requireCart();
        cartService.removeProduct(cart, product);
        notifyChanged();
    }

    /** Discards the current cart (after an order) and starts a new one. */
    public void reset() {
        if (customer != null) {
            startFor(customer);
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void requireCart() {
        if (cart == null) {
            throw new IllegalStateException("Select a customer before using the cart.");
        }
    }

    private void notifyChanged() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
