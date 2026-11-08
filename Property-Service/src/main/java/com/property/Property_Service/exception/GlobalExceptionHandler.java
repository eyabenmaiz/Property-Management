package com.property.Property_Service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 Property Not Found ───────────────────────────────────────────────
    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PropertyNotFoundException ex) {
        log.warn("[PropertyService] Propriété non trouvée → {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("PROPERTY_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── 409 Property Not Available ───────────────────────────────────────────
    @ExceptionHandler(PropertyNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNotAvailable(PropertyNotAvailableException ex) {
        log.warn("[PropertyService] Propriété non disponible → {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("PROPERTY_NOT_AVAILABLE")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── 400 Validation (@Valid échoue) ───────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[PropertyService] Validation échouée → {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── 400 Type mismatch ────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Le paramètre '" + ex.getName() + "' doit être de type "
                + ex.getRequiredType().getSimpleName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("TYPE_MISMATCH")
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── 500 Fallback ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("[PropertyService] Erreur inattendue → type={}, message={}",
                ex.getClass().getName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_ERROR")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}