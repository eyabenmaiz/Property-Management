package com.contract.ContractService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String cin;

    @NotBlank
    private String phone;
}