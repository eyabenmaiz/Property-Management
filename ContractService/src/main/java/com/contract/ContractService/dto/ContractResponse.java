package com.contract.ContractService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponse {

    private String id;
    private String tenantId;
    private Long propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private boolean active;
}