package br.edu.ifba.inf008.plugins.ecommerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OrderTest {

    private Customer customer() {
        return new Customer(1L, "Ana Souza", "ana@example.com", CustomerType.REGULAR);
    }

    private Product product(String sku, String price, int stock) {
        return new Product(1L, sku, "Name", "Description", Money.of(price), stock, true);
    }

    @Test
    void fromCartCopiesItemsAndSubtotal() throws Exception {
        Cart cart = new Cart(customer());
        cart.addProduct(product("A", "100.00", 5), 2);
        cart.addProduct(product("B", "50.00", 5), 1);

        Order order = Order.fromCart(cart);

        assertEquals(2, order.getItems().size());
        assertEquals(Money.of("250.00"), order.getSubtotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void grandTotalIsSubtotalMinusDiscountPlusShipping() throws Exception {
        Cart cart = new Cart(customer());
        cart.addProduct(product("A", "100.00", 5), 2);
        Order order = Order.fromCart(cart);

        order.setDiscountTotal(Money.of("20.00"));
        order.setShippingTotal(Money.of("15.00"));

        // 200 - 20 + 15
        assertEquals(Money.of("195.00"), order.getGrandTotal());
    }

    @Test
    void fromEmptyCartThrows() {
        Cart cart = new Cart(customer());
        assertThrows(IllegalArgumentException.class, () -> Order.fromCart(cart));
    }
}
