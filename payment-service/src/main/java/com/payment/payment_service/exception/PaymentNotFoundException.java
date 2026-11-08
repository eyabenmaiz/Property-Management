package com.payment.payment_service.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Paiement introuvable avec l'ID : " + id);
    }
}

