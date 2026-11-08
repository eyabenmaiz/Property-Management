package com.payment.payment_service.dto;

import com.payment.payment_service.entity.PaymentMethod;
import com.payment.payment_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private String eventType;      // "PAYMENT_COMPLETED", "PAYMENT_FAILED", "PAYMENT_REFUNDED"
    private String contractId;
    private Long propertyId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDate paymentDate;
}