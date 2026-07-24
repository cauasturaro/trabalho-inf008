package br.edu.ifba.inf008.plugins.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.CustomerType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CartServiceTest {

    private static class FakeProductRepository implements ProductRepository {
        private final Product product;

        FakeProductRepository(Product product) {
            this.product = product;
        }

        @Override
        public List<Product> findAllActive() {
            return List.of(product);
        }

        @Override
        public Optional<Product> findBySku(String sku) {
            return product.getSku().equals(sku) ? Optional.of(product) : Optional.empty();
        }

        @Override
        public boolean existsBySku(String sku) {
            return product.getSku().equals(sku);
        }

        @Override
        public Product insert(Product newProduct, int initialQuantity) {
            newProduct.setId(2L);
            return newProduct;
        }
    }

    private final Product product =
            new Product(1L, "A", "Name", "Desc", Money.of("10.00"), 3, true);
    private final CartService service = new CartService(new FakeProductRepository(product));
    private final Cart cart =
            service.createCart(new Customer(1L, "Ana", "ana@example.com", CustomerType.REGULAR));

    @Test
    void addsProductLoadedFromRepository() throws Exception {
        service.addProduct(cart, "A", 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(Money.of("20.00"), cart.getSubtotal());
    }

    @Test
    void rejectsUnknownSku() {
        assertThrows(IllegalArgumentException.class, () -> service.addProduct(cart, "NOPE", 1));
    }

    @Test
    void propagatesInsufficientStock() {
        assertThrows(InsufficientStockException.class, () -> service.addProduct(cart, "A", 4));
    }

    @Test
    void removesProduct() throws Exception {
        service.addProduct(cart, "A", 1);
        service.removeProduct(cart, product);

        assertTrue(cart.isEmpty());
    }
}
