package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.Discount;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingMethod;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.CustomerRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.DiscountRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.OrderRepository;
import br.edu.ifba.inf008.plugins.ecommerce.persistence.ShippingMethodRepository;

import java.util.List;
import java.util.Objects;

/**
 * Application service that runs the full order flow: it turns a cart into an
 * order, applies the selected discount and shipping strategies, processes the
 * payment and persists the outcome. It also exposes the reference data the
 * checkout screen needs (customers, shipping methods, discounts).
 */
public class OrderService {

    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final DiscountRepository discountRepository;

    public OrderService(CheckoutService checkoutService,
                        OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ShippingMethodRepository shippingMethodRepository,
                        DiscountRepository discountRepository) {
        this.checkoutService = Objects.requireNonNull(checkoutService, "checkoutService");
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
        this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository");
        this.shippingMethodRepository =
                Objects.requireNonNull(shippingMethodRepository, "shippingMethodRepository");
        this.discountRepository = Objects.requireNonNull(discountRepository, "discountRepository");
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public List<ShippingMethod> listShippingMethods() {
        return shippingMethodRepository.findAll();
    }

    public List<Discount> listDiscounts() {
        return discountRepository.findAllActive();
    }

    /**
     * Preview of the totals for the current selection, without touching the
     * payment or the database. Used by the UI to show subtotal, discount,
     * shipping and grand total before confirmation.
     */
    public Order previewOrder(Cart cart, Discount discount, ShippingMethod method) {
        Order order = Order.fromCart(cart);
        DiscountPolicy discountPolicy = PolicyFactory.discountPolicyFor(discount);
        ShippingPolicy shippingPolicy = PolicyFactory.shippingPolicyFor(method);
        order.setDiscountTotal(discountPolicy.calculateDiscount(order));
        order.setShippingTotal(shippingPolicy.calculateShipping(order));
        return order;
    }

    /**
     * Runs the checkout for the cart and persists the resulting order.
     *
     * <p>When the payment is invalid the order is still persisted with status
     * {@code PAYMENT_INVALID} (without payment row or stock movement) and the
     * exception propagates so the UI can inform the user.</p>
     */
    public Order placeOrder(Cart cart, Discount discount, ShippingMethod method, Payable payment)
            throws InvalidPaymentException {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(payment, "payment");

        Order order = Order.fromCart(cart);
        DiscountPolicy discountPolicy = PolicyFactory.discountPolicyFor(discount);
        ShippingPolicy shippingPolicy = PolicyFactory.shippingPolicyFor(method);

        try {
            checkoutService.checkout(order, discountPolicy, shippingPolicy, payment);
        } catch (InvalidPaymentException e) {
            orderRepository.save(order, method, discount);
            throw e;
        }
        return orderRepository.save(order, method, discount);
    }
}
