package br.edu.ifba.inf008.plugins.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.CustomerType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.CustomerRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.DiscountRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.OrderRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ShippingMethodRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    /** In-memory fake capturing what would be written to the database. */
    private static class FakeOrderRepository implements OrderRepository {
        Order savedOrder;
        Discount savedDiscount;

        @Override
        public Order save(Order order, ShippingMethod method, Discount discount) {
            this.savedOrder = order;
            this.savedDiscount = discount;
            order.setId(99L);
            return order;
        }
    }

    private final FakeOrderRepository orderRepository = new FakeOrderRepository();

    private OrderService service() {
        CustomerRepository customers = Collections::emptyList;
        ShippingMethodRepository methods = Collections::emptyList;
        DiscountRepository discounts = Collections::emptyList;
        return new OrderService(new CheckoutService(), orderRepository,
                customers, methods, discounts);
    }

    private Cart cart() throws Exception {
        Customer customer = new Customer(1L, "Ana Souza", "ana@example.com", CustomerType.REGULAR);
        Product product = new Product(1L, "A", "Name", "Desc", Money.of("100.00"), 10, true);
        Cart cart = new Cart(customer);
        cart.addProduct(product, 2);
        return cart;
    }

    private ShippingMethod standard() {
        return new ShippingMethod(1L, "STANDARD", "Standard", Money.of("25.00"), 7, Money.of("500.00"));
    }

    @Test
    void placeOrderComputesTotalsPersistsAndReturnsPaidOrder() throws Exception {
        Discount coupon = new Discount(1L, "WELCOME10", "Welcome", DiscountType.PERCENTAGE,
                new BigDecimal("10"));

        Order order = service().placeOrder(cart(), coupon, standard(), new PixPayment("key"));

        // subtotal 200, discount 20, shipping 25 (below 500 threshold) => 205
        assertEquals(Money.of("205.00"), order.getGrandTotal());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(99L, order.getId());
        assertEquals(order, orderRepository.savedOrder);
        assertEquals(coupon, orderRepository.savedDiscount);
    }

    @Test
    void invalidPaymentStillPersistsOrderAsPaymentInvalid() throws Exception {
        Cart cart = cart();

        assertThrows(InvalidPaymentException.class,
                () -> service().placeOrder(cart, null, standard(), new PixPayment(" ")));

        assertNotNull(orderRepository.savedOrder);
        assertEquals(OrderStatus.PAYMENT_INVALID, orderRepository.savedOrder.getStatus());
    }

    @Test
    void previewComputesTotalsWithoutPersisting() throws Exception {
        Order preview = service().previewOrder(cart(), null, standard());

        assertEquals(Money.of("200.00"), preview.getSubtotal());
        assertEquals(Money.of("225.00"), preview.getGrandTotal()); // no discount, +25 shipping
        assertEquals(OrderStatus.PENDING, preview.getStatus());
        assertEquals(null, orderRepository.savedOrder);
    }

    @Test
    void listMethodsDelegateToRepositories() {
        OrderService service = new OrderService(new CheckoutService(), orderRepository,
                () -> List.of(new Customer(1L, "Ana", "a@a.com", CustomerType.REGULAR)),
                () -> List.of(standard()),
                Collections::emptyList);

        assertEquals(1, service.listCustomers().size());
        assertEquals(1, service.listShippingMethods().size());
        assertEquals(0, service.listDiscounts().size());
    }
}
