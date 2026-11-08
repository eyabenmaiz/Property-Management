package com.contract.ContractService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractRequest {

    @NotNull(message = "tenantId est obligatoire")
    private String tenantId;

    @NotNull(message = "propertyId est obligatoire")
    private Long propertyId;

    @NotNull(message = "startDate est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "endDate est obligatoire")
    private LocalDate endDate;
}