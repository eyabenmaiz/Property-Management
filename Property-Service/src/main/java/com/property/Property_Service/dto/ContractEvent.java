package com.property.Property_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractEvent {
    private String contractId;
    private String eventType;
    private String tenantId;
    private Long propertyId;
    private Double monthlyRent;
}