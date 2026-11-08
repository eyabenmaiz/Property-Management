package com.payment.payment_service.dto;



import com.payment.payment_service.entity.PaymentMethod;
import com.payment.payment_service.entity.PaymentStatus;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private String contractId;
    private Long propertyId;
    private Double amount;
    private PaymentStatus status;   // PENDING, COMPLETED, FAILED, REFUNDED
    private PaymentMethod method;
    private LocalDate paymentDate;
    private String description;
}
