package com.contract.ContractService.exception;

public class PropertyNotAvailableException extends RuntimeException {
    public PropertyNotAvailableException(String propertyId) {
        super("La propriété " + propertyId + " est déjà occupée — aucun contrat actif possible.");
    }
}