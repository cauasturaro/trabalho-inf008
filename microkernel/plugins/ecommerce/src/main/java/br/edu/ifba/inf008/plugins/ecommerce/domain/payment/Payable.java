package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;

import java.math.BigDecimal;

/**
 * A payment method. Each concrete implementation encapsulates the rules of one
 * form of payment (credit card, pix, boleto), so that the checkout can process
 * any of them polymorphically.
 */
public interface Payable {

    /**
     * Processes a payment for the given amount.
     *
     * @return the result of the attempt (approved, pending or failed)
     * @throws InvalidPaymentException if the payment data or amount is invalid
     */
    PaymentResult pay(BigDecimal amount) throws InvalidPaymentException;

    /** Stable code of the payment method, e.g. {@code CREDIT_CARD}. */
    String getMethodCode();
}
