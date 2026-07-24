package br.edu.ifba.inf008.plugins.ecommerce.persistence;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * Read access to products. The available quantity of each product is derived
 * from the {@code stock_movements} table
 * (INBOUND - OUTBOUND - RESERVED).
 */
public interface ProductRepository {

    /** All active products with their current available quantity. */
    List<Product> findAllActive();

    /** A single active product by SKU, with its current available quantity. */
    Optional<Product> findBySku(String sku);
}
