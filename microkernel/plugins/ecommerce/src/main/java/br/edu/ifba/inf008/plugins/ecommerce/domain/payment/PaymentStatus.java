package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

/**
 * Outcome of processing a payment through a {@link Payable}.
 */
public enum PaymentStatus {
    /** Payment confirmed. */
    APPROVED,
    /** Payment registered but not confirmed yet (e.g. a boleto awaiting payment). */
    PENDING,
    /** Payment was processed but declined. */
    FAILED
}
