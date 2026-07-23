package br.edu.ifba.inf008.plugins.ecommerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

class CartTest {

    private Customer customer() {
        return new Customer(1L, "Ana Souza", "ana@example.com", CustomerType.REGULAR);
    }

    private Product product(String sku, String price, int stock) {
        return new Product(1L, sku, "Name", "Description", Money.of(price), stock, true);
    }

    @Test
    void subtotalIsSumOfLineTotals() throws Exception {
        Cart cart = new Cart(customer());
        cart.addProduct(product("A", "10.00", 5), 2);
        cart.addProduct(product("B", "5.50", 5), 3);

        assertEquals(Money.of("36.50"), cart.getSubtotal());
    }

    @Test
    void addingSameProductMergesQuantities() throws Exception {
        Product product = product("A", "10.00", 5);
        Cart cart = new Cart(customer());

        cart.addProduct(product, 2);
        cart.addProduct(product, 1);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
        assertEquals(Money.of("30.00"), cart.getSubtotal());
    }

    @Test
    void addingBeyondStockThrows() {
        Cart cart = new Cart(customer());
        Product product = product("A", "10.00", 3);

        InsufficientStockException ex =
                assertThrows(InsufficientStockException.class, () -> cart.addProduct(product, 4));
        assertEquals("A", ex.getProductSku());
        assertEquals(4, ex.getRequestedQuantity());
        assertEquals(3, ex.getAvailableQuantity());
    }

    @Test
    void mergedQuantityBeyondStockThrows() throws Exception {
        Cart cart = new Cart(customer());
        Product product = product("A", "10.00", 3);
        cart.addProduct(product, 2);

        assertThrows(InsufficientStockException.class, () -> cart.addProduct(product, 2));
    }

    @Test
    void nonPositiveQuantityThrows() {
        Cart cart = new Cart(customer());
        Product product = product("A", "10.00", 3);

        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(product, 0));
    }

    @Test
    void removeProductRemovesLine() throws Exception {
        Cart cart = new Cart(customer());
        Product product = product("A", "10.00", 3);
        cart.addProduct(product, 1);

        cart.removeProduct(product);

        assertTrue(cart.isEmpty());
    }
}
