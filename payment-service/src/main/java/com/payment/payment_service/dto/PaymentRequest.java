package com.payment.payment_service.dto;



import com.payment.payment_service.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotBlank(message = "L'ID du contrat est obligatoire")
    private String contractId;

    @NotNull(message = "L'ID de la propriété est obligatoire")
    private Long propertyId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @DecimalMax(value = "99999.99", message = "Le montant ne peut pas dépasser 99 999")
    private Double amount;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod method;   // CASH, BANK_TRANSFER, CARD

    @NotNull(message = "La date de paiement est obligatoire")
    private LocalDate paymentDate;

    private String description;     // optionnel ex: "Loyer Avril 2026"
}