package com.payment.payment_service.dto;




import com.payment.payment_service.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private PaymentStatus status;   // utilisé pour PATCH /payments/{id}/status
}
