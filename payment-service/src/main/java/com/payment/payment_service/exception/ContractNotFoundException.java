package com.payment.payment_service.exception;

public class ContractNotFoundException extends RuntimeException {
    public ContractNotFoundException(String contractId) {
        super("Contrat introuvable avec l'ID : " + contractId);
    }
}
