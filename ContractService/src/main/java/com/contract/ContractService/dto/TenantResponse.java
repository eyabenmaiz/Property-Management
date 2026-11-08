package com.contract.ContractService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantResponse {

    private String id;
    private String name;
    private String cin;
    private String phone;
}