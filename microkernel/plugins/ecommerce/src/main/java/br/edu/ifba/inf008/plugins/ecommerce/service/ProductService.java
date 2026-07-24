package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service for listing and searching products.
 */
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
    }

    public List<Product> listProducts() {
        return productRepository.findAllActive();
    }

    /** Case-insensitive search by SKU or name; blank query returns everything. */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listProducts();
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<Product> matches = new ArrayList<>();
        for (Product product : productRepository.findAllActive()) {
            if (product.getSku().toLowerCase(Locale.ROOT).contains(needle)
                    || product.getName().toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(product);
            }
        }
        return matches;
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    /**
     * Registers a new product with an optional initial stock quantity.
     *
     * @return the persisted product with its generated id
     * @throws IllegalArgumentException when a field is invalid or the SKU is
     *                                  already in use
     */
    public Product registerProduct(String sku, String name, String description,
                                   BigDecimal unitPrice, int initialQuantity) {
        if (isBlank(sku)) {
            throw new IllegalArgumentException("SKU is required.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (isBlank(description)) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive.");
        }
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative.");
        }
        String normalizedSku = sku.trim().toUpperCase(Locale.ROOT);
        if (productRepository.existsBySku(normalizedSku)) {
            throw new IllegalArgumentException(
                    "A product with SKU " + normalizedSku + " already exists.");
        }
        Product product = new Product(null, normalizedSku, name.trim(), description.trim(),
                Money.scale(unitPrice), initialQuantity, true);
        return productRepository.insert(product, initialQuantity);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
