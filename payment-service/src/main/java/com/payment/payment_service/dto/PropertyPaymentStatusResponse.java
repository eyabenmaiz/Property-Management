package com.payment.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyPaymentStatusResponse {

    private Long propertyId;
    private String contractId;
    private boolean paid;
}