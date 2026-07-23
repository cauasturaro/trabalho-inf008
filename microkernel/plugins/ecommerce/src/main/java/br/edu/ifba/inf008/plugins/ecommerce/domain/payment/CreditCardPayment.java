package br.edu.ifba.inf008.plugins.ecommerce.domain.payment;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;

import java.math.BigDecimal;

/**
 * Credit card payment. Rejects invalid card data with an
 * {@link InvalidPaymentException} and declines (FAILED) when the amount
 * exceeds the available limit.
 */
public class CreditCardPayment implements Payable {

    private final String cardHolder;
    private final String cardNumber;
    private final String securityCode;
    private final BigDecimal creditLimit;

    public CreditCardPayment(String cardHolder, String cardNumber, String securityCode) {
        this(cardHolder, cardNumber, securityCode, null);
    }

    public CreditCardPayment(String cardHolder, String cardNumber, String securityCode,
                             BigDecimal creditLimit) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
        this.creditLimit = creditLimit;
    }

    @Override
    public PaymentResult pay(BigDecimal amount) throws InvalidPaymentException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive.");
        }
        if (isBlank(cardHolder) || isBlank(cardNumber) || isBlank(securityCode)) {
            throw new InvalidPaymentException("Credit card data is incomplete.");
        }
        if (securityCode.length() != 3) {
            throw new InvalidPaymentException("Invalid card security code.");
        }
        if (creditLimit != null && amount.compareTo(creditLimit) > 0) {
            return PaymentResult.failed("cc_" + last4(), "Credit limit exceeded.");
        }
        return PaymentResult.approved("cc_approved_" + last4());
    }

    @Override
    public String getMethodCode() {
        return "CREDIT_CARD";
    }

    private String last4() {
        return cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
