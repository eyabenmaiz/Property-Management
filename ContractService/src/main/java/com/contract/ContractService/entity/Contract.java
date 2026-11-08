package com.contract.ContractService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String tenantId;

    @Column(nullable = false)
    private Long propertyId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private Double monthlyRent;

    private boolean active = true;
}