package br.edu.ifba.inf008.plugins.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    /** In-memory fake acting as the products table. */
    private static class FakeProductRepository implements ProductRepository {
        final List<Product> products = new ArrayList<>();
        Integer lastInitialQuantity;

        @Override
        public List<Product> findAllActive() {
            return products;
        }

        @Override
        public Optional<Product> findBySku(String sku) {
            return products.stream().filter(p -> p.getSku().equals(sku)).findFirst();
        }

        @Override
        public boolean existsBySku(String sku) {
            return findBySku(sku).isPresent();
        }

        @Override
        public Product insert(Product product, int initialQuantity) {
            product.setId((long) (products.size() + 1));
            products.add(product);
            lastInitialQuantity = initialQuantity;
            return product;
        }
    }

    private final FakeProductRepository repository = new FakeProductRepository();
    private final ProductService service = new ProductService(repository);

    @Test
    void registersProductWithInitialStock() {
        Product product = service.registerProduct(" kb-mech-02 ", "Keyboard", "Compact keyboard",
                Money.of("199.90"), 5);

        assertEquals("KB-MECH-02", product.getSku()); // trimmed and upper-cased
        assertEquals(1L, product.getId());
        assertEquals(5, repository.lastInitialQuantity);
        assertTrue(product.isActive());
    }

    @Test
    void rejectsInvalidFields() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct(" ", "Name", "Desc", Money.of("10.00"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("SKU-1", "", "Desc", Money.of("10.00"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("SKU-1", "Name", " ", Money.of("10.00"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("SKU-1", "Name", "Desc", null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("SKU-1", "Name", "Desc", Money.of("0.00"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("SKU-1", "Name", "Desc", Money.of("10.00"), -1));
    }

    @Test
    void rejectsDuplicateSku() {
        service.registerProduct("SKU-1", "Name", "Desc", Money.of("10.00"), 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.registerProduct("sku-1", "Other", "Other", Money.of("20.00"), 1));
    }

    @Test
    void searchMatchesSkuAndNameCaseInsensitive() {
        service.registerProduct("KB-MECH-02", "Mechanical Keyboard", "Desc", Money.of("199.90"), 1);
        service.registerProduct("MS-WIRE-02", "Wireless Mouse", "Desc", Money.of("89.90"), 1);

        assertEquals(1, service.searchProducts("keyboard").size());
        assertEquals(1, service.searchProducts("ms-wire").size());
        assertEquals(2, service.searchProducts("  ").size());
    }
}
