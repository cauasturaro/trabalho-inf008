package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;

import java.util.Objects;

/**
 * Application service for cart operations. Before adding a product it reloads
 * the product from the repository so the stock validation always runs against
 * the current available quantity, not a stale snapshot.
 */
public class CartService {

    private final ProductRepository productRepository;

    public CartService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
    }

    public Cart createCart(Customer customer) {
        return new Cart(customer);
    }

    /**
     * Adds a product to the cart, validating the requested quantity against
     * fresh stock data.
     *
     * @throws InsufficientStockException when stock cannot cover the request
     * @throws IllegalArgumentException   when the SKU is unknown or quantity invalid
     */
    public void addProduct(Cart cart, String sku, int quantity) throws InsufficientStockException {
        Objects.requireNonNull(cart, "cart");
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + sku));
        cart.addProduct(product, quantity);
    }

    public void removeProduct(Cart cart, Product product) {
        Objects.requireNonNull(cart, "cart");
        cart.removeProduct(product);
    }
}
