package com.payment.payment_service.exception;

public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(String contractId) {
        super("Un paiement existe déjà pour ce contrat à cette date : " + contractId);
    }
}