package com.payment.payment_service.exception;



import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage()));
    }


    @ExceptionHandler(ContractNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContractNotFound(ContractNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(HttpStatus.NOT_FOUND, "CONTRACT_NOT_FOUND", ex.getMessage()));
    }


    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(PaymentAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(HttpStatus.CONFLICT, "PAYMENT_ALREADY_EXISTS", ex.getMessage()));
    }


    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidPaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message));
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Le paramètre '" + ex.getName() + "' doit être de type "
                + ex.getRequiredType().getSimpleName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "Une erreur inattendue s'est produite."));
    }

    private ErrorResponse build(HttpStatus status, String error, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}