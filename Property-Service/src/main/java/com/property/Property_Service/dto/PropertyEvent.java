package com.property.Property_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyEvent {
    private Long propertyId;
    private String eventType;
    private Double rentPrice;
    private String address;
    private Integer rooms;
    private Double surface;
    private String criteria;
}