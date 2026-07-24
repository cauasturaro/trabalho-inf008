package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.domain.Order;
import br.edu.ifba.inf008.plugins.ecommerce.domain.OrderStatus;
import br.edu.ifba.inf008.plugins.ecommerce.domain.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PaymentResult;
import br.edu.ifba.inf008.plugins.ecommerce.domain.shipping.ShippingPolicy;

import java.util.Objects;

/**
 * Computes the totals of an order and processes its payment.
 *
 * <p>The three selectable strategies are applied polymorphically:
 * <ul>
 *   <li>{@link DiscountPolicy} computes the discount total;</li>
 *   <li>{@link ShippingPolicy} computes the shipping total;</li>
 *   <li>{@link Payable} processes the payment for the grand total.</li>
 * </ul>
 * The grand total follows {@code subtotal - discount + shipping} and the order
 * status is updated according to the payment result.</p>
 */
public class CheckoutService {

    /**
     * Applies the discount and shipping strategies to the order, then processes
     * the payment and updates the order status accordingly.
     *
     * @return the same order, with totals, payment result and status set
     * @throws InvalidPaymentException when the payment data or state is invalid;
     *                                 the order status is set to
     *                                 {@link OrderStatus#PAYMENT_INVALID} before
     *                                 the exception propagates
     */
    public Order checkout(Order order, DiscountPolicy discountPolicy,
                          ShippingPolicy shippingPolicy, Payable payment)
            throws InvalidPaymentException {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(discountPolicy, "discountPolicy");
        Objects.requireNonNull(shippingPolicy, "shippingPolicy");
        Objects.requireNonNull(payment, "payment");

        order.setDiscountTotal(discountPolicy.calculateDiscount(order));
        order.setShippingTotal(shippingPolicy.calculateShipping(order));

        PaymentResult result;
        try {
            result = payment.pay(order.getGrandTotal());
        } catch (InvalidPaymentException e) {
            order.setStatus(OrderStatus.PAYMENT_INVALID);
            throw e;
        }

        order.setPaymentResult(result);
        order.setPaymentMethodCode(payment.getMethodCode());
        order.setStatus(mapStatus(result));
        return order;
    }

    private OrderStatus mapStatus(PaymentResult result) {
        switch (result.getStatus()) {
            case APPROVED:
                return OrderStatus.PAID;
            case PENDING:
                return OrderStatus.PENDING;
            case FAILED:
            default:
                return OrderStatus.CANCELLED;
        }
    }
}
