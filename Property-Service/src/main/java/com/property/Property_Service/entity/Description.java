package com.property.Property_Service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Description {

    private Integer rooms;
    private Double surface;
    private String criteria;
}