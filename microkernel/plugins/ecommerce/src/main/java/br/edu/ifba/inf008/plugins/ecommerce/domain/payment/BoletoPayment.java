package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;

import java.math.BigDecimal;

/**
 * Boleto payment. Generates a pending charge that is only confirmed once the
 * customer pays it, so the payment result is {@link PaymentStatus#PENDING}.
 */
public class BoletoPayment implements Payable {

    private final String payerDocument;

    public BoletoPayment(String payerDocument) {
        this.payerDocument = payerDocument;
    }

    @Override
    public PaymentResult pay(BigDecimal amount) throws InvalidPaymentException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive.");
        }
        if (payerDocument == null || payerDocument.trim().isEmpty()) {
            throw new InvalidPaymentException("Payer document is required for boleto.");
        }
        return PaymentResult.pending("boleto_pending_" + Integer.toHexString(payerDocument.hashCode()));
    }

    @Override
    public String getMethodCode() {
        return "BOLETO";
    }
}
