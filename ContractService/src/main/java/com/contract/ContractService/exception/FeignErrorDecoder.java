package com.contract.ContractService.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[FeignErrorDecoder] Erreur Feign → method={}, status={}",
                methodKey, response.status());

        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException(
                    "Property-Service: propriété introuvable (404)");
            case 409 -> new PropertyNotAvailableException(
                    "Property-Service: propriété non disponible (409)");
            case 500 -> new RuntimeException(
                    "Property-Service internal error");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}