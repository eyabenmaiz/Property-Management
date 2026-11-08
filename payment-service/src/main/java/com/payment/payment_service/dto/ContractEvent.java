package com.payment.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractEvent {
    private String contractId;
    private String eventType;      // "CONTRACT_CREATED", "CONTRACT_TERMINATED"
    private String tenantId;
    private Long propertyId;
    private Double monthlyRent;
}