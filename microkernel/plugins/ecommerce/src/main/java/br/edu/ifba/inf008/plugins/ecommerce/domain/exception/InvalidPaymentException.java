package br.edu.ifba.inf008.plugins.ecommerce.domain.exception;

/**
 * Thrown when a payment cannot be confirmed because the payment data or the
 * current state prevents the order from being paid.
 */
public class InvalidPaymentException extends Exception {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
