package com.payment.payment_service.dto;



import com.payment.payment_service.entity.PaymentStatus;
import lombok.*;
import java.time.LocalDate;

// Version allégée — utilisée pour les listes et les récapitulatifs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {
    private Long id;
    private Double amount;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String description;
}