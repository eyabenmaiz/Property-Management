package com.payment.payment_service.feign;

import lombok.Data;

@Data
public class PropertyResponse {

    private Long id;
    private String type;
    private String address;
    private Double rentPrice;
    private Boolean available;
}