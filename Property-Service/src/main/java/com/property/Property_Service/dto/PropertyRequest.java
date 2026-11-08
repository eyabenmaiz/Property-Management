package com.property.Property_Service.dto;

import com.property.Property_Service.entity.Description;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class PropertyRequest {

    private String type;
    private String address;
    private Double rentPrice;

    @Valid
    @NotNull(message = "La description est obligatoire")
    private Description description;
}