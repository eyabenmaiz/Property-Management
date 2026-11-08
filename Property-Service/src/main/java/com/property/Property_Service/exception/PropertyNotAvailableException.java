package com.property.Property_Service.exception;



public class PropertyNotAvailableException extends RuntimeException {
    public PropertyNotAvailableException(Long id) {
        super("La propriété avec l'ID " + id + " n'est pas disponible à la location.");
    }
}
