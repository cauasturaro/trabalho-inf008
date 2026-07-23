package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

/**
 * Immutable result of a payment attempt: its status, a transaction reference
 * and, when it fails, the reason. Built through the static factory methods.
 */
public class PaymentResult {

    private final PaymentStatus status;
    private final String transactionReference;
    private final String failureReason;

    private PaymentResult(PaymentStatus status, String transactionReference, String failureReason) {
        this.status = status;
        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
    }

    public static PaymentResult approved(String transactionReference) {
        return new PaymentResult(PaymentStatus.APPROVED, transactionReference, null);
    }

    public static PaymentResult pending(String transactionReference) {
        return new PaymentResult(PaymentStatus.PENDING, transactionReference, null);
    }

    public static PaymentResult failed(String transactionReference, String failureReason) {
        return new PaymentResult(PaymentStatus.FAILED, transactionReference, failureReason);
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
