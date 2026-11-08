package com.property.Property_Service.dto;



import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {

    @NotNull(message = "Le champ 'available' est obligatoire")
    private Boolean available;
}