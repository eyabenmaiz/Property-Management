package com.payment.payment_service.feign;


import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractResponse {

    private String id;
    private String tenantId;
    private Long propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private boolean active;
}
