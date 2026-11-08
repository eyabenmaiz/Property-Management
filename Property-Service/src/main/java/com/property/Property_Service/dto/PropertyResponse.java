package com.property.Property_Service.dto;

import com.property.Property_Service.entity.Description;
import lombok.*;

@Data
@Builder
public class PropertyResponse {

    private Long id;
    private String type;
    private String address;
    private Double rentPrice;
    private Boolean available;

    private Description description;
}