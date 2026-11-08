package com.contract.ContractService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AvailableSoonResponse {

    private long availableInDays;
    private LocalDate availableDate;
    private PropertyResponse property;
}