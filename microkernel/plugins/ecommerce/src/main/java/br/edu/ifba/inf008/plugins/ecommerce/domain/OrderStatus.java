package br.edu.ifba.inf008.plugins.ecommerce.domain;

/**
 * Lifecycle status of an order, updated according to the payment result.
 */
public enum OrderStatus {
    /** Order created, payment not processed yet. */
    PENDING,
    /** Payment approved; order confirmed. */
    PAID,
    /** Order cancelled. */
    CANCELLED,
    /** Payment could not be processed due to invalid data or state. */
    PAYMENT_INVALID
}
