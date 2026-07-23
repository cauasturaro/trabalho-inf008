package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;

import java.math.BigDecimal;

/**
 * Pix payment. Confirmed immediately when a valid pix key is provided.
 */
public class PixPayment implements Payable {

    private final String pixKey;

    public PixPayment(String pixKey) {
        this.pixKey = pixKey;
    }

    @Override
    public PaymentResult pay(BigDecimal amount) throws InvalidPaymentException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive.");
        }
        if (pixKey == null || pixKey.trim().isEmpty()) {
            throw new InvalidPaymentException("Pix key is required.");
        }
        return PaymentResult.approved("pix_approved_" + Integer.toHexString(pixKey.hashCode()));
    }

    @Override
    public String getMethodCode() {
        return "PIX";
    }
}
