package com.property.Property_Service.exception;


public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(Long id) {
        super("Propriété introuvable avec l'ID : " + id);
    }
}
