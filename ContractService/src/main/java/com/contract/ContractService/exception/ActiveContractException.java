package com.contract.ContractService.exception;

public class ActiveContractException extends RuntimeException {
    public ActiveContractException(Long propertyId) {
        super("Property " + propertyId + " already has an active contract");
    }
}