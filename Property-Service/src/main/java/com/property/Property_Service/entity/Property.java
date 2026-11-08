package com.property.Property_Service.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le type est obligatoire")
    @Column(nullable = false)
    private String type;

    @NotBlank(message = "L'adresse est obligatoire")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Column(nullable = false)
    private Double rentPrice;

    @Valid
    @Embedded
    private Description description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean available = true;
}