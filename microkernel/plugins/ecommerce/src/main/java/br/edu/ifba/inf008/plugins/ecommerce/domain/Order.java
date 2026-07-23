package br.edu.ifba.inf008.plugins.ecommerce.domain;

import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PaymentResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A customer order. An order is composed of a collection of {@link OrderItem}s
 * that it creates and owns (composition): the items do not exist independently
 * of the order. Discount, shipping and grand totals are computed during
 * checkout and stored back on the order.
 */
public class Order {

    private Long id;
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;

    private BigDecimal discountTotal = Money.ZERO;
    private BigDecimal shippingTotal = Money.ZERO;
    private PaymentResult paymentResult;

    public Order(Customer customer) {
        this.customer = Objects.requireNonNull(customer, "customer");
    }

    /** Builds an order from a cart, copying each cart line into an order item. */
    public static Order fromCart(Cart cart) {
        Objects.requireNonNull(cart, "cart");
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order from an empty cart.");
        }
        Order order = new Order(cart.getCustomer());
        for (CartItem item : cart.getItems()) {
            order.addItem(item.getProduct(), item.getQuantity());
        }
        return order;
    }

    /** Adds an item to this order (composition: the order creates the item). */
    public void addItem(Product product, int quantity) {
        items.add(new OrderItem(product, quantity));
    }

    /** Subtotal = sum of every order item line total. */
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return Money.scale(subtotal);
    }

    /** Grand total = subtotal - discounts + shipping. */
    public BigDecimal getGrandTotal() {
        return Money.scale(getSubtotal().subtract(discountTotal).add(shippingTotal));
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = Money.scale(discountTotal);
    }

    public BigDecimal getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(BigDecimal shippingTotal) {
        this.shippingTotal = Money.scale(shippingTotal);
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(PaymentResult paymentResult) {
        this.paymentResult = paymentResult;
    }
}
