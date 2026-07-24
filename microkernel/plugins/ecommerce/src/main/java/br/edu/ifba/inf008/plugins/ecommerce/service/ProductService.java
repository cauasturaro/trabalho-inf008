package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;

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
}
