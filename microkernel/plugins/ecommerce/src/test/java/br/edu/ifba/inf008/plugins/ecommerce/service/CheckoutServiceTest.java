package br.edu.ifba.inf008.plugins.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.CustomerType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Money;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Product;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountType;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.BoletoPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.StandardShippingPolicy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CheckoutServiceTest {

    private final CheckoutService checkout = new CheckoutService();

    private Order order(String price, int quantity) {
        Customer customer = new Customer(1L, "Ana Souza", "ana@example.com", CustomerType.REGULAR);
        Order order = new Order(customer);
        order.addItem(new Product(1L, "A", "Name", "Desc", Money.of(price), 100, true), quantity);
        return order;
    }

    @Test
    void computesGrandTotalWithDiscountAndShipping() throws Exception {
        Order order = order("100.00", 2); // subtotal 200
        checkout.checkout(order,
                new CouponDiscountPolicy("C", DiscountType.PERCENTAGE, new BigDecimal("10")), // -20
                new ExpressShippingPolicy(Money.of("60.00")),                                  // +60
                new PixPayment("ana@example.com"));

        assertEquals(Money.of("20.00"), order.getDiscountTotal());
        assertEquals(Money.of("60.00"), order.getShippingTotal());
        assertEquals(Money.of("240.00"), order.getGrandTotal()); // 200 - 20 + 60
    }

    @Test
    void approvedPaymentMarksOrderPaid() throws Exception {
        Order order = order("100.00", 1);
        checkout.checkout(order,
                new CouponDiscountPolicy("C", DiscountType.PERCENTAGE, BigDecimal.ZERO),
                new StandardShippingPolicy(Money.of("25.00"), Money.of("500.00")),
                new CreditCardPayment("Ana", "4111111111111111", "123"));

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void pendingPaymentMarksOrderPending() throws Exception {
        Order order = order("100.00", 1);
        checkout.checkout(order,
                new CouponDiscountPolicy("C", DiscountType.PERCENTAGE, BigDecimal.ZERO),
                new StandardShippingPolicy(Money.of("25.00"), null),
                new BoletoPayment("12345678900"));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void declinedPaymentCancelsOrder() throws Exception {
        Order order = order("100.00", 1);
        checkout.checkout(order,
                new CouponDiscountPolicy("C", DiscountType.PERCENTAGE, BigDecimal.ZERO),
                new StandardShippingPolicy(Money.of("25.00"), null),
                new CreditCardPayment("Ana", "4111111111111111", "123", Money.of("10.00")));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void invalidPaymentThrowsAndMarksOrderInvalid() {
        Order order = order("100.00", 1);
        assertThrows(InvalidPaymentException.class, () -> checkout.checkout(order,
                new CouponDiscountPolicy("C", DiscountType.PERCENTAGE, BigDecimal.ZERO),
                new StandardShippingPolicy(Money.of("25.00"), null),
                new PixPayment(""))); // blank pix key
        assertEquals(OrderStatus.PAYMENT_INVALID, order.getStatus());
    }
}
