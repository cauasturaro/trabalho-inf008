package br.edu.ifba.inf008.plugins.ecommerce.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifba.inf008.plugins.ecommerce.domain.exception.InvalidPaymentException;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.BoletoPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PaymentStatus;
import br.edu.ifba.inf008.plugins.ecommerce.domain.payment.PixPayment;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void creditCardApprovesValidData() throws Exception {
        Payable payment = new CreditCardPayment("Ana Souza", "4111111111111111", "123");
        assertEquals(PaymentStatus.APPROVED, payment.pay(Money.of("100.00")).getStatus());
    }

    @Test
    void creditCardRejectsIncompleteData() {
        Payable payment = new CreditCardPayment("Ana Souza", "", "123");
        assertThrows(InvalidPaymentException.class, () -> payment.pay(Money.of("100.00")));
    }

    @Test
    void creditCardDeclinesAboveLimit() throws Exception {
        Payable payment = new CreditCardPayment("Ana Souza", "4111111111111111", "123", Money.of("50.00"));
        assertEquals(PaymentStatus.FAILED, payment.pay(Money.of("100.00")).getStatus());
    }

    @Test
    void pixApprovesWithValidKey() throws Exception {
        Payable payment = new PixPayment("ana@example.com");
        assertEquals(PaymentStatus.APPROVED, payment.pay(Money.of("100.00")).getStatus());
    }

    @Test
    void boletoIsPending() throws Exception {
        Payable payment = new BoletoPayment("12345678900");
        assertEquals(PaymentStatus.PENDING, payment.pay(Money.of("100.00")).getStatus());
    }

    @Test
    void rejectsNonPositiveAmount() {
        Payable payment = new PixPayment("ana@example.com");
        assertThrows(InvalidPaymentException.class, () -> payment.pay(Money.of("0.00")));
    }
}
