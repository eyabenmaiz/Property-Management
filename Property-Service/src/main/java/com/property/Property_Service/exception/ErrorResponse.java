package com.property.Property_Service.exception;



import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}